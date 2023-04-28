import com.github.pemistahl.lingua.api.Language;
import com.github.pemistahl.lingua.api.LanguageDetector;
import com.github.pemistahl.lingua.api.LanguageDetectorBuilder;
import org.junit.Assert;
import org.junit.Test;

/**
 * Fill the comment
 *
 * @author bayura-ea
 */
public class DetectLangTest {


    @Test
    public void testLang() {
        LanguageDetector detector = LanguageDetectorBuilder.fromAllLanguages().build(); //fromLanguages(Language., Language.FRENCH, Language.GERMAN, Language.SPANISH).build();
        //Language detectedLanguage = detector.detectLanguageOf("languages are awesome");
        Language detectedLanguage = detector.detectLanguageOf(null);
        Assert.assertEquals("rus", detectedLanguage.getIsoCode639_3().toString());

    }
}
