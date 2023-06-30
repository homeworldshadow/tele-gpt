package org.free.client.openai;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.theokanning.openai.service.OpenAiService;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.RequestBody;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;

import java.io.File;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ExecutorService;

/**
 * Extended OpenAi service
 *
 * @author bayura-ea
 */
public class OpenAiServiceExt extends OpenAiService {

    private final OpenAiApiExt apiExt;

    public static OpenAiServiceExt getInstance(String token, Duration timeout) {
        ObjectMapper mapper = defaultObjectMapper();
        HttpLoggingInterceptor logger = new HttpLoggingInterceptor();
        logger.setLevel(HttpLoggingInterceptor.Level.BODY);
        OkHttpClient client = defaultClient(token, timeout)
                .newBuilder()
                .addInterceptor(logger)
                .build();
        Retrofit retrofit = defaultRetrofit(client, mapper);
        OpenAiApiExt api = retrofit.create(OpenAiApiExt.class);
        ExecutorService executorService = client.dispatcher().executorService();
        return new OpenAiServiceExt(api, executorService);
    }

    public OpenAiServiceExt(OpenAiApiExt api, ExecutorService executorService) {
        super(api, executorService);
        this.apiExt = api;
    }

    public Transcription createTranscription(TranscriptionsRequest request) {
        java.io.File file = new File(request.getFile());
        MultipartBody.Part fileBodyPart = MultipartBody.Part.createFormData("file",
                file.getName(), RequestBody.create(file, MediaType.parse("audio")));
        Map<String, RequestBody> parts = new HashMap<>();
        parts.put("model", RequestBody.create(request.getModel(), okhttp3.MultipartBody.FORM));
        Optional.ofNullable(request.getPrompt())
                .ifPresent(s -> parts.put("prompt", RequestBody.create(s, okhttp3.MultipartBody.FORM)));
        Optional.ofNullable(request.getResponseFormat())
                .ifPresent(s -> parts.put("responseFormat", RequestBody.create(s, okhttp3.MultipartBody.FORM)));
        Optional.ofNullable(request.getTemperature())
                .map(String::valueOf)
                .ifPresent(s -> parts.put("temperature", RequestBody.create(s, okhttp3.MultipartBody.FORM)));
        Optional.ofNullable(request.getLanguage())
                .ifPresent(s -> parts.put("language", RequestBody.create(s, okhttp3.MultipartBody.FORM)));
        return execute(apiExt.transcriptions(parts, fileBodyPart));
    }


}
