package org.free.client.openai;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;

/**
 * Voice transcription request
 *
 * @author bayura-ea
 */
@Data
@Builder
public class TranscriptionsRequest {

    private String file;
    private String model;
    private String prompt;
    @JsonProperty("response_format")
    private String responseFormat;
    private Number temperature;
    private String language;
}
