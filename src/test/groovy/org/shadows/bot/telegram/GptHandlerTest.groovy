package org.shadows.bot.telegram

import com.pengrad.telegrambot.TelegramBot
import com.pengrad.telegrambot.UpdatesListener
import com.pengrad.telegrambot.model.Chat
import com.pengrad.telegrambot.model.Message
import com.pengrad.telegrambot.model.Update
import com.pengrad.telegrambot.model.Voice
import com.pengrad.telegrambot.request.GetFile
import com.pengrad.telegrambot.request.SendMessage
import com.pengrad.telegrambot.request.SendPhoto
import com.pengrad.telegrambot.request.SendVoice
import com.pengrad.telegrambot.response.GetFileResponse
import com.theokanning.openai.completion.chat.ChatMessage
import com.theokanning.openai.image.Image
import com.theokanning.openai.image.ImageResult
import org.shadows.AppProperties
import org.shadows.client.openai.GptClient
import org.shadows.client.openai.OpenAiServiceExt
import org.shadows.converter.TTSConverter
import spock.lang.Specification

import java.nio.file.Path

/**
 * @author bayura-ea
 */
class GptHandlerTest extends Specification {

    def "Handle text request/response messages"() {
        setup:
        def chatId = 1L
        TelegramBot botMock = Mock()
        GptClient gptClientMock = Mock()
        TTSConverter ttsConverterMock = Mock()
        def properties = new AppProperties()
        properties['tg.retry.max'] = '0'
        GptHandler handler = new GptHandler(botMock, gptClientMock, ttsConverterMock, properties)
        gptClientMock.textAnswer(chatId, inputMessage) >> Optional.of(new ChatMessage(content: outputMessage))
        when:
        Update update = new Update(message:
                new Message(chat:
                        new Chat(id: chatId),
                        text: inputMessage
                )
        )
        def result = handler.process([update])
        then:
        noExceptionThrown()
        result == UpdatesListener.CONFIRMED_UPDATES_ALL
        invocation_count * botMock.execute({ it.parameters['text'] == outputMessage }  as SendMessage)
        where:
        inputMessage | outputMessage | invocation_count
        'in'         | 'out'         | 1
        ''           | ''            | 1
        null         | 'out'         | 0
        'in'         | null          | 1
    }


    def "Handle text request and image as response messages"() {
        setup:
        def chatId = 1L
        TelegramBot botMock = Mock()
        GptClient gptClientMock = Mock()
        TTSConverter ttsConverterMock = Mock()
        def properties = new AppProperties()
        properties['tg.retry.max'] = '0'
        GptHandler handler = new GptHandler(botMock, gptClientMock, ttsConverterMock, properties)
        gptClientMock.textAnswer(chatId, inputMessage) >> Optional.of(new ChatMessage(content: '_dummy_'))
        gptClientMock.imageAnswer(chatId, inputMessage) >> Optional.of(new ImageResult(data: [new Image(url: imageUrl)]))
        when:
        Update update = new Update(message:
                new Message(chat:
                        new Chat(id: chatId),
                        text: inputMessage
                )
        )
        def result = handler.process([update])
        then:
        noExceptionThrown()
        result == UpdatesListener.CONFIRMED_UPDATES_ALL
        invocation_count * botMock.execute({ it.parameters['photo'] == imageUrl } as SendPhoto)
        where:
        inputMessage     | imageUrl   | invocation_count
        'Imagine, image' | 'imageUrl' | 1
        'Imagine,'       | 'imageUrl' | 1
        'Imagine,'       | ''         | 1
        'imagine,'       | 'imageUrl' | 0
        'imagine'        | 'imageUrl' | 0

    }


    def "Handle voice request/response messages"() {
        setup:
        def chatId = 1L
        def inputVoiceFileId = 'input_voice_file_id.ogg'
        def outVoicePath = Path.of("/tmp/output_voice_file.mp3")
        def outputMessage = 'output_text'

        TelegramBot botMock = Mock()
        GptClient gptClientMock = Mock()
        TTSConverter ttsConverterMock = Mock()
        def properties = new AppProperties()
        properties['tg.retry.max'] = '0'
        GptHandler handler = new GptHandler(botMock, gptClientMock, ttsConverterMock, properties)

        GetFileResponse getFileResponse = Mock()
        getFileResponse.file() >> new com.pengrad.telegrambot.model.File(file_id: inputVoiceFileId)
        botMock.execute(_ as GetFile) >> getFileResponse

        ttsConverterMock.voiceToText('ogg', _) >> inputMessage
        ttsConverterMock.textToVoice(outputMessage) >> outVoicePath

        gptClientMock.textAnswer(chatId, inputMessage) >> Optional.of(new ChatMessage(content: outputMessage))

        when:
        Update update = new Update(message:
                new Message(chat:
                        new Chat(id: chatId),
                        voice: new Voice(mime_type: 'ogg', file_id: inputVoiceFileId)
                )
        )
        def result = handler.process([update])
        then:
        noExceptionThrown()
        result == UpdatesListener.CONFIRMED_UPDATES_ALL
        invocation_count * botMock.execute({ it.parameters['voice'] == outVoicePath.toFile() } as SendVoice)
        where:
        inputMessage | invocation_count
        'in'         | 1
        ''           | 1
        null         | 0

    }


