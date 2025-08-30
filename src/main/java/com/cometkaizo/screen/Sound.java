package com.cometkaizo.screen;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.UnsupportedAudioFileException;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashSet;
import java.util.Set;

public class Sound {
    private static final Set<Sound> PLAYED_THIS_TICK = new HashSet<>();
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
        if (!PLAYED_THIS_TICK.add(this)) return;
        try {
            var clip = AudioSystem.getClip();
            clip.open(format, audio, 0, audio.length);
            clip.start();
        } catch (Exception e){
            e.printStackTrace();
        }
    }
    public static void tick() {
        PLAYED_THIS_TICK.clear();
    }
}
