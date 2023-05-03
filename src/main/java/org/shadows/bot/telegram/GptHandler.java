package org.shadows.bot.telegram;

import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.UpdatesListener;
import com.pengrad.telegrambot.model.Update;
import com.pengrad.telegrambot.model.Voice;
import com.pengrad.telegrambot.request.*;
import com.pengrad.telegrambot.response.SendResponse;
import com.theokanning.openai.completion.chat.ChatMessage;
import com.theokanning.openai.image.ImageResult;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.shadows.client.GptClient;
import org.shadows.client.opentts.OpenTTSClient;
import org.shadows.converter.TTSConverter;
import org.shadows.utils.Retry;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Properties;

/**
 * Fill the comment
 *
 * @author bayura-ea
 */
@Slf4j
public class GptHandler implements UpdatesListener {

    private final GptClient gptClient;
    private final TelegramBot bot;
    private final int retryMax;
    private final Duration retryTimeout;
    private final TTSConverter ttsConverter;

    public GptHandler(TelegramBot bot, GptClient gptClient, Properties properties) {
        this.gptClient = gptClient;
        this.bot = bot;
        this.retryMax = Optional.ofNullable(properties.getProperty("tg.retry.max"))
                .map(Integer::parseInt)
                .orElse(1);
        this.retryTimeout = Optional.ofNullable(properties.getProperty("tg.retry.timeout"))
                .map(Duration::parse)
                .orElse(Duration.parse("PT10S"));

        this.ttsConverter = new TTSConverter(
                Optional.ofNullable(properties.getProperty("opentts.url"))
                        .map(s -> {
                            try {
                                return new URL(s);
                            } catch (MalformedURLException e) {
                                throw new RuntimeException(e);
                            }
                        })
                        .map(url -> OpenTTSClient.getInstance(url,
                                Optional.ofNullable(properties.getProperty("opentts.timeout"))
                                        .map(Duration::parse)
                                        .orElse(Duration.parse("PT10S"))))
                        .orElse(null),
                gptClient);
    }

    @Override
    public int process(List<Update> updates) {
        for (Update upd : updates) {
            Retry.with(() -> doUpdate(upd), retryMax, retryTimeout);
        }
        return UpdatesListener.CONFIRMED_UPDATES_ALL;
    }


    @SneakyThrows
    private Void doUpdate(Update upd) {
        log.debug("Received: text={}", upd.message().text());
        if (upd.message().text() != null) {
            textResponse(upd.message().chat().id(), upd.message().text());
        } else if (upd.message().voice() != null) {
            voiceResponse(upd.message().chat().id(), upd.message().voice(), true);
        }
        return null;
    }

    private void voiceResponse(Long chatId, Voice voice, boolean textResponseFallback) throws IOException {
        Objects.requireNonNull(chatId, "Chat ID is required");
        if (voice != null) {
            String textRequest = ttsConverter.voiceToText(voice.mimeType(),
                    bot.getFileContent(bot.execute(new GetFile(voice.fileId())).file()));
            Optional<ChatMessage> textAnswer = gptClient.textAnswer(chatId, textRequest);
            Optional<Path> voicePath = textAnswer.map(chatMessage -> ttsConverter.textToVoice(chatMessage.getContent()));
            try {
                if (response(voicePath.map(path -> new SendVoice(chatId, path.toFile()))) == null
                        && textResponseFallback) {
                    response(textAnswer.map(msg -> new SendMessage(chatId, msg.getContent())));
                }
            } finally {
                if (voicePath.isPresent()) {
                    Files.delete(voicePath.get());
                }
            }
        }
    }


    private void textResponse(Long chatId, String text) {
        Objects.requireNonNull(chatId, "Chat ID is required");
        if (text != null) {
            Optional<AbstractSendRequest<?>> tgRequest;
            if (text.startsWith("Imagine,")) {
                Optional<ImageResult> imageResult = gptClient.imageAnswer(chatId, text);
                tgRequest = imageResult.map(img -> new SendPhoto(chatId,
                        img.getData().get(0).getUrl()));
            } else {
                Optional<ChatMessage> message = gptClient.textAnswer(chatId, text);
                tgRequest = message.map(msg -> new SendMessage(chatId,
                        msg.getContent()));
            }
            response(tgRequest);
        }
    }

    private SendResponse response(Optional<AbstractSendRequest<?>> request) {
        SendResponse result = null;
        if (request.isPresent()) {
            result = bot.execute(request.get());
            log.debug("Response: isOk={}, text={}",
                    result.isOk(), result.message().text());
        } else {
            log.debug("Unprocessed request: {}", request);
        }
        return result;
    }


}
