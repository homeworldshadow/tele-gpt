package org.shadows.bot.telegram;

import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.UpdatesListener;
import com.pengrad.telegrambot.model.File;
import com.pengrad.telegrambot.model.Message;
import com.pengrad.telegrambot.model.Update;
import com.pengrad.telegrambot.request.AbstractSendRequest;
import com.pengrad.telegrambot.request.GetFile;
import com.pengrad.telegrambot.request.SendMessage;
import com.pengrad.telegrambot.request.SendPhoto;
import com.pengrad.telegrambot.response.SendResponse;
import com.theokanning.openai.completion.chat.ChatMessage;
import com.theokanning.openai.image.ImageResult;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.shadows.client.GptClient;
import org.shadows.converter.AudioConverter;

import java.time.Duration;
import java.io.FileOutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;
import java.util.Properties;
import java.util.function.Supplier;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

/**
 * Fill the comment
 *
 * @author bayura-ea
 */
@Slf4j
public class GptHandler implements UpdatesListener {

    private final GptClient gptClient;
    private final TelegramBot bot;

    private int retryMax;
    private Duration retryTimeout;

    public GptHandler(TelegramBot bot, GptClient chatService, Properties properties) {
        this.chatService = chatService;
    private AudioConverter audioConverter;

    public GptHandler(TelegramBot bot, GptClient gptClient) {
        this.gptClient = gptClient;
        this.bot = bot;
        this.retryMax = Optional.ofNullable(properties.getProperty("tg.retry.max"))
                .map(Integer::parseInt)
                .orElse(1);
        this.retryTimeout = Optional.ofNullable(properties.getProperty("tg.retry.timeout"))
                .map(Duration::parse)
                .orElse(Duration.parse("PT10S"));
        this.audioConverter = new AudioConverter();
    }

    @SneakyThrows
    @Override
    public int process(List<Update> updates) {
        for (Update upd : updates) {
            try {
                log.debug("Received: text={}", upd.message().text());
                String text = null;
                if (upd.message().text() != null) {
                    text = upd.message().text();
                } else if (upd.message().voice() != null) {
                    text = voiceToText(upd.message());
                }
                responseByGpt(upd.message().chat().id(), text);
            } catch (Exception e) {
                log.error(e.toString(), e);
            }
        }
        return UpdatesListener.CONFIRMED_UPDATES_ALL;
    }


    private void responseByGpt(Long chatId, String text) {
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
            if (tgRequest.isPresent()) {
                SendResponse tgResponse = bot.execute(tgRequest.get());
                log.debug("Response: text={}, Response: isOk={}, text={}",
                        text, tgResponse.isOk(), tgResponse.message().text());
            } else {
                log.debug("Unprocessed message: text={}", text);
            }
        }
    }


    @SneakyThrows
    private String voiceToText(Message message) {
        String result = null;
        if (message.voice() != null) {
            File file = bot.execute(new GetFile(message.voice().fileId())).file();
            Path tgFilePath = message.voice().mimeType().contains("ogg")
                    ? Files.createTempFile(UUID.randomUUID().toString(), ".ogg")
                    : Files.createTempFile(UUID.randomUUID().toString(), ".bin");
            Path mp3FilePath = null;
            try (FileOutputStream fos = new FileOutputStream(tgFilePath.toFile())) {
                fos.write(bot.getFileContent(file));
                mp3FilePath = audioConverter.convertToMp3(tgFilePath);
                result = gptClient.voiceToText(mp3FilePath);
            } finally {
                Files.delete(tgFilePath);
                if (mp3FilePath != null) {
                    Files.delete(mp3FilePath);
                }
            }
        }
        log.debug("Voice {} to text: {}", message.voice().fileId(), result);
        return result;
    }


}
