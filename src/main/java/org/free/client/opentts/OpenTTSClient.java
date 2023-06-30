package org.free.client.opentts;

import lombok.extern.slf4j.Slf4j;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Response;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Map;
import java.util.UUID;

/**
 * OpenTTS API client
 *
 * @author bayura-ea
 */
@Slf4j
public class OpenTTSClient {

    public enum VoiceQuality {
        high, medium, low
    }

    private final OpenTTSApi api;
    private final Map<String, String> voiceMap;
    private final VoiceQuality voiceQuality;

    protected OpenTTSClient(OpenTTSApi api, Map<String, String> voiceMap, VoiceQuality voiceQuality) {
        this.api = api;
        this.voiceMap = voiceMap;
        this.voiceQuality = voiceQuality;
    }


    public Path textToSpeechFile(String langIso2, String text) throws IOException {
        Call<ResponseBody> call = api.textToSpeech(voiceMap.get(langIso2),
                text,
                voiceQuality.name(),
                new BigDecimal("0.03"),
                false);

        Path resultPath = Files.createTempFile(UUID.randomUUID().toString(), ".wav");
        Response<ResponseBody> response = call.execute();
        if (response.isSuccessful()) {
            if (response.body() != null) {
                BufferedInputStream bis = new BufferedInputStream(response.body().byteStream());
                Files.copy(bis, resultPath, StandardCopyOption.REPLACE_EXISTING);
            }
        } else {
            log.error("Response not ok");
        }
        return resultPath;
    }


    public Map<String, OpenTTSVoice> voices(String ttsName, String language, String locale, Number gender) {
        return syncCall(api.voices(ttsName, language, locale, gender)).body();
    }

    protected <T> Response<T> syncCall(Call<T> call) {
        try {
            Response<T> response = call.execute();
            if (response.isSuccessful()) {
                return response;
            } else {
                throw new OpenTTSException("Unsuccessful response. Code is " + response.code());
            }
        } catch (OpenTTSException e) {
            throw e;
        } catch (Exception e) {
            throw new OpenTTSException(e);
        }
    }


}
