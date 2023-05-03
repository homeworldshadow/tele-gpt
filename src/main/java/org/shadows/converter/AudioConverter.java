package org.shadows.converter;

import ws.schild.jave.Encoder;
import ws.schild.jave.EncoderException;
import ws.schild.jave.MultimediaObject;
import ws.schild.jave.encode.AudioAttributes;
import ws.schild.jave.encode.EncodingAttributes;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.UUID;

/**
 * Fill the comment
 *
 * @author bayura-ea
 */
public class AudioConverter {

    private final Encoder encoder;

    private final EncodingAttributes encodingAttributes;

    public AudioConverter() {
        this.encoder = new Encoder();
        AudioAttributes audio = new AudioAttributes();
        audio.setCodec("libmp3lame");
        audio.setBitRate(64000);
        audio.setChannels(1);
        audio.setSamplingRate(48000);
        //Encoding attributes
        EncodingAttributes attrs = new EncodingAttributes();
        attrs.setOutputFormat("mp3");
        attrs.setAudioAttributes(audio);
        this.encodingAttributes = attrs;
    }


    public Path convertToMp3(Path source) throws IOException, EncoderException {
        Path tmp = Files.createTempFile(UUID.randomUUID().toString(), ".mp3");
        encoder.encode(new MultimediaObject(source.toFile()), tmp.toFile(), encodingAttributes);
        return tmp;
    }

}
