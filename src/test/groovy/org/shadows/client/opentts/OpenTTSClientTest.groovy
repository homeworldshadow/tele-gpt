package org.shadows.client.opentts

import spock.lang.Ignore
import spock.lang.Specification

import java.time.Duration

/**
 * Fill the comment
 *
 * @author bayura-ea
 */
class OpenTTSClientTest extends Specification {


    @Ignore
    def "test"() {
        setup:
        OpenTTSClient openTTSClient = OpenTTSClient.getInstance(new URL('http://192.168.50.170:5500/'), Duration.parse("PT10S"))
        when:
        //Call<Map<String, OpenTTSVoice>> call = openTTSClient.getApi().voices(null, null, null, null)
        Map<String, OpenTTSVoice> response = openTTSClient.voices(null, null, null, null)
        then:
        response != null


    }

}