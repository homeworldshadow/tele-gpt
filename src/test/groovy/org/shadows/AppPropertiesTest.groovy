package org.shadows

import org.shadows.client.opentts.OpenTTSClient
import org.shadows.client.opentts.OpenTTSException
import spock.lang.Specification


/**
 *
 * @author bayura-ea
 */
class AppPropertiesTest extends Specification {

    def "Exception on incorrect OpenTTS url"() {
        setup:
        AppProperties appProperties = new AppProperties()
        appProperties.put('opentts.url', 'bad')
        when:
        appProperties.openttsUrl()
        then:
        thrown(OpenTTSException)
    }

    def "Voice map for OpenTTS"() {
        setup:
        AppProperties appProperties = new AppProperties()
        appProperties.put(param, voice)
        when:
        def map = appProperties.openttsVoiceMap()
        then:
        map == result
        where:
        param                                     | voice || result
        AppProperties.OPENTTS_VOICE_PREFIX + 'en' | 'v1'  || ['en': 'v1']
        AppProperties.OPENTTS_VOICE_PREFIX + 'EN' | 'V1'  || ['EN': 'v1']
        AppProperties.OPENTTS_VOICE_PREFIX        | 'v1'  || [:]
    }

    def "Default OpenTTS quality for unknown property"() {
        setup:
        AppProperties appProperties = new AppProperties()
        appProperties.put('opentts.quality', '_unknown_')
        when:
        def result = appProperties.openttsQuality()
        then:
        result == OpenTTSClient.VoiceQuality.medium
    }
}