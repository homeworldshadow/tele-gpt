package org.shadows.client;

import com.theokanning.openai.completion.chat.ChatMessage;
import com.theokanning.openai.image.ImageResult;

/**
 * Fill the comment
 *
 * @author bayura-ea
 */
public class GenericResponse<T> {

    private T response;

    public GenericResponse(T response) {
        this.response = response;
    }

    public boolean isMessage() {
        return response instanceof ChatMessage;
    }

    public boolean isImage() {
        return response instanceof ImageResult;
    }

    public <R> R getResponse() {
        return (R) response;
    }
}
