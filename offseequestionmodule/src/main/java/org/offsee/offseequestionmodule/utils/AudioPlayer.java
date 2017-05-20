package org.offsee.offseequestionmodule.utils;

import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.SoundPool;

import java.util.HashMap;

/**
 * Created by hamed on 3/9/2017.
 */

public class AudioPlayer {
    private static SoundPool soundPool;
    private static HashMap<Integer, Integer> maps = new HashMap<>();
    private static HashMap<Integer, AudioPlayer> auMap = new HashMap<>();
    int mySoundId;
    private boolean loaded = false;
    private boolean played = false;
    private boolean repeated = false;
    private int streamId = 0;

    MediaPlayer mediaPlayer = new MediaPlayer();

    public static AudioPlayer instance(int resourceRaw) {

        if (soundPool == null) {
            soundPool = new SoundPool(10, AudioManager.STREAM_MUSIC, 0);
            soundPool.setOnLoadCompleteListener(new SoundPool.OnLoadCompleteListener() {
                @Override
                public void onLoadComplete(SoundPool soundPool, final int sampleId, int status) {
                    auMap.get(sampleId).loadComplete();

                }
            });
        }
        Integer soundId = maps.get(resourceRaw);
        if (soundId == null) {
            soundId = soundPool.load(App.getContext(), resourceRaw, 1);
            maps.put(resourceRaw, soundId);
        }

        AudioPlayer audioPlayer = auMap.get(soundId);
        if (audioPlayer == null) {
            audioPlayer = new AudioPlayer(soundId);
            auMap.put(soundId, audioPlayer);
        }
        return audioPlayer;
    }

    AudioManager am;

    private AudioPlayer(int soundId) {
        mySoundId = soundId;
        am = (AudioManager) App.getContext().getSystemService(App.getContext().AUDIO_SERVICE);

    }

    public void stop() {
        soundPool.stop(streamId);
    }

    public void pause() {
        soundPool.pause(streamId);
    }

    public void play() {
        if (loaded) {
            //int volumeLevel = am.getStreamVolume(AudioManager.STREAM_MUSIC);
            //int maxLevel = am.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
            streamId = soundPool.play(mySoundId, ((float) volumeLevel) / maxLevel, ((float) volumeLevel) / maxLevel, 1, 0, 1f);
        } else {
            played = true;
        }
    }

    int volumeLevel = 1;
    int maxLevel = 1;

    public void repeate() {
        if (loaded) {
            //int volumeLevel = am.getStreamVolume(AudioManager.STREAM_MUSIC);
            //int maxLevel = am.getStreamMaxVolume(AudioManager.STREAM_MUSIC);

            streamId = soundPool.play(mySoundId, ((float) volumeLevel) / maxLevel, ((float) volumeLevel) / maxLevel, 1, -1, 1f);
        } else {
            repeated = true;
        }
    }

    public void repeateWithRate(float rate) {
        if (loaded) {
            //int volumeLevel = am.getStreamVolume(AudioManager.STREAM_MUSIC);
            //int maxLevel = am.getStreamMaxVolume(AudioManager.STREAM_MUSIC);

            streamId = soundPool.play(mySoundId, ((float) volumeLevel) / maxLevel, ((float) volumeLevel) / maxLevel, 1, -1, rate);
        } else {
            repeated = true;
        }
    }

    public void repeateWithSpeedUp(int durationImMillies) {
        if (loaded) {
            //int volumeLevel = am.getStreamVolume(AudioManager.STREAM_MUSIC);
            //int maxLevel = am.getStreamMaxVolume(AudioManager.STREAM_MUSIC);

            streamId = soundPool.play(mySoundId, ((float) volumeLevel) / maxLevel, ((float) volumeLevel) / maxLevel, 1, -1, 1f);
        } else {
            repeated = true;
        }
    }

    public void loadComplete() {
        loaded = true;
        if (played) {
            play();
        }
        if (repeated) {
            repeate();
        }
    }

}
