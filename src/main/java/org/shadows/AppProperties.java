package org.shadows;

import lombok.extern.slf4j.Slf4j;
import org.shadows.client.opentts.OpenTTSClient;
import org.shadows.client.opentts.OpenTTSException;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.time.Duration;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Properties;
import java.util.stream.Collectors;

/**
 * Properties collector
 *
 * @author bayura-ea
 */
@Slf4j
public class AppProperties extends Properties {

    private static final String OPENTTS_VOICE_PREFIX = "opentts.tts-voice.";


    public AppProperties() throws IOException {
        this.load(Bootstrap.class.getClassLoader().getResourceAsStream("application.properties"));
        for (Object key : keySet()) {
            Optional.ofNullable(System.getenv((String) key))
                    .ifPresent(v -> put(key, v));
        }
        assertRequiredProperties();
    }

    public int serverPort() {
        return Integer.parseInt(getProperty("server.port", "0"));
    }

    public String tgApiKey() {
        return getProperty("tg.api_key");
    }

    public int tgRetryMax() {
        return Integer.parseInt(getProperty("tg.retry.max", "1"));
    }

    public Duration tgRetryTimeout() {
        return Duration.parse(getProperty("tg.retry.timeout", "PT10S"));
    }

    public String gptApiKey() {
        return getProperty("gpt.api_key");
    }

    public String gptImageSize() {
        return getProperty("gpt.image.size", "512x512");
    }

    public String gptModel() {
        return getProperty("gpt.model", "gpt-3.5-turbo");
    }

    public Duration gptClientReadTimeout() {
        return Duration.parse(getProperty("gpt.client.read-timeout", "PT1M"));
    }

    public URL openttsUrl() {
        return Optional.ofNullable(getProperty("opentts.url"))
                .map(spec -> {
                    try {
                        return new URL(spec);
                    } catch (MalformedURLException e) {
                        throw new OpenTTSException("OpenTTS url is incorrect", e);
                    }
                })
                .orElseThrow(() -> new OpenTTSException("OpenTTS url is undefined"));
    }

    public Duration openttsTimeout() {
        return Duration.parse(getProperty("opentts.timeout", "PT120S"));
    }

    public Map<String, String> openttsVoiceMap() {
        return entrySet().stream()
                .filter(e -> ((String) e.getKey()).startsWith(OPENTTS_VOICE_PREFIX)
                        && ((String) e.getKey()).length() > OPENTTS_VOICE_PREFIX.length()
                        && e.getValue() != null)
                .collect(Collectors.toMap(
                        k -> ((String) k.getKey()).substring(OPENTTS_VOICE_PREFIX.length()),
                        v -> ((String) v.getValue()).trim().toLowerCase()));
    }

    public OpenTTSClient.VoiceQuality openttsQuality() {
        try {
            return Optional.ofNullable(getProperty("opentts.quality"))
                    .map(OpenTTSClient.VoiceQuality::valueOf)
                    .orElse(OpenTTSClient.VoiceQuality.medium);
        } catch (Exception e) {
            log.warn("Voice quality parameter is unkmown: {}, using {} as default",
                    getProperty("opentts.quality"), OpenTTSClient.VoiceQuality.medium);
            return OpenTTSClient.VoiceQuality.medium;
        }
    }

    private void assertRequiredProperties() {
        Objects.requireNonNull(tgApiKey());
        Objects.requireNonNull(gptApiKey());
        Objects.requireNonNull(openttsUrl());
        Objects.requireNonNull(gptModel());
    }


}