    def "Handle voice request and text response messages"() {
        setup:
        def chatId = 1L
        def inputVoiceFileId = 'input_voice_file_id.ogg'
        //def outputMessage = 'output_text'
        TelegramBot botMock = Mock()
        GptClient gptClientMock = Mock()
        TTSConverter ttsConverterMock = Mock()
        def properties = new AppProperties()
        properties['tg.retry.max'] = '0'
        GptHandler handler = new GptHandler(botMock, gptClientMock, ttsConverterMock, properties)

        GetFileResponse getFileResponse = Mock()
        getFileResponse.file() >> new com.pengrad.telegrambot.model.File(file_id: inputVoiceFileId)
        botMock.execute({ it.parameters['file_id'] == inputVoiceFileId } as GetFile) >> getFileResponse

        ttsConverterMock.voiceToText('ogg', _) >> inputMessage
        ttsConverterMock.textToVoice(outputMessage) >> null
        gptClientMock.textAnswer(chatId, inputMessage) >> Optional.of(new ChatMessage(content: outputMessage))
        when:
        Update update = new Update(message:
                new Message(chat:
                        new Chat(id: chatId),
                        voice: new Voice(mime_type: 'ogg', file_id: inputVoiceFileId)
                )
        )
        def result = handler.process([update])
        then:
        noExceptionThrown()
        result == UpdatesListener.CONFIRMED_UPDATES_ALL
        invocation_count * botMock.execute({ it.parameters['text'] == outputMessage } as SendMessage)
        where:
        inputMessage | outputMessage | invocation_count
        'in'         | 'out'         | 1
        ''           | 'out'         | 1
        ''           | ''            | 1
        'in'         | null          | 0
        null         | 'out'         | 0
    }


    def "Retry to handle request/response messages on error"() {
        setup:
        def chatId = 1L
        TelegramBot botMock = Mock()
        GptClient gptClientMock = Mock()
        TTSConverter ttsConverterMock = Mock()
        def properties = new AppProperties()
        properties['tg.retry.max'] = '1'
        properties['tg.retry.timeout'] = 'PT1S'
        GptHandler handler = new GptHandler(botMock, gptClientMock, ttsConverterMock, properties)
        int count = 0
        gptClientMock.textAnswer(chatId, inputMessage) >> {
            count++;
            if (count % 2 == 0)
                Optional.of(new ChatMessage(content: outputMessage))
            else
                throw new RuntimeException("test")
        }
        when:
        Update update = new Update(message:
                new Message(chat:
                        new Chat(id: chatId),
                        text: inputMessage
                )
        )
        def result = handler.process([update])
        then:
        noExceptionThrown()
        result == UpdatesListener.CONFIRMED_UPDATES_ALL
        invocation_count * botMock.execute({ it.parameters['text'] == outputMessage } as SendMessage)
        where:
        inputMessage | outputMessage | invocation_count
        'in'         | 'out'         | 1
    }


    def "Handle message to clear topic context"() {
        setup:
        def chatId = 1L
        def properties = new AppProperties()
        properties['tg.retry.max'] = '0'

        TelegramBot botMock = Mock()
        OpenAiServiceExt openAiServiceExt = Mock()
        GptClient gptClient = new GptClient(openAiServiceExt, properties)
        TTSConverter ttsConverterMock = Mock()
        GptHandler handler = new GptHandler(botMock, gptClient, ttsConverterMock, properties)
        when:
        Update update = new Update(message:
                new Message(chat:
                        new Chat(id: chatId),
                        text: inputMessage
                )
        )
        def result = handler.process([update])
        then:
        noExceptionThrown()
        result == UpdatesListener.CONFIRMED_UPDATES_ALL
        invocation_count * botMock.execute({ it.parameters['text'] == outputMessage } as SendMessage)
        where:
        inputMessage | outputMessage | invocation_count
        'new topic'  | 'any'         | 0
    }

}