package org.free.client.opentts;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.Map;

/**
 * OpenTTS voice descriptor
 *
 * @author bayura-ea
 */
@Data
public class OpenTTSVoice {

    private String gender;
    private String id;
    private String language;
    private String locale;
    private Boolean multispeaker;
    private String name;
    private Map<String, Integer> speakers;
    private String tag;

    @JsonProperty("tts_name")
    private String ttsName;


}
