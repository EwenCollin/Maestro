package net.gamescode.ewen.maestro;

import android.content.ContentResolver;
import android.content.Context;
import android.media.AudioFormat;
import android.media.AudioTrack;
import android.net.Uri;
import android.os.Environment;
import android.util.Log;


import com.levien.synthesizer.core.midi.MidiEvent;
import com.levien.synthesizer.core.midi.MidiFile;
import com.levien.synthesizer.core.midi.MidiHeader;
import com.levien.synthesizer.core.midi.MidiTrack;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.Normalizer;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import be.tarsos.dsp.AudioEvent;
import be.tarsos.dsp.AudioProcessor;
import be.tarsos.dsp.GainProcessor;
import be.tarsos.dsp.WaveformSimilarityBasedOverlapAdd;
import be.tarsos.dsp.resample.RateTransposer;

public class Utils {

    public static final byte[] IDENTIFIER = { 'M', 'T', 'h', 'd' };
    public static final byte[] IDENTIFIER_TRACK = { 'M', 'T', 'r', 'k' };
    public static final byte[] IDENTIFIER_TRACK_END = { 'T', 'r', 'k' };
    public static final byte[] IDENTIFIER_END = { 'M', 'T', 'r', 'k', 'E', 'n', 'd' };

    //Gets the 10 max values of a float[]
    public static int[] getMaximumValuesIndexes(float[] input, int numberOfValues) {
        int[] indexes = new int[numberOfValues];
        float[] values = new float[numberOfValues];
        boolean isIndexAlreadyUsed = false;
        for(int i = 0; i < indexes.length; i++) {
            indexes[i] = 0;
            values[i] = 0;
        }
        for(int x = 0; x < numberOfValues; x++) {
            for(int i = 0; i < input.length; i++) {
                isIndexAlreadyUsed = false;
                for(int j = 0; j < numberOfValues; j++) {
                    if(indexes[j] == i) {
                        isIndexAlreadyUsed = true;
                    }
                }
                if(values[x] < input[i] && !isIndexAlreadyUsed) {
                    values[x] = input[i];
                    indexes[x] = i;
                }
            }
        }
        return indexes;
    }

