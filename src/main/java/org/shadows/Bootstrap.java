package org.shadows;

import lombok.extern.slf4j.Slf4j;
import org.shadows.bot.telegram.GptBot;
import org.shadows.client.openai.GptClient;
import org.shadows.client.openai.OpenAiServiceExt;
import org.shadows.client.opentts.OpenTTSClientFactory;
import org.shadows.converter.TTSConverter;
import org.shadows.server.HealthServer;

import java.io.IOException;
import java.time.Duration;
import java.util.Properties;

/**
 * Starter class
 *
 * @author bayura-ea
 */
@Slf4j
public final class Bootstrap {

    private Bootstrap() {
    }

    public static void main(String[] args) throws IOException {
        log.info("Loading configs and init objects...");
        Properties properties = new AppProperties();
        log.debug("Configs: {}", properties);
        OpenAiServiceExt openAiServiceExt = OpenAiServiceExt.getInstance(properties.getProperty("gpt.api_key"),
                Duration.parse(properties.getProperty("gpt.client.read-timeout")));
        GptClient chatService = new GptClient(openAiServiceExt, properties);
        TTSConverter ttsConverter = new TTSConverter(OpenTTSClientFactory.getInstance(properties), chatService);
        GptBot gptBot = new GptBot(properties, chatService, ttsConverter);
        HealthServer server = new HealthServer(properties);
        log.info("Loading configs and init objects...done");
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            log.info("Shutting down...");
            server.close();
            gptBot.close();
            chatService.close();
            log.info("Shutting down...done");
        }));
    }

}
