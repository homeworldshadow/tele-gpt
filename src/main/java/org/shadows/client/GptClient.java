package org.shadows.client;

import com.theokanning.openai.service.OpenAiService;
import lombok.extern.slf4j.Slf4j;

import java.io.Closeable;
import java.time.Duration;
import java.util.Map;
import java.util.Optional;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Fill the comment
 *
 * @author bayura-ea
 */
@Slf4j
public class GptClient implements Closeable {

    private OpenAiService service;

    private Properties properties;


    private Map<Long, ChatContext> chatContextMap = new ConcurrentHashMap<>();

    public GptClient(Properties properties) {
        service = new OpenAiService(properties.getProperty("gpt.api_key"),
                Duration.parse(properties.getProperty("gpt.client.read-timeout")));
        this.properties = properties;
    }

    public GenericResponse<?> ask(Long id, String message) {
        ChatContext chatContext = chatContextMap.compute(id,
                (idd, ctx) -> ctx == null ? new ChatContext(properties) : ctx);

        if (message.startsWith("Imagine,")) {
            log.debug("chatId={}, message={}, type=image", id, message);
            return Optional.ofNullable(service.createImage(
                            chatContext.imageRequest(message.substring(7))))
                    .map(GenericResponse::new)
                    .orElse(null);
        } else {
            log.debug("chatId={}, message={}, type=text", id, message);
            return service.createChatCompletion(chatContext.textRequest(message))
                    .getChoices().stream()
                    .findFirst()
                    .map(c -> {
                        chatContext.textResponse(c.getMessage());
                        return c.getMessage();
                    })
                    .map(GenericResponse::new)
                    .orElse(null);
        }
    }


    @Override
    public void close() {
        if (service != null) {
            service.shutdownExecutor();
        }
    }
}
