package com.merg.diceroller.sound;

import android.media.AudioAttributes;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;

public final class SoundEffectPlayer {
    private static final int SAMPLE_RATE = 22050;

    public void playLift() { playTone(180, 55, 0.09f); }
    public void playLand() { playTone(95, 95, 0.18f); }

    private void playTone(int hz, int ms, float volume) {
        int samples = SAMPLE_RATE * ms / 1000;
        short[] data = new short[samples];
        for (int i = 0; i < samples; i++) {
            double env = 1.0 - (i / (double) samples);
            double noise = Math.sin(2.0 * Math.PI * i * hz / SAMPLE_RATE) + Math.sin(2.0 * Math.PI * i * hz * 1.7 / SAMPLE_RATE) * 0.45;
            data[i] = (short) (noise * env * volume * Short.MAX_VALUE);
        }
        AudioTrack track = new AudioTrack.Builder()
                .setAudioAttributes(new AudioAttributes.Builder().setUsage(AudioAttributes.USAGE_GAME).setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION).build())
                .setAudioFormat(new AudioFormat.Builder().setSampleRate(SAMPLE_RATE).setEncoding(AudioFormat.ENCODING_PCM_16BIT).setChannelMask(AudioFormat.CHANNEL_OUT_MONO).build())
                .setBufferSizeInBytes(data.length * 2)
                .setTransferMode(AudioTrack.MODE_STATIC)
                .build();
        track.write(data, 0, data.length);
        track.setNotificationMarkerPosition(data.length);
        track.setPlaybackPositionUpdateListener(new AudioTrack.OnPlaybackPositionUpdateListener() {
            @Override public void onMarkerReached(AudioTrack audioTrack) { audioTrack.release(); }
            @Override public void onPeriodicNotification(AudioTrack audioTrack) {}
        });
        track.play();
    }
}
