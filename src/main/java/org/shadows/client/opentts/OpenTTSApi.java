package org.shadows.client.opentts;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Headers;
import retrofit2.http.Query;

/**
 * Fill the comment
 *
 * @author bayura-ea
 */
public interface OpenTTSApi {

    /*curl -X 'GET' \
            'http://localhost:5500/api/tts?voice=larynx%3Aen&text=Welcome%20to%20the%20world%20of%20speech%20synthesis%21&vocoder=high&denoiserStrength=0.03&cache=false' \
            -H 'accept: *//*'
     */

   // @Streaming
    @Headers({"Accept: */*"})
    @GET("/api/tts")
    Call<ResponseBody> textToSpeech(@Query("voice") String voice,
                                    @Query("text") String text,
                                    @Query("vocoder") String vocoder,
                                    @Query("denoiserStrength") Number denoiserStrength,
                                    @Query("cache") Boolean cache
    );

}
