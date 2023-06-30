package org.free.bot.telegram;

import com.pengrad.telegrambot.TelegramBot;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.free.AppProperties;
import org.free.client.openai.GptClient;
import org.free.converter.TTSConverter;

import java.io.Closeable;

/**
 * Telegram GPT bot starter
 *
 * @author bayura-ea
 */
@Slf4j
@Getter
public class GptBot implements Closeable {

    private final TelegramBot bot;

    public GptBot(AppProperties properties, GptClient chatService, TTSConverter ttsConverter) {
        log.info("Starting bot...");
        this.bot = new TelegramBot.Builder(properties.tgApiKey())
                //.debug()
                .build();
        bot.setUpdatesListener(new GptHandler(bot, chatService, ttsConverter, properties));
        log.info("Starting bot...done");
    }

    @Override
    public void close() {
        bot.shutdown();
    }
}
