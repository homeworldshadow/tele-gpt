package org.shadows.client;

import com.theokanning.openai.completion.chat.ChatCompletionRequest;
import com.theokanning.openai.completion.chat.ChatMessage;
import com.theokanning.openai.image.CreateImageRequest;
import lombok.extern.slf4j.Slf4j;

import java.util.*;

import static com.theokanning.openai.completion.chat.ChatMessageRole.USER;

/**
 * Fill the comment
 *
 * @author bayura-ea
 */
@Slf4j
public class ChatContext {

    private final String model;
    private final String imageSize;

    private final List<ChatMessage> messageList = new ArrayList<>();

    public ChatContext(Properties properties) {
        this.model = Objects.requireNonNull(properties.getProperty("gpt.model"), "GPT model is required");
        this.imageSize = Optional.ofNullable(properties.getProperty("gpt.image.size"))
                .orElseGet(() -> {
                    log.warn("Default 512x512 image size is used");
                    return "512x512";
                });
    }

    public ChatCompletionRequest textRequest(String message) {
        if (!message.isBlank() && message.toLowerCase().startsWith("new topic")) {
            messageList.clear();
        }
        ChatMessage chatMessage = new ChatMessage(USER.value(), message);
        messageList.add(chatMessage);
        return ChatCompletionRequest.builder()
                .model(model)
                .messages(messageList)
                .build();
    }

    public CreateImageRequest imageRequest(String message) {
        return CreateImageRequest.builder()
                .n(1)
                .prompt(message)
                .size(imageSize)
                .responseFormat("url")
                .build();
    }

    public void textResponse(ChatMessage chatMessage) {
        messageList.add(chatMessage);
    }


}
