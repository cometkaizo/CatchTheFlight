package com.cometkaizo.screen;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.UnsupportedAudioFileException;
import java.io.IOException;
import java.io.InputStream;

public class Sound {
    protected final AudioFormat format;
    protected final byte[] audio;
    public Sound(InputStream in) {
        try {
            var audioIn = AudioSystem.getAudioInputStream(in);
            this.format = audioIn.getFormat();
            this.audio = audioIn.readAllBytes();
        } catch (UnsupportedAudioFileException | IOException e) {
            throw new RuntimeException(e);
        }
    }
    public void play() {
        try{
            var clip = AudioSystem.getClip();
            clip.open(format, audio, 0, audio.length);
            clip.start();
        } catch (Exception e){
            e.printStackTrace();
        }
    }
}
