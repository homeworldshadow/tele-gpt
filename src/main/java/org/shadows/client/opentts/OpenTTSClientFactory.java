package org.shadows.client.opentts;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import lombok.extern.slf4j.Slf4j;
import okhttp3.ConnectionPool;
import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import org.shadows.AppProperties;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;
import retrofit2.converter.jackson.JacksonConverterFactory;

import java.net.URL;
import java.time.Duration;
import java.util.concurrent.TimeUnit;

/**
 * OpenTTSClient factory
 *
 * @author bayura-ea
 */
@Slf4j
public final class OpenTTSClientFactory {

    private OpenTTSClientFactory() {
    }

    public static OpenTTSClient getInstance(AppProperties properties) {
        URL url = properties.openttsUrl();
        Duration timeout = properties.openttsTimeout();
        ObjectMapper mapper = defaultObjectMapper();
        HttpLoggingInterceptor logger = new HttpLoggingInterceptor();
        logger.setLevel(HttpLoggingInterceptor.Level.BODY);
        OkHttpClient client = defaultClient(timeout)
                .newBuilder()
                .addInterceptor(logger)
                .build();
        Retrofit retrofit = defaultRetrofit(url, client, mapper);
        OpenTTSApi api = retrofit.create(OpenTTSApi.class);
        return new OpenTTSClient(api, properties.openttsVoiceMap(), properties.openttsQuality());
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
