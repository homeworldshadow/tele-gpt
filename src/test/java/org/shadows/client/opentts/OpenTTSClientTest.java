package org.shadows.client.opentts;

import org.junit.Assert;
import org.junit.Test;

import java.io.IOException;
import java.net.URL;
import java.nio.file.Path;
import java.time.Duration;

/**
 * Fill the comment
 *
 * @author bayura-ea
 */
public class OpenTTSClientTest {


    @Test
    public void tts() throws IOException {
        OpenTTSClient client = OpenTTSClient.getInstance(new URL("http://localhost:5500"), Duration.ofSeconds(30));
        Path path = client.textToSpeechFile("en", "Hello");
        Assert.assertNotNull(path);
    }

}
