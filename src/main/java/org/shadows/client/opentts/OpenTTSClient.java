package org.shadows.client.opentts;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import io.reactivex.Single;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import okhttp3.ConnectionPool;
import okhttp3.OkHttpClient;
import okhttp3.ResponseBody;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.*;
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;
import retrofit2.converter.jackson.JacksonConverterFactory;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.time.Duration;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * Fill the comment
 *
 * @author bayura-ea
 */
@Slf4j
public class OpenTTSClient {

    private OpenTTSApi api;


    protected OpenTTSClient(OpenTTSApi api) {
        this.api = api;
    }

    public static OpenTTSClient getInstance(URL url, Duration timeout) {
        ObjectMapper mapper = defaultObjectMapper();
        HttpLoggingInterceptor logger = new HttpLoggingInterceptor();
        logger.setLevel(HttpLoggingInterceptor.Level.BODY);
        OkHttpClient client = defaultClient(timeout)
                .newBuilder()
                .addInterceptor(logger)
                .build();
        Retrofit retrofit = defaultRetrofit(url, client, mapper);
        OpenTTSApi api = retrofit.create(OpenTTSApi.class);
        return new OpenTTSClient(api);
    }


    public Path textToSpeechFile(String langIso2, String text) throws IOException {
        Call<ResponseBody> call = api.textToSpeech("larynx:" + langIso2,
                text,
                "high",
                new BigDecimal("0.03"),
                false);

        Path resultPath = Files.createTempFile(UUID.randomUUID().toString(), ".wav");
        Response<ResponseBody> response =  call.execute();
        if (response.isSuccessful()) {
            if (response.body() != null) {
                BufferedInputStream bis = new BufferedInputStream(response.body().byteStream());
                try {
                    Files.copy(bis, resultPath, StandardCopyOption.REPLACE_EXISTING);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        } else {
            log.error("Response not ok");
        }

        /*call.enqueue(new Callback<>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                if (response.isSuccessful()) {
                    if (response.body() != null) {
                        BufferedInputStream bis = new BufferedInputStream(response.body().byteStream());
                        try {
                            Files.copy(bis, resultPath);
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }
                    }
                } else {
                    log.error("Response not ok");
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                log.error(t.toString(), t);
            }
        });

         */
        return resultPath;
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
        mapper.setPropertyNamingStrategy(PropertyNamingStrategy.SNAKE_CASE);
        return mapper;
    }

    public static <T> T execute(Single<T> apiCall) {
        try {
            return apiCall.blockingGet();
        } catch (HttpException e) {
            try {
                if (e.response() == null || e.response().errorBody() == null) {
                    throw e;
                }
                String errorBody = e.response().errorBody().string();
                throw new RuntimeException(errorBody);
                // OpenAiError error = mapper.readValue(errorBody, OpenAiError.class);
                //throw new OpenAiHttpException(error, e, e.code());
            } catch (IOException ex) {
                // couldn't parse OpenAI error
                throw e;
            }
        }
    }

}
