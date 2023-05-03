package org.shadows.client;

import com.theokanning.openai.OpenAiApi;
import io.reactivex.Single;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;
import retrofit2.http.PartMap;

import java.util.Map;

/**
 * Extended OpenAi API
 *
 * @author bayura-ea
 */
public interface OpenAiApiExt extends OpenAiApi {

    @Multipart
    @POST("/v1/audio/transcriptions")
    Single<Transcription> transcriptions(@PartMap Map<String, RequestBody> parts, @Part MultipartBody.Part file);
}
