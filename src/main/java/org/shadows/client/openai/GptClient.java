package org.shadows.client.openai;

import com.theokanning.openai.completion.chat.ChatCompletionRequest;
import com.theokanning.openai.completion.chat.ChatMessage;
import com.theokanning.openai.image.ImageResult;
import lombok.extern.slf4j.Slf4j;
import org.shadows.AppProperties;

import java.io.Closeable;
import java.nio.file.Path;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * GPT client
 *
 * @author bayura-ea
 */
@Slf4j
public class GptClient implements Closeable {

    private final OpenAiServiceExt service;

    private final AppProperties properties;


    private final Map<Long, ChatContext> chatContextMap = new ConcurrentHashMap<>();

    public GptClient(OpenAiServiceExt openAiService, AppProperties properties) {
        this.service = openAiService;
        this.properties = properties;
    }


    public Optional<ChatMessage> textAnswer(Long id, String text) {
        Optional<ChatMessage> result = Optional.empty();
        if (text != null) {
            log.debug("chatId={}, message={}, type=text", id, text);
            ChatCompletionRequest chatCompletionRequest = chatContext(id).textRequest(text);
            if (chatCompletionRequest != null
                    && chatCompletionRequest.getMessages() != null
                    && !chatCompletionRequest.getMessages().isEmpty()) {
                result = service.createChatCompletion(chatCompletionRequest)
                        .getChoices().stream()
                        .findFirst()
                        .map(c -> {
                            chatContext(id).textResponse(c.getMessage());
                            return c.getMessage();
                        });
            }
        }
        return result;
    }

    public Optional<ImageResult> imageAnswer(Long id, String text) {
        Optional<ImageResult> result = Optional.empty();
        if (text != null && text.startsWith("Imagine,")) {
            log.debug("chatId={}, message={}, type=image", id, text);
            result = Optional.ofNullable(service.createImage(
                    chatContext(id).imageRequest(text.substring(7))));
        }
        return result;
    }

    private ChatContext chatContext(Long id) {
        return chatContextMap.compute(id,
                (idd, ctx) -> ctx == null ? new ChatContext(properties) : ctx);
    }


    public String voiceToText(Path filePath) {
        TranscriptionsRequest request = TranscriptionsRequest.builder()
                .model("whisper-1")
                .file(filePath.toString())
                .build();
        Transcription transcription = service.createTranscription(request);
        return transcription.getText();
    }


    @Override
    public void close() {
        if (service != null) {
            service.shutdownExecutor();
        }
    }
}
