package net.gamescode.ewen.maestro;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Color;
import android.net.Uri;
import android.provider.ContactsContract;
import android.util.Log;
import android.widget.TextView;

import java.lang.reflect.Type;
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
    private ArrayList<Integer[]> NotesToPlay = new ArrayList<>();
    private int timer = 0;

    private AudioDispatcher dispatcher = AudioDispatcherFactory.fromDefaultMicrophone(22050, 1024, 0);
    private Thread audioThread;
    private MultiplePitchHandler multiplePitchHandler = new MultiplePitchHandler();
    private int[] multipleFrequencies;

    private String TypeContext = "";
    private String TypeAction = "";
    private String TypeAlgo = "";

    private Metronom metronom = new Metronom();
    private ArrayList<Integer> lastNotes = new ArrayList<>();

    private AudioSoundPlayer audioSoundPlayer;


    public GameManager(Resources res, Context context, String typeContext, String typeAction, String typeAlgo, String fileName) {
        Uri fileUri = null;
        if (typeContext.equals("smartMetronom") && fileName != null) {
            fileUri = Uri.parse(fileName);
            NotesToPlay = Utils.getNotesToPlayFromMIDI(fileUri, context);
        }
        if(typeAction != null && typeAction.equals("PlayRecord")) {
            NotesToPlay = Utils.getNotesToPlayFromGeneratedMIDI(context);
        }
        audioSoundPlayer = new AudioSoundPlayer(context);
        TypeContext = typeContext;
        TypeAction = typeAction;
        TypeAlgo = typeAlgo;
        note[0] = 5;
        resources = res;
        Context = context;
        display = new Display(res);

        initializeDispatcher();


    }

    public void update() {
        timer = timer + 5;
        metronom.update(timer);
        display.updateOffset();

        Integer[] newNote = new Integer[3];
        newNote[0] = timer;
        newNote[1] = note[0];
        newNote[2] = 5;
        if(TypeAction.equals("AutoPlay") || TypeAction.equals("PlayRecord")) notes = metronom.noteListGenerator(NotesToPlay);
        else if(TypeAlgo.equals("MPH")) multipleFrequenciesToNote();
        else if(TypeAlgo.equals("FFT_YIN") || TypeAlgo.equals("DYNAMIC_WAVELET")) notes.add(newNote);
        if(notes.size() > 0) if (notes.get(0)[0] > timer + 2000) notes.remove(0);
        if((TypeAction.equals("AutoPlay") || TypeAction.equals("PlayRecord")) && TypeContext.equals("smartMetronom")) {
            ArrayList<Integer> notesToStop = metronom.getDifferentNotes(lastNotes, metronom.getNotesListToPlay(notes));
            for(int i = 0; i < notesToStop.size(); i++) {
                audioSoundPlayer.stopNote(notesToStop.get(i));
            }
            if(!lastNotes.equals(metronom.getNotesListToPlay(notes))) {
                lastNotes = metronom.getNotesListToPlay(notes);
                for(int i = 0; i < lastNotes.size(); i++) {
                    audioSoundPlayer.playNote(lastNotes.get(i));
                }
            }
        }

        if(TypeContext.equals("smartMetronom")) {
            metronom.metronom(notes, NotesToPlay);
        }
    }

    public void draw(Canvas canvas) {

        if(TypeAction.equals("Spectrogram")) {
            display.drawAmplitudes(multiplePitchHandler.getAmplitudes(), canvas, 0);
            display.drawAmplitudes(multiplePitchHandler.getBinAmplitudes(), canvas, 1);
        }
        else if(TypeAction.equals("DrawFFT")) {
            display.resetDisplay(canvas);
            display.drawFFT(multiplePitchHandler.getAmplitudes(), multiplePitchHandler.getRelevantValues(), Color.rgb(255, 0, 0), canvas, true);
            multiplePitchHandler.convertSpectreToFreqAndIntens(multiplePitchHandler.getRelevantValues());
        }
        else if(TypeContext.equals("basicDetection")){
            display.resetDisplay(canvas);
            display.drawNotes(notes, canvas, Color.rgb(0, 255, 255), 0);
            display.drawUi(canvas);
        }
        else if(TypeContext.equals("smartMetronom")) {
            display.resetDisplay(canvas);
            display.drawNotes(metronom.getNotesToPlay(), canvas, Color.rgb(0, 90, 255), (float)0.1);
            display.drawNotes(notes, canvas, Color.rgb(0, 255, 255), 0);
            display.drawNotes(metronom.getNotesSuccessful(notes), canvas, Color.rgb(0, 230, 0), 0);
            display.drawUi(canvas);
        }
        else {
            display.resetDisplay(canvas);
            display.drawNotes(notes, canvas, Color.rgb(0, 255, 255), 0);
            display.drawUi(canvas);
        }

    }

    public void destroy(){
        if(TypeAction.equals("Record")) {
            Utils.writeMIDIFromNotesPlayed(metronom.getLastNotes(notes, notes.size()), Context);
        }
        try {
            dispatcher.stop();
            audioThread.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public void multipleFrequenciesToNote() {
        multipleFrequencies = multiplePitchHandler.getFrequenciesFromSpectre();
        float totalIntensity = multiplePitchHandler.getTotalIntensity();
        if(totalIntensity > 4) {
            for (int i = 0; i < multipleFrequencies.length; i++) {
                Integer[] note = new Integer[3];
                note[0] = timer;
                note[1] = Utils.frequencyToNoteN(multipleFrequencies[i], 12);
                note[2] = 5;
                notes.add(note);
            }
        }
    }

    public void initializeDispatcher() {
        if(TypeAlgo.equals("FFT_YIN")) {
            if (dispatcher != null) {
                dispatcher.addAudioProcessor(new PitchProcessor(PitchProcessor.PitchEstimationAlgorithm.FFT_YIN, 22050, 1024, new PitchDetectionHandler() {

                    @Override
                    public void handlePitch(PitchDetectionResult pitchDetectionResult,
                                            AudioEvent audioEvent) {
                        final float pitchInHz = pitchDetectionResult.getPitch();
                        note[0] = Utils.frequencyToNoteN(pitchInHz, 0);
                        new Runnable() {
                            @Override
                            public void run() {
                                int mnote = Utils.frequencyToNoteN(pitchInHz, 0);
                                String noteS = Utils.noteNToNoteSfr(mnote);
                                note[0] = mnote;

                            }
                        };
                    }
                }));
            }
        }
        else if(TypeAlgo.equals("DYNAMIC_WAVELET")) {
            if (dispatcher != null) {
                dispatcher.addAudioProcessor(new PitchProcessor(PitchProcessor.PitchEstimationAlgorithm.DYNAMIC_WAVELET, 22050, 1024, new PitchDetectionHandler() {

                    @Override
                    public void handlePitch(PitchDetectionResult pitchDetectionResult,
                                            AudioEvent audioEvent) {
                        final float pitchInHz = pitchDetectionResult.getPitch();
                        note[0] = Utils.frequencyToNoteN(pitchInHz, 0);
                        new Runnable() {
                            @Override
                            public void run() {
                                int mnote = Utils.frequencyToNoteN(pitchInHz, 0);
                                String noteS = Utils.noteNToNoteSfr(mnote);
                                note[0] = mnote;

                            }
                        };
                    }
                }));
            }
        }
        else if(TypeAlgo.equals("MPH")) {
            if(dispatcher != null) {
                dispatcher.addAudioProcessor(multiplePitchHandler.fftProcessor);
            }
        }
        else {
            if (dispatcher != null) {
                dispatcher.addAudioProcessor(new PitchProcessor(PitchProcessor.PitchEstimationAlgorithm.FFT_YIN, 22050, 1024, new PitchDetectionHandler() {

                    @Override
                    public void handlePitch(PitchDetectionResult pitchDetectionResult,
                                            AudioEvent audioEvent) {
                        final float pitchInHz = pitchDetectionResult.getPitch();
                        note[0] = Utils.frequencyToNoteN(pitchInHz, 0);
                        new Runnable() {
                            @Override
                            public void run() {
                                int mnote = Utils.frequencyToNoteN(pitchInHz, 0);
                                String noteS = Utils.noteNToNoteSfr(mnote);
                                note[0] = mnote;

                            }
                        };
                    }
                }));
            }

        }
        if(dispatcher != null) {
            audioThread = new Thread(dispatcher, "Audio Dispatcher");
            audioThread.start();

        }
    }
}
