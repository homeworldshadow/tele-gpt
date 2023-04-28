package org.shadows.converter;

import com.github.pemistahl.lingua.api.Language;
import com.github.pemistahl.lingua.api.LanguageDetector;
import com.github.pemistahl.lingua.api.LanguageDetectorBuilder;

import java.nio.file.Path;
import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * Fill the comment
 *
 * @author bayura-ea
 */
public class TextToSpeech {

    private final LanguageDetector detector = LanguageDetectorBuilder.fromAllLanguages().build();

    private Language detectLang(String text) {
        if (text == null) {
            return Language.ENGLISH;
        }
        return detector.detectLanguageOf(text);
    }

    public void tts(String text, Consumer<Path> fileCons)  {


    }
}
