package org.shadows.bot.telegram;

import com.pengrad.telegrambot.TelegramBot;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.shadows.client.GptClient;

import java.io.Closeable;
import java.net.MalformedURLException;
import java.util.Properties;

/**
 * Fill the comment
 *
 * @author bayura-ea
 */
@Slf4j
@Getter
public class GptBot implements Closeable {

    private final TelegramBot bot;

    public GptBot(Properties properties, GptClient chatService) throws MalformedURLException {
        log.info("Starting bot...");
        this.bot = new TelegramBot.Builder(properties.getProperty("tg.api_key"))
                //.debug()
                .build();
        bot.setUpdatesListener(new GptHandler(bot, chatService, properties));
        log.info("Starting bot...done");
    }

    @Override
    public void close() {
        bot.shutdown();
    }
}
