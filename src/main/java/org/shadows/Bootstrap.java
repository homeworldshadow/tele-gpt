package org.shadows;

import lombok.extern.slf4j.Slf4j;
import org.shadows.bot.telegram.GptBot;
import org.shadows.client.openai.GptClient;
import org.shadows.client.opentts.OpenTTSClient;
import org.shadows.converter.TTSConverter;
import org.shadows.server.HealthServer;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.time.Duration;
import java.util.Optional;
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
        GptClient chatService = new GptClient(properties);
        TTSConverter ttsConverter = new TTSConverter(buildOpenTTSClient(properties), chatService);
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

    private static OpenTTSClient buildOpenTTSClient(Properties properties) throws MalformedURLException {
        String openttsUrl = properties.getProperty("opentts.url");
        OpenTTSClient openTTSClient = null;
        if (openttsUrl != null) {
            log.info("OpenTTS URL config found: {}", openttsUrl);
            openTTSClient = OpenTTSClient.getInstance(new URL(openttsUrl),
                    Optional.ofNullable(properties.getProperty("opentts.timeout"))
                            .map(Duration::parse)
                            .orElse(Duration.parse("PT10S")));
        }
        return openTTSClient;
    }

}
