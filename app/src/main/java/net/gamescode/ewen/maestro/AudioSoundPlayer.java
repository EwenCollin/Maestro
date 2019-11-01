/*
The AudioSoundPlayer is used to play notes generated in an ArrayList. It can play multiple notes at
the same time but it is also quite laggy.
It uses a threadMap to store the current playing threads and still play sounds while playing new ones.
It should be reworked to be less resource consuming but the main logic is there.
*/
package net.gamescode.ewen.maestro;


import android.content.Context;
import android.content.res.AssetFileDescriptor;
import android.content.res.AssetManager;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;
import android.media.MediaPlayer;
import android.util.Log;
import android.util.SparseArray;

import java.io.IOException;
import java.io.InputStream;

import static java.lang.System.currentTimeMillis;

/**
 * Created by ssaurel on 15/03/2018.
 */
public class AudioSoundPlayer {

    private SparseArray<PlayThread> threadMap = null;
    private Context context;
    private static final SparseArray<String> SOUND_MAP = new SparseArray<>();
    public static final int MAX_VOLUME = 100, CURRENT_VOLUME = 0;

    static {
        // white keys sounds
        SOUND_MAP.put(-36, "A1");
        SOUND_MAP.put(-35, "Bb1");
        SOUND_MAP.put(-34, "B1");
        SOUND_MAP.put(-33, "C1");
        SOUND_MAP.put(-32, "Db1");
        SOUND_MAP.put(-31, "D1");
        SOUND_MAP.put(-30, "Eb1");
        SOUND_MAP.put(-29, "E1");
        SOUND_MAP.put(-28, "F1");
        SOUND_MAP.put(-27, "Gb1");
        SOUND_MAP.put(-26, "G1");
        SOUND_MAP.put(-25, "Ab2");
        SOUND_MAP.put(-24, "A2");
        SOUND_MAP.put(-23, "Bb2");
        SOUND_MAP.put(-22, "B2");
        SOUND_MAP.put(-21, "C2");
        SOUND_MAP.put(-20, "Db2");
        SOUND_MAP.put(-19, "D2");
        SOUND_MAP.put(-18, "Eb2");
        SOUND_MAP.put(-17, "E2");
        SOUND_MAP.put(-16, "F2");
        SOUND_MAP.put(-15, "Gb2");
        SOUND_MAP.put(-14, "G2");
        SOUND_MAP.put(-13, "Ab3");
        SOUND_MAP.put(-12, "A3");
        SOUND_MAP.put(-11, "Bb3");
        SOUND_MAP.put(-10, "B3");
        SOUND_MAP.put(-9, "C3");
        SOUND_MAP.put(-8, "Db3");
        SOUND_MAP.put(-7, "D3");
        SOUND_MAP.put(-6, "Eb3");
        SOUND_MAP.put(-5, "E3");
        SOUND_MAP.put(-4, "F3");
        SOUND_MAP.put(-3, "Gb3");
        SOUND_MAP.put(-2, "G3");
        SOUND_MAP.put(-1, "Ab4");
        SOUND_MAP.put(0, "A4");
        SOUND_MAP.put(1, "Bb4");
        SOUND_MAP.put(2, "B4");
        SOUND_MAP.put(3, "C4");
        SOUND_MAP.put(4, "Db4");
        SOUND_MAP.put(5, "D4");
        SOUND_MAP.put(6, "Eb4");
        SOUND_MAP.put(7, "E4");
        SOUND_MAP.put(8, "F4");
        SOUND_MAP.put(9, "Gb4");
        SOUND_MAP.put(10, "G4");
        SOUND_MAP.put(11, "Ab5");
        SOUND_MAP.put(12, "A5");
        SOUND_MAP.put(13, "Bb5");
        SOUND_MAP.put(14, "B5");
        SOUND_MAP.put(15, "C5");
        SOUND_MAP.put(16, "Db5");
        SOUND_MAP.put(17, "D5");
        SOUND_MAP.put(18, "Eb5");
        SOUND_MAP.put(19, "E5");
        SOUND_MAP.put(20, "F5");
        SOUND_MAP.put(21, "Gb5");
        SOUND_MAP.put(22, "G5");
        SOUND_MAP.put(23, "Ab6");
        SOUND_MAP.put(24, "A6");
        SOUND_MAP.put(25, "Bb6");
        SOUND_MAP.put(26, "B6");
        SOUND_MAP.put(27, "C6");
        SOUND_MAP.put(28, "Db6");
        SOUND_MAP.put(29, "D6");
        SOUND_MAP.put(30, "Eb6");
        SOUND_MAP.put(31, "E6");
        SOUND_MAP.put(32, "F6");
        SOUND_MAP.put(33, "Gb6");
        SOUND_MAP.put(34, "G6");
        SOUND_MAP.put(35, "Ab7");
    }

