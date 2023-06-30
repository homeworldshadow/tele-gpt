package org.free.client.openai;

import com.theokanning.openai.completion.chat.ChatCompletionRequest;
import com.theokanning.openai.completion.chat.ChatMessage;
import com.theokanning.openai.image.CreateImageRequest;
import lombok.extern.slf4j.Slf4j;
import org.free.AppProperties;

import java.util.ArrayList;
import java.util.List;

import static com.theokanning.openai.completion.chat.ChatMessageRole.USER;

/**
 * GPT topic context
 *
 * @author bayura-ea
 */
@Slf4j
public class ChatContext {

    private final String model;
    private final String imageSize;

    private final List<ChatMessage> messageList = new ArrayList<>();

    public ChatContext(AppProperties properties) {
        this.model = properties.gptModel();
        this.imageSize = properties.gptImageSize();
    }

    public ChatCompletionRequest textRequest(String message) {
        if (!message.isBlank() && message.toLowerCase().startsWith("new topic")) {
            messageList.clear();
        } else {
            messageList.add(new ChatMessage(USER.value(), message));
        }
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
