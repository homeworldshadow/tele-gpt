package org.free;

import com.github.pemistahl.lingua.api.LanguageDetectorBuilder;
import lombok.extern.slf4j.Slf4j;
import org.free.bot.telegram.GptBot;
import org.free.client.openai.GptClient;
import org.free.client.openai.OpenAiServiceExt;
import org.free.client.opentts.OpenTTSClientFactory;
import org.free.converter.TTSConverter;
import org.free.server.HealthServer;

import java.io.IOException;

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
        var properties = new AppProperties();
        log.debug("Configs: {}", properties);
        var openAiServiceExt = OpenAiServiceExt.getInstance(properties.gptApiKey(), properties.gptClientReadTimeout());
        var chatService = new GptClient(openAiServiceExt, properties);
        var ttsConverter = new TTSConverter(OpenTTSClientFactory.getInstance(properties), chatService,
                LanguageDetectorBuilder.fromAllLanguages().build());
        var gptBot = new GptBot(properties, chatService, ttsConverter);
        var server = new HealthServer(properties);
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
