package org.shadows.client;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;

/**
 * Fill the comment
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
