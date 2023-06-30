package org.free.client.openai


import com.theokanning.openai.completion.chat.ChatMessage
import org.free.AppProperties
import spock.lang.Specification

/**
 * @author bayura-ea
 */
class ChatContextTest extends Specification {

    def "Context requires model property"() {
        when:
        new ChatContext(properties)
        then:
        thrown NullPointerException
        where:
        properties << [null]
    }

    def "Keep chat request/response context"() {
        setup:
        def properties = new AppProperties()
        properties['gpt.model'] = 'non-null'
        ChatContext chatContext = new ChatContext(properties)
        when:
        chatContext.textRequest('req_1')
        chatContext.textResponse(new ChatMessage(content: 'resp_1'))
        chatContext.textRequest('req_2')
        chatContext.textResponse(new ChatMessage(content: 'resp_2'))
        def request = chatContext.textRequest('req_3')
        then:
        request.messages.collect { it.content } == ['req_1', 'resp_1', 'req_2', 'resp_2', 'req_3']
    }


    def "Chat request/response context can be cleared"() {
        setup:
        def properties = new AppProperties()
        properties['gpt.model'] = 'non-null'
        ChatContext chatContext = new ChatContext(properties)
        when:
        chatContext.textRequest('req_1')
        chatContext.textResponse(new ChatMessage(content: 'resp_1'))
        def request = chatContext.textRequest('req_2')
        then:
        request.messages.collect { it.content } == ['req_1', 'resp_1', 'req_2']
        when:
        request = chatContext.textRequest('new topic')
        then:
        request.messages.collect { it.content } == []
    }

}