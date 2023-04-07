package org.shadows.bot.telegram;

import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.UpdatesListener;
import com.pengrad.telegrambot.model.Update;
import com.pengrad.telegrambot.request.AbstractSendRequest;
import com.pengrad.telegrambot.request.SendMessage;
import com.pengrad.telegrambot.request.SendPhoto;
import com.pengrad.telegrambot.response.SendResponse;
import com.theokanning.openai.completion.chat.ChatMessage;
import com.theokanning.openai.image.ImageResult;
import lombok.extern.slf4j.Slf4j;
import org.shadows.client.GenericResponse;
import org.shadows.client.GptClient;

import java.util.List;

/**
 * Fill the comment
 *
 * @author bayura-ea
 */
@Slf4j
public class GptHandler implements UpdatesListener {

    private final GptClient chatService;
    private final TelegramBot bot;

    public GptHandler(TelegramBot bot, GptClient chatService) {
        this.chatService = chatService;
        this.bot = bot;
    }

    @Override
    public int process(List<Update> updates) {
        for (Update upd : updates) {
            log.debug("Received: text={}", upd.message().text());
            GenericResponse<?> gptResponse = chatService.ask(upd.message().chat().id(), upd.message().text());
            AbstractSendRequest<?> tgRequest = null;
            if (gptResponse.isMessage()) {
                tgRequest = new SendMessage(upd.message().chat().id(),
                        ((ChatMessage) gptResponse.getResponse()).getContent());
            } else if (gptResponse.isImage()) {
                ImageResult imageResult = gptResponse.getResponse();
                tgRequest = new SendPhoto(upd.message().chat().id(),
                        imageResult.getData().get(0).getUrl());
            }
            if (tgRequest != null) {
                SendResponse tgResponse = bot.execute(tgRequest);
                log.debug("Response: text={}, Response: isOk={}, text={}",
                        upd.message().text(), tgResponse.isOk(), tgResponse.message().text());
            } else {
                log.debug("Unprocessed message: text={}",
                        upd.message().text());
            }
        }
        return UpdatesListener.CONFIRMED_UPDATES_ALL;
    }


}