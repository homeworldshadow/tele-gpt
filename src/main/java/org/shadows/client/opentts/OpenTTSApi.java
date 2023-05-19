package org.shadows.client.opentts;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Headers;
import retrofit2.http.Query;

import java.util.Map;

/**
 * OpenTTS API
 *
 * @author bayura-ea
 */
public interface OpenTTSApi {

    @Headers({"Accept: */*"})
    @GET("/api/tts")
    Call<ResponseBody> textToSpeech(@Query("voice") String voice,
                                    @Query("text") String text,
                                    @Query("vocoder") String vocoder,
                                    @Query("denoiserStrength") Number denoiserStrength,
                                    @Query("cache") Boolean cache
    );


    @Headers({"Accept: */*"})
    @GET("/api/voices")
    Call<Map<String, OpenTTSVoice>> voices(@Query("tts_name") String ttsName,
                                           @Query("language") String language,
                                           @Query("locale") String locale,
                                           @Query("gender") Number gender
    );

}
