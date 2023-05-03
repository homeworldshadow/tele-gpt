package org.shadows.bot.telegram;

import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.UpdatesListener;
import com.pengrad.telegrambot.model.Message;
import com.pengrad.telegrambot.model.Update;
import com.pengrad.telegrambot.request.*;
import com.pengrad.telegrambot.response.SendResponse;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.shadows.client.GptClient;
import org.shadows.client.opentts.OpenTTSClient;
import org.shadows.converter.TTSConverter;
import org.shadows.utils.Retry;
import ws.schild.jave.EncoderException;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.List;
import java.util.Optional;
import java.util.Properties;

/**
 * Fill the comment
 *
 * @author bayura-ea
 */
@Slf4j
public class GptHandler implements UpdatesListener {

    enum ResponseType {
        TEXT, IMAGE, VOICE
    }

    private final GptClient gptClient;
    private final TelegramBot bot;
    private final int retryMax;
    private final Duration retryTimeout;
    private final TTSConverter ttsConverter;


    public GptHandler(TelegramBot bot, GptClient gptClient, Properties properties) throws MalformedURLException {
        this.gptClient = gptClient;
        this.bot = bot;
        this.retryMax = Optional.ofNullable(properties.getProperty("tg.retry.max"))
                .map(Integer::parseInt)
                .orElse(1);
        this.retryTimeout = Optional.ofNullable(properties.getProperty("tg.retry.timeout"))
                .map(Duration::parse)
                .orElse(Duration.parse("PT10S"));

        String openttsUrl = properties.getProperty("opentts.url");
        OpenTTSClient openTTSClient = null;
        if (openttsUrl != null) {
            openTTSClient = OpenTTSClient.getInstance(new URL(openttsUrl),
                    Optional.ofNullable(properties.getProperty("opentts.timeout"))
                            .map(Duration::parse)
                            .orElse(Duration.parse("PT10S")));
        }
        this.ttsConverter = new TTSConverter(openTTSClient, gptClient);
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
        Long chatId = upd.message().chat().id();
        Optional<String> textMessage = toText(upd.message());
        if (textMessage.isPresent()) {
            Optional<AbstractSendRequest<?>> request = switch (getType(upd.message())) {
                case TEXT -> textAnswer(chatId, textMessage.get());
                case IMAGE -> imageAnswer(chatId, textMessage.get());
                case VOICE -> voiceAnswer(chatId, textMessage.get(), true);
            };
            request.ifPresent(r -> {
                response(r);
                cleanup(r);
            });

        }
        return null;
    }


    private Optional<String> toText(Message message) throws IOException, EncoderException {
        String result = null;
        if (message.text() != null) {
            result = message.text();
        } else if (message.voice() != null) {
            result = ttsConverter.voiceToText(message.voice().mimeType(),
                    bot.getFileContent(bot.execute(new GetFile(message.voice().fileId())).file()));
        }
        return Optional.ofNullable(result);
    }

    private ResponseType getType(Message message) {
        if (message.text() != null) {
            return ResponseType.TEXT;
        } else if (message.text() != null && message.text().startsWith("Imagine,")) {
            return ResponseType.IMAGE;
        } else if (message.voice() != null) {
            return ResponseType.VOICE;
        }
        return ResponseType.TEXT;
    }

    private Optional<AbstractSendRequest<?>> textAnswer(Long chatId, String text) {
        return gptClient.textAnswer(chatId, text).map(msg -> new SendMessage(chatId, msg.getContent()));
    }

    private Optional<AbstractSendRequest<?>> imageAnswer(Long chatId, String text) {
        if (text != null && text.startsWith("Imagine,")) {
            return gptClient.imageAnswer(chatId, text).map(img -> new SendPhoto(chatId, img.getData().get(0).getUrl()));
        }
        return Optional.empty();
    }

    private Optional<AbstractSendRequest<?>> voiceAnswer(Long chatId, String text, boolean textResponseFallback)
            throws IOException, EncoderException {
        AbstractSendRequest<?> result = null;
        Path voicePath = ttsConverter.textToVoice(text);
        if (voicePath != null) {
            result = new SendVoice(chatId, voicePath.toFile());
        } else if (textResponseFallback) {
            result = new SendMessage(chatId, text);
        }
        return Optional.ofNullable(result);
    }


    private void response(AbstractSendRequest<?> request) {
        if (request != null) {
            SendResponse result = bot.execute(request);
            log.debug("Response: isOk={}, text={}",
                    result.isOk(), result.message().text());
        }
    }


    @SneakyThrows
    private void cleanup(AbstractSendRequest<?> request) {
        if (request instanceof SendVoice voice) {
            File file = (File) voice.getParameters().get("voice");
            if (file != null) {
                Files.delete(file.toPath());
            }
        }
    }


}
