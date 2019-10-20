package net.gamescode.ewen.maestro;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.util.Log;
import android.widget.TextView;

import java.util.ArrayList;

import be.tarsos.dsp.AudioDispatcher;
import be.tarsos.dsp.AudioEvent;
import be.tarsos.dsp.io.android.AudioDispatcherFactory;
import be.tarsos.dsp.pitch.PitchDetectionHandler;
import be.tarsos.dsp.pitch.PitchDetectionResult;
import be.tarsos.dsp.pitch.PitchProcessor;

public class GameManager {
    private Resources resources;
    private Context Context;
    private Display display;
    final int[] note = new int[1];
    private ArrayList<Integer[]> notes = new ArrayList<>();
    private int timer = 0;
    AudioDispatcher dispatcher = AudioDispatcherFactory.fromDefaultMicrophone(22050, 1024, 0);
    Thread audioThread;

    public GameManager(Resources res, Context context) {
        note[0] = 5;
        resources = res;
        Context = context;
        display = new Display(res);

        if (dispatcher != null) {
            dispatcher.addAudioProcessor(new PitchProcessor(PitchProcessor.PitchEstimationAlgorithm.FFT_YIN, 22050, 1024, new PitchDetectionHandler() {

                @Override
                public void handlePitch(PitchDetectionResult pitchDetectionResult,
                                        AudioEvent audioEvent) {
                    final float pitchInHz = pitchDetectionResult.getPitch();
                    note[0] = Utils.frequencyToNoteN(pitchInHz);
                    new Runnable() {
                        @Override
                        public void run() {
                            int mnote = Utils.frequencyToNoteN(pitchInHz);
                            String noteS = Utils.noteNToNoteSfr(mnote);
                            note[0] = mnote;

                        }
                    };
                }
            }));
            audioThread = new Thread(dispatcher, "Audio Dispatcher");
            audioThread.start();
        }
    }

    public void update() {
        if (timer > 1000) notes.remove(0);
        timer = timer + 5;
        Integer[] newNote = new Integer[2];
        newNote[0] = timer;
        newNote[1] = note[0];
        Log.d( "GameManageUpdate", "Note played level = " + note[0]);
        notes.add(newNote);
    }

    public void draw(Canvas canvas) {
        display.resetDisplay(canvas);
        display.drawNotes(notes, canvas);
        display.drawUi(canvas);
    }


}
