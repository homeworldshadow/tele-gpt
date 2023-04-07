package org.shadows;

import lombok.extern.slf4j.Slf4j;
import org.shadows.bot.telegram.GptBot;
import org.shadows.client.GptClient;
import org.shadows.server.HealthServer;

import java.io.IOException;
import java.util.Properties;

/**
 * Fill the comment
 *
 * @author bayura-ea
 */
@Slf4j
public final class Bootstrap {

    private Bootstrap() {
    }

    public static void main(String[] args) throws IOException {
        log.info("Loading configs...");
        Properties properties = new AppProperties();
        log.debug("Configs: {}", properties);
        log.info("Loading configs...done");
        GptClient chatService = new GptClient(properties);
        GptBot gptBot = new GptBot(properties, chatService);
        HealthServer server = new HealthServer(properties);
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            log.info("Shutting down...");
            server.close();
            gptBot.close();
            chatService.close();
            log.info("Shutting down...done");
        }));
    }
}
