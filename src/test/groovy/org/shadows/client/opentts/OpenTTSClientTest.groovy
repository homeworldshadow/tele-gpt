package org.shadows.client.opentts

import okhttp3.MediaType
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Response
import spock.lang.Specification

import java.nio.file.Path


/**
 *
 * @author bayura-ea
 */
class OpenTTSClientTest extends Specification {


    def "Request voices from OpenTTS"() {
        setup:
        def voiceMap = ['voice': new OpenTTSVoice()]
        OpenTTSApi apiMock = Mock()
        Call<Map<String, OpenTTSVoice>> voiceCallMock = Mock()
        Response<Map<String, OpenTTSVoice>> responseMock = Response.success(voiceMap)
        voiceCallMock.execute() >> responseMock
        apiMock.voices(_, _, _, _) >> voiceCallMock
        OpenTTSClient openTTSClient = new OpenTTSClient(apiMock, [:], OpenTTSClient.VoiceQuality.medium)
        expect:
        openTTSClient.voices(null, null, null, null) == voiceMap
    }


    def "Exception on unsuccessful voices request from OpenTTS"() {
        setup:
        OpenTTSApi apiMock = Mock()
        Call<Map<String, OpenTTSVoice>> voiceCallMock = Mock()
        Response<Map<String, OpenTTSVoice>> responseMock = Response.error(400,
                ResponseBody.create('Error', MediaType.parse('plain/text')))
        voiceCallMock.execute() >> responseMock
        apiMock.voices(_, _, _, _) >> voiceCallMock
        OpenTTSClient openTTSClient = new OpenTTSClient(apiMock, [:], OpenTTSClient.VoiceQuality.medium)
        when:
        openTTSClient.voices(null, null, null, null)
        then:
        thrown(OpenTTSException)
    }


    def "Text to speech audio file via OpenTTS"() {
        setup:
        OpenTTSApi apiMock = Mock()
        Call<ResponseBody> responseBodyCall = Mock()
        responseBodyCall.execute() >> Response.success(ResponseBody.create(voiceData,
                MediaType.parse('plain/text')))
        apiMock.textToSpeech(_, _, _, _, _) >> responseBodyCall
        OpenTTSClient openTTSClient = new OpenTTSClient(apiMock, ['lang': 'voice'], OpenTTSClient.VoiceQuality.medium)
        when:
        Path path = openTTSClient.textToSpeechFile('lang', 'text')
        then:
        path.toFile().exists()
        path.toFile().text == voiceData
        cleanup:
        path.toFile().delete()
        where:
        voiceData << ['voice_data', '']
    }

    def "Empty text to speech audio file via OpenTTS on unsuccessful response"() {
        setup:
        OpenTTSApi apiMock = Mock()
        Call<ResponseBody> responseBodyCall = Mock()
        responseBodyCall.execute() >> Response.error(400, ResponseBody.create('Error',
                MediaType.parse('plain/text')))
        apiMock.textToSpeech(_, _, _, _, _) >> responseBodyCall
        OpenTTSClient openTTSClient = new OpenTTSClient(apiMock, ['lang': 'voice'], OpenTTSClient.VoiceQuality.medium)
        when:
        Path path = openTTSClient.textToSpeechFile('lang', 'text')
        then:
        path.toFile().exists()
        path.toFile().text.isEmpty()
        cleanup:
        path.toFile().delete()
    }
}