    public static void writeMIDIFromNotesPlayed(ArrayList<Integer[]> notes,Context context) {
        Utils utils = new Utils();
        MidiFile midi = new MidiFile();
        midi.addTrack();

        long deltaTime = 0;
        int previousTick = 0;
        int longestDelta = 0;
        for(int i = 0; i < notes.size(); i++) {
            if(longestDelta < notes.get(i)[0] - previousTick) longestDelta = notes.get(i)[0] - previousTick;
            previousTick = notes.get(i)[0];
        }
        previousTick = 0;
        for(int i = 0; i < notes.size(); i++) {
            deltaTime = notes.get(i)[0] - previousTick;
            previousTick = notes.get(i)[0];
            byte[] messageA = new byte[4];
            byte[] messageB = new byte[4];
            messageA[0] = (byte) 0x90;
            messageA[1] = (byte) (int) (notes.get(i)[1] + 69);
            messageA[2] = (byte) 0x40;
            messageA[3] = (byte) 0x00;
            MidiEvent eventA = new MidiEvent(deltaTime, messageA);
            midi.getTrack(0).addEvent(eventA);
            messageB[0] = (byte) 0x80;
            messageB[1] = (byte) (int) (notes.get(i)[1] + 69);
            messageB[2] = (byte) 0x00;
            messageB[3] = (byte) (127*deltaTime/longestDelta);
            MidiEvent eventB = new MidiEvent(deltaTime, messageB);

            midi.getTrack(0).addEvent(eventB);
        }
        try {
            utils.writeToFile(new File(context.getExternalFilesDir(Environment.DIRECTORY_MUSIC), "save.mid"), 1, 1, 480, midi.getTrack(0));
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public void writeToFile(File outFile, int mType, int mTrackCount, int mResolution, MidiTrack mTrack) throws FileNotFoundException, IOException
    {
        FileOutputStream fout = new FileOutputStream(outFile);

        fout.write(IDENTIFIER);
        fout.write(intToBytes(6, 4));
        fout.write(intToBytes(mType, 2));
        fout.write(intToBytes(mTrackCount, 2));
        fout.write(intToBytes(mResolution, 2));
        writeTracksToFile(mTrack, fout);

        fout.flush();
        fout.close();
    }

    public void writeTracksToFile(MidiTrack track, FileOutputStream fout) {
        int mTrackSize = 0;
        for (int i = 0; i < track.getEventCount(); i++) {
            mTrackSize += track.getEvent(i).getMessage().length;
        }
        try {
            fout.write(IDENTIFIER_TRACK);
            fout.write(intToBytes(mTrackSize, 4));
            fout.write(intToBytes(0, 1));
        } catch (IOException e) {
            e.printStackTrace();
        }
        for(int i = track.getEventCount() - 1; i >= 0; i--) {
            if(i == 0) writeEventsToFile(track.getEvent(i), fout, true);
            else writeEventsToFile(track.getEvent(i), fout, false);
        }

    }

    public void writeEventsToFile(MidiEvent event, FileOutputStream fout, boolean last) {
        try {
            if(last) {
                fout.write(0x90);
                fout.write(0x90);
                fout.write(0x90);
                fout.write(0xFF);
            }
            else fout.write(event.getMessage());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static byte[] intToBytes(int val, int byteCount)
    {
        byte[] buffer = new byte[byteCount];

        int[] ints = new int[byteCount];

        for(int i = 0; i < byteCount; i++)
        {
            ints[i] = val & 0xFF;
            buffer[byteCount - i - 1] = (byte) ints[i];

            val = val >> 8;

            if(val == 0)
            {
                break;
            }
        }

        return buffer;
    }

    //Passes notes obtained from a midi file to an ArrayList of Integers[3], there is stored the tick, the note and the note duration.
    public static ArrayList<Integer[]> getNotesToPlayFromMIDI(Uri uri, Context context) {

        ArrayList<Integer[]> notesRead = new ArrayList<>();

        ContentResolver contentResolver = context.getContentResolver();

        InputStream input = null;
        try {
            input = contentResolver.openInputStream(uri);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        MidiFile midi = null;
        if(input != null) {
            try {
                midi = new MidiFile(input);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        if(midi != null) {
            int tick = 0;
            MidiTrack mainTrack = midi.getTrack(1);
            //MidiTrack secondTrack = null;
            //if(midi.getTrackCount() == 3) secondTrack = midi.getTrack(2);
            for(int i = 0; i < mainTrack.getEventCount(); i++) {
                MidiEvent event = mainTrack.getEvent(i);
                int note = event.getMessage()[1];
                tick = (int) (tick + event.getDeltaTime());
                if(event.getMessage().length == 3)Log.d("Utils", "DeltaTime is : " + event.getDeltaTime() + " event type is " +  event.getMessage()[0] + " event note is " + event.getMessage()[1] + " event time is " + event.getMessage()[2]);
                if(event.getMessage().length == 2)Log.d("Utils", "DeltaTime is : " + event.getDeltaTime() + " event type is " + event.getMessage()[0] + " event note is " + event.getMessage()[1]);

                if(event.getMessage()[0] == -112 || event.getMessage()[0] == -111) {
                    Integer[] newNote = new Integer[3];
                    newNote[0] = tick/5;
                    newNote[1] = note - 69;
                    newNote[2] = Integer.valueOf((int) 100);
                    for(int x = 0; x < mainTrack.getEventCount() - i; x++) {
                        if(mainTrack.getEvent(i + x).getMessage()[1] == note && (mainTrack.getEvent(i + x).getMessage()[0] == -128 || mainTrack.getEvent(i + x).getMessage()[0] == -127)) {
                            if(mainTrack.getEvent(i + x).getDeltaTime() > 0) {
                                newNote[2] = (int) mainTrack.getEvent(i + x).getDeltaTime();
                            }
                            else newNote[2] = Integer.valueOf(event.getMessage()[2]);
                        }
                    }
                    notesRead.add(newNote);
                }/*
                if(event.getMessage()[0] == -128) {
                    for(int x = 0; x < notesRead.size(); x++) {
                        if(notesRead.size() - x - 1 >= 0 && notesRead.get(notesRead.size() - x - 1)[1] == event.getMessage()[1] - 69) {
                            Integer[] changedNote = notesRead.get(notesRead.size() - x - 1).clone();
                            changedNote[2] = (int)(event.getDeltaTime());
                            notesRead.set(notesRead.size() - x - 1, changedNote);
                            x = notesRead.size();
                        }
                    }
                }*/
                //Log.d("UtilsReadMidi", "Event message : " + note);

            }
        }
        return notesRead;
    }

    public static ArrayList<Integer[]> getNotesToPlayFromGeneratedMIDI(Context context) {

        ArrayList<Integer[]> notesRead = new ArrayList<>();

        File filesDir = context.getExternalFilesDir(Environment.DIRECTORY_MUSIC);
        File input = new File(filesDir, "save.mid");
        MidiFile midi = null;
        if(input != null) {
            try {
                midi = new MidiFile(new FileInputStream(input));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        if(midi != null) {
            int tick = 0;
            MidiTrack mainTrack;
            if(midi.getTrackCount() >= 2)mainTrack = midi.getTrack(1);
            else mainTrack = midi.getTrack(0);

            //MidiTrack secondTrack = null;
            //if(midi.getTrackCount() == 3) secondTrack = midi.getTrack(2);
            for(int i = 0; i < mainTrack.getEventCount(); i++) {
                MidiEvent event = mainTrack.getEvent(i);
                int note = event.getMessage()[1];
                tick = (int) (tick + event.getDeltaTime());
                if(event.getMessage().length == 3)Log.d("Utils", "DeltaTime is : " + event.getDeltaTime() + " event type is " +  event.getMessage()[0] + " event note is " + event.getMessage()[1] + " event time is " + event.getMessage()[2]);
                if(event.getMessage().length == 2)Log.d("Utils", "DeltaTime is : " + event.getDeltaTime() + " event type is " + event.getMessage()[0] + " event note is " + event.getMessage()[1]);

                if(event.getMessage()[0] == -112 || event.getMessage()[0] == -111) {
                    Integer[] newNote = new Integer[3];
                    newNote[0] = tick*4;
                    newNote[1] = note - 69;
                    newNote[2] = Integer.valueOf((int) 100);
                    for(int x = 0; x < mainTrack.getEventCount() - i; x++) {
                        if(mainTrack.getEvent(i + x).getMessage()[1] == note && (mainTrack.getEvent(i + x).getMessage()[0] == -128 || mainTrack.getEvent(i + x).getMessage()[0] == -127)) {
                            if(mainTrack.getEvent(i + x).getDeltaTime() > 0) {
                                newNote[2] = (int) mainTrack.getEvent(i + x).getDeltaTime();
                            }
                            else newNote[2] = Integer.valueOf(event.getMessage()[2]);
                        }
                    }
                    notesRead.add(newNote);
                }
            }
        }
        return notesRead;
    }

    //Converts a frenquency to an int that represents the number of the note, like in a Midi file but it hasn't the same offset.
    //You can do (+69) to notes generated by this function to obtain the code corresponding in a midi file.
    public static int frequencyToNoteN(float frequency, int offset) {
        if (frequency == -1) return -50;
        int result = (int) Math.round(((Math.log(frequency/440))/Math.log(2))*12) + offset;
        return result;
    }

    public static String noteNToNoteSfr(int note) {
        if( note < -24) {
            return "Basse";
        }
        if (note == -24) {
            return "La 1";
        }
        if (note == -23) {
            return "La# 1 / Sib 1";
        }
        if (note == -22) {
            return "Si 1";
        }
        if (note == -21) {
            return "Do 2";
        }
        if (note == -20) {
            return "Do# 2 / Réb 2";
        }
        if (note == -19) {
            return "Ré 2";
        }
        if (note == -18) {
            return "Ré# 2 / Mib 2";
        }
        if (note == -17) {
            return "Mi 2";
        }
        if (note == -16) {
            return "Fa 2";
        }
        if (note == -15) {
            return "Fa# 2 / Solb 2";
        }
        if (note == -14) {
            return "Sol 2";
        }
        if (note == -13) {
            return "Sol# 2 / Lab 2";
        }
        if (note == -12) {
            return "La 2";
        }
        if (note == -11) {
            return "La# 2 / Sib 2";
        }
        if (note == -10) {
            return "Si 2";
        }
        if (note == -9) {
            return "Do 3";
        }
        if (note == -8) {
            return "Do# 3 / Réb 3";
        }
        if (note == -7) {
            return "Ré 3";
        }
        if (note == -6) {
            return "Ré# 3 / Mib 3";
        }
        if (note == -5) {
            return "Mi 3";
        }
        if (note == -4) {
            return "Fa 3";
        }
        if (note == -3) {
            return "Fa# 3 / Solb 3";
        }
        if (note == -2) {
            return "Sol 3";
        }
        if (note == -1) {
            return "Sol# 3 / Lab 3";
        }
        if (note == 0) {
            return "La 3";
        }
        if (note == 1) {
            return "La# 3 / Sib 3";
        }
        if (note == 2) {
            return "Si 3";
        }
        if (note == 3) {
            return "Do 4";
        }
        if (note == 4) {
            return "Do# 4 / Réb 4";
        }
        if (note == 5) {
            return "Ré 4";
        }
        if (note == 6) {
            return "Ré# 4 / Mib 4";
        }
        if (note == 7) {
            return "Mi 4";
        }
        if (note == 8) {
            return "Fa 4";
        }
        if (note == 9) {
                return "Fa# 4 / Solb 4";
        }
        if (note == 10) {
            return "Sol 4";
        }
        if (note == 11) {
            return "Sol# 4 / Lab 4";
        }
        if (note == 12) {
            return "La 4";
        }
        if (note == 13) {
            return "La# 4 / Sib 4";
        }
        if (note == 14) {
            return "Si 4";
        }
        if (note == 15) {
            return "Do 5";
        }
        if (note == 16) {
            return "Do# 5 / Réb 5";
        }
        if (note == 17) {
            return "Ré 5";
        }
        if (note == 18) {
            return "Ré# 5 / Mib 5";
        }
        if (note == 19) {
            return "Mi 5";
        }
        if (note == 20) {
            return "Fa 5";
        }
        if (note == 21) {
            return "Fa# 5 / Solb 5";
        }
        if (note == 22) {
            return "Sol 5";
        }
        if (note == 23) {
            return "Sol# 5 / Lab 5";
        }
        if (note == 24) {
            return "La 5";
        }
        if (note == 25) {
            return "La# 5 / Sib 5";
        }
        if (note == 26) {
            return "Si 5";
        }
        if (note == 27) {
            return "Do 6";
        }
        if (note == 28) {
            return "Do# 6 / Réb 6";
        }
        if (note == 29) {
            return "Ré 6";
        }
        if (note == 30) {
            return "Ré# 6 / Mib 6";
        }
        if (note == 31) {
            return "Mi 6";
        }
        if (note == 32) {
            return "Fa 6";
        }
        if (note == 33) {
            return "Fa# 6 / Solb 6";
        }
        if (note == 34) {
            return "Sol 6";
        }
        if (note == 35) {
            return "Sol# 6 / Lab 6";
        }
        if (note >= 36) {
            return "Haute";
        }
        return "Out of detection";
    }
}
