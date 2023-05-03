package org.shadows.converter;

import com.github.pemistahl.lingua.api.Language;
import com.github.pemistahl.lingua.api.LanguageDetector;
import com.github.pemistahl.lingua.api.LanguageDetectorBuilder;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.shadows.client.GptClient;
import org.shadows.client.opentts.OpenTTSClient;

import java.io.FileOutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.UUID;

/**
 * Fill the comment
 *
 * @author bayura-ea
 */
@Slf4j
public class TTSConverter {


    private OpenTTSClient openTTSClient;

    private final GptClient gptClient;
    private final LanguageDetector detector = LanguageDetectorBuilder.fromAllLanguages().build();

    private AudioConverter audioConverter;

    public TTSConverter(OpenTTSClient openTTSClient, GptClient gptClient) {
        this.openTTSClient = openTTSClient;
        this.gptClient = gptClient;
        this.audioConverter = new AudioConverter();
    }

    private Language detectLang(String text) {
        if (text == null) {
            return Language.ENGLISH;
        }
        return detector.detectLanguageOf(text);
    }

    @SneakyThrows
    public Path textToVoice(String text) {
        if (openTTSClient != null) {
            Path wavPath = null;
            try {
                wavPath = openTTSClient.textToSpeechFile(detectLang(text).getIsoCode639_1().toString(), text);
                return audioConverter.convertToMp3(wavPath);
            } finally {
                if (wavPath != null) {
                    Files.delete(wavPath);
                }
            }
        }
        return null;
    }


    @SneakyThrows
    public String voiceToText(String mimeType, byte[] content) {
        String result = null;
        if (content != null) {
            //  File file = bot.execute(new GetFile(voice.fileId())).file();
            Path tgFilePath = mimeType.contains("ogg")
                    ? Files.createTempFile(UUID.randomUUID().toString(), ".ogg")
                    : Files.createTempFile(UUID.randomUUID().toString(), ".bin");
            Path mp3FilePath = null;
            try (FileOutputStream fos = new FileOutputStream(tgFilePath.toFile())) {
                fos.write(content);
                mp3FilePath = audioConverter.convertToMp3(tgFilePath);
                result = gptClient.voiceToText(mp3FilePath);
            } finally {
                Files.delete(tgFilePath);
                if (mp3FilePath != null) {
                    Files.delete(mp3FilePath);
                }
            }
        }
        //  log.debug("Voice {} to text: {}", voice.fileId(), result);
        return result;
    }
}