    public AudioSoundPlayer(Context context) {
        this.context = context;
        threadMap = new SparseArray<>();
    }

    public void playNote(int note) {
        if (!isNotePlaying(note) && note > -37 && note < 36) {
            PlayThread thread = new PlayThread(note);
            thread.start();
            threadMap.put(note, thread);
        }
    }

    public void stopNote(int note) {
        if(note > -37 && note < 36) {
            PlayThread thread = threadMap.get(note);

            if (thread != null) {
                threadMap.remove(note);
            }
        }

    }

    /*public void playNote(int note) {
        PlayThread thread = threadMap.get(note);
        if (thread != null && note > -37 && note < 36) {
            thread.stopPlayer();
            thread.startPlayer();
        }
        else if (note > -37 && note < 36) {
            thread = new PlayThread(note);
            thread.start();
            thread.startPlayer();
            threadMap.put(note, thread);
        }
    }

    public void stopNote(int note) {
        if (note > -37 && note < 36) {
            PlayThread thread = threadMap.get(note);
            if (thread != null) {
                thread.stopPlayer();
            }
        }
    }*/

    public boolean isNotePlaying(int note) {
        return threadMap.get(note) != null;
    }
    /*
    private class PlayThread extends Thread {
        int note;
        MediaPlayer player = null;
        String path;
        AssetFileDescriptor afd;
        public PlayThread(int note) {
            this.note = note;
            String path = "piano/Piano.pp." + SOUND_MAP.get(note) + ".wav";
            try {
                AssetFileDescriptor afd = context.getAssets().openFd(path);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void run() {
            try{
                //player.release();
                player = new MediaPlayer();
                player.setDataSource(afd.getFileDescriptor(),afd.getStartOffset(),afd.getLength());
            }
            catch (Exception e) {
                e.printStackTrace();
            }
        }

        public void startPlayer(){
            if(player != null) {
                try {
                    player.setDataSource(afd.getFileDescriptor(),afd.getStartOffset(),afd.getLength());
                    player.prepare();
                    player.start();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        public void stopPlayer() {
            if(player != null) {
                player.stop();
            }
        }
    }*/


    private class PlayThread extends Thread {
        int note;
        MediaPlayer player;

        public PlayThread(int note) {
            this.note = note;
        }

        @Override
        public void run() {
            try {
                String path = "piano/Piano.pp." + SOUND_MAP.get(note) + ".wav";
                AssetManager assetManager = context.getAssets();
                AssetFileDescriptor ad = assetManager.openFd(path);


                player = new MediaPlayer();
                player.setDataSource(ad.getFileDescriptor(),ad.getStartOffset(),ad.getLength());
                player.prepare();
                player.start();

                long previousTime = System.currentTimeMillis();
                int x = 0;
                Log.d("AudioSoundPlayer", "Player duration : " + player.getDuration());
                while(System.currentTimeMillis() - previousTime < player.getDuration()) x++;
                player.stop();
                player.release();

            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                if (player != null) {
                    //player.release();
                }
            }
        }
    }

}
