import com.sun.speech.freetts.FreeTTS;
import com.sun.speech.freetts.Voice;
import com.sun.speech.freetts.VoiceManager;
import com.sun.speech.freetts.audio.AudioPlayer;
import com.sun.speech.freetts.audio.SingleFileAudioPlayer;
import com.sun.speech.freetts.en.us.cmu_time_awb.AlanVoiceDirectory;
import org.junit.Test;

import javax.sound.sampled.AudioFileFormat;
import javax.speech.AudioException;
import javax.speech.Central;
import javax.speech.EngineException;
import javax.speech.EngineList;
import javax.speech.synthesis.Synthesizer;
import javax.speech.synthesis.SynthesizerModeDesc;
import java.beans.PropertyVetoException;
import java.util.Locale;

/**
 * Fill the comment
 *
 * @author bayura-ea
 */
public class VoiceSynthTest {


    @Test
    public void test() throws EngineException, AudioException, PropertyVetoException, InterruptedException {
        System.setProperty("freetts.voices", "com.sun.speech.freetts.en.us.cmu_us_kal.KevinVoiceDirectory");
//        System.setProperty("freetts.voices", "com.sun.speech.freetts.en.us.cmu_time_awb.AlanVoiceDirectory");
        //com.sun.speech.freetts.Voice  voice1 = VoiceManager.getInstance().getVoice("kevin16");

        AudioPlayer audioPlayer = null;
        VoiceManager voiceManager = VoiceManager.getInstance();
        Voice helloVoice = voiceManager.getVoice("kevin");
        helloVoice.allocate();

        /* Synthesize speech.
         */
//create a audioplayer to dump the output file
        audioPlayer = new SingleFileAudioPlayer("/tmp/1", AudioFileFormat.Type.WAVE);
        //attach the audioplayer
        helloVoice.setAudioPlayer(audioPlayer);



        helloVoice.speak("Thank you for giving me a voice. "
                + "I'm so glad to say hello to this world.");



        /* Clean up and leave.
         */
        helloVoice.deallocate();
//don't forget to close the audioplayer otherwise file will not be saved
        audioPlayer.close();

       /* Central.registerEngineCentral("com.sun.speech.freetts.jsapi.FreeTTSEngineCentral");

        SynthesizerModeDesc desc = new SynthesizerModeDesc(
                null,      // engine name
                "general",  // mode name
                Locale.US, // locale
                null,      // running
                null);     // voices

        EngineList engineList = Central.availableSynthesizers(desc);
        for (int i = 0; i < engineList.size(); i++) {

            desc = (SynthesizerModeDesc) engineList.get(i);
            System.out.println("    " + desc.getEngineName()
                    + " (mode=" + desc.getModeName()
                    + ", locale=" + desc.getLocale() + "):");
            Voice[] voices = desc.getVoices();
            for (int j = 0; j < voices.length; j++) {
                System.out.println("        " + voices[j].getName());
            }
        }

        desc = new SynthesizerModeDesc(
                null,      // engine name
                "general",  // mode name
                Locale.US, // locale
                null,      // running
                null);     // voices

        Synthesizer synthesizer = Central.createSynthesizer(desc);
        synthesizer.allocate();
        synthesizer.resume();
        desc = (SynthesizerModeDesc) synthesizer.getEngineModeDesc();

        String voiceName = "kevin16";
        Voice[] voices = desc.getVoices();
        Voice voice = null;
        for (int i = 0; i < voices.length; i++) {
            if (voices[i].getName().equals(voiceName)) {
                voice = voices[i];
                break;
            }
        }

        synthesizer.getSynthesizerProperties().setVoice(voice);
        synthesizer.speakPlainText("Hello world!", null);
        synthesizer.waitEngineState(Synthesizer.QUEUE_EMPTY);
        synthesizer.deallocate();

        */


    }
}
