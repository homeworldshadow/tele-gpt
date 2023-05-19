package org.shadows.client.opentts;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import lombok.extern.slf4j.Slf4j;
import okhttp3.ConnectionPool;
import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;
import retrofit2.converter.jackson.JacksonConverterFactory;

import java.net.MalformedURLException;
import java.net.URL;
import java.time.Duration;
import java.util.Map;
import java.util.Optional;
import java.util.Properties;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * OpenTTSClient factory
 *
 * @author bayura-ea
 */
@Slf4j
public final class OpenTTSClientFactory {

    private static final String OPENTTS_VOICE_PREFIX = "opentts.tts-voice.";

    private OpenTTSClientFactory() {
    }

    public static OpenTTSClient getInstance(Properties properties) {

        String urlStr = properties.getProperty("opentts.url");
        URL url = Optional.ofNullable(urlStr)
                .map(spec -> {
                    try {
                        return new URL(spec);
                    } catch (MalformedURLException e) {
                        throw new OpenTTSException("Failed to create OpenTTSClient. OpenTTS url is incorrect", e);
                    }
                })
                .orElseThrow(() -> new OpenTTSException("Failed to create OpenTTSClient. OpenTTS url is undefined"));

        log.info("OpenTTS URL config found: {}", url);
        Duration timeout = Optional.ofNullable(properties.getProperty("opentts.timeout"))
                .map(Duration::parse)
                .orElse(Duration.parse("PT10S"));

        Map<String, String> voiceMap = properties.entrySet().stream()
                .filter(e -> ((String) e.getKey()).startsWith(OPENTTS_VOICE_PREFIX)
                        && ((String) e.getKey()).length() > OPENTTS_VOICE_PREFIX.length()
                        && e.getValue() != null)
                .collect(Collectors.toMap(
                        k -> ((String) k.getKey()).substring(OPENTTS_VOICE_PREFIX.length()),
                        v -> ((String) v.getValue()).trim().toLowerCase()));

        ObjectMapper mapper = defaultObjectMapper();
        HttpLoggingInterceptor logger = new HttpLoggingInterceptor();
        logger.setLevel(HttpLoggingInterceptor.Level.BODY);
        OkHttpClient client = defaultClient(timeout)
                .newBuilder()
                .addInterceptor(logger)
                .build();
        Retrofit retrofit = defaultRetrofit(url, client, mapper);
        OpenTTSClient.VoiceQuality quality = OpenTTSClient.VoiceQuality.high;
        try {
            quality = Optional.ofNullable(properties.getProperty("opentts.quality"))
                    .map(OpenTTSClient.VoiceQuality::valueOf)
                    .orElse(OpenTTSClient.VoiceQuality.high);

        } catch (Exception e) {
            log.error("Voice quality parameter is unkmown: {}", properties.getProperty("opentts.quality"));
        }
        OpenTTSApi api = retrofit.create(OpenTTSApi.class);
        return new OpenTTSClient(api, voiceMap, quality);
    }

    public static Retrofit defaultRetrofit(URL url, OkHttpClient client, ObjectMapper mapper) {
        return new Retrofit.Builder()
                .baseUrl(url)
                .client(client)
                .addConverterFactory(JacksonConverterFactory.create(mapper))
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .build();
    }

    public static OkHttpClient defaultClient(Duration timeout) {
        return new OkHttpClient.Builder()
                .connectionPool(new ConnectionPool(5, 1, TimeUnit.SECONDS))
                .readTimeout(timeout.toMillis(), TimeUnit.MILLISECONDS)
                .build();
    }

    public static ObjectMapper defaultObjectMapper() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        mapper.setPropertyNamingStrategy(PropertyNamingStrategies.SNAKE_CASE);
        return mapper;
    }


}
