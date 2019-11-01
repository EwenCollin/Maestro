/*
The Metronom is used to work with the notes data, the ones you play, the ones you have to play.
It can determinate the next note to play according to an ArrayList of notes to play (usually from a MIDI file).
It can determinate if you play notes correctly.
*/
package net.gamescode.ewen.maestro;

import android.provider.ContactsContract;
import android.util.Log;

import java.lang.reflect.Array;
import java.util.ArrayList;

public class Metronom {

    private int timer = 0;
    private int elapsedTime = 0;
    private int indexOfNoteToGenerate = 0;
    private int indexOfNextNoteToPlay = 0;
    private int indexOfNotePlayed = 0;
    private ArrayList<Integer[]> notes = new ArrayList<>();
    private ArrayList<Integer[]> NotesToPlay = new ArrayList<>();
    private ArrayList<Integer[]> NotesSuccessful = new ArrayList<>();


    public void update(int inTimer) {
        timer = inTimer;
    }

    public ArrayList<Integer[]> getNotesToPlay() {
        return NotesToPlay;
    }

    public Integer noteGenerator(ArrayList<Integer[]> notesToGenerate) {
        elapsedTime = elapsedTime + 5;
        if(elapsedTime >= notesToGenerate.get(indexOfNoteToGenerate)[2]) {
            elapsedTime = 0;
            indexOfNoteToGenerate++;
            if(indexOfNoteToGenerate > notesToGenerate.size() - 1) indexOfNoteToGenerate = 0;
        }
        return notesToGenerate.get(indexOfNoteToGenerate)[1];

    }

    public ArrayList<Integer[]> noteListGenerator(ArrayList<Integer[]> notesToGenerate) {
        ArrayList<Integer[]> notesListGenerated = new ArrayList<>();
        for (int i = 0; i < notesToGenerate.size(); i++) {
            for(int x = 0; x < Math.round(notesToGenerate.get(i)[2]/12); x++) {
                Integer[] note = new Integer[3];
                note[0] = notesToGenerate.get(i)[0] + x*5;
                note[1] = notesToGenerate.get(i)[1];
                note[2] = 5;
                notesListGenerated.add(note);
            }
        }
        return  notesListGenerated;
    }

    public void metronom(ArrayList<Integer[]> notes, ArrayList<Integer[]> notesToPlay) {

        Integer[] note = new Integer[3];

        ArrayList<Integer[]> lastNotesPlayed = getLastNotes(notes, 20);

        ArrayList<Integer> nextNotes = getNextNote(lastNotesPlayed, notesToPlay, 10, 20);

        for(int i = 0; i < nextNotes.size(); i++) {
            note[0] = timer;
            note[1] = nextNotes.get(i);
            if(note[1] != null) NotesToPlay.add(note);

        }

        //Log.d("Metronom", "BONJOIR" + NotesToPlay);

        //Log.d("Metronom", "LastNote : " + lastNotesPlayed.get(0) + " NextNote : " + nextNote);





    }

    public ArrayList<Integer> getDifferentNotes(ArrayList<Integer> previousNotes, ArrayList<Integer> nextNotes) {
        ArrayList<Integer> differentNotes = new ArrayList<>();
        for(int i = 0; i < previousNotes.size(); i++) {
            int j = 0;
            for(int x = 0; x < nextNotes.size(); x++) {
                if(!previousNotes.get(i).equals(nextNotes.get(x))) j++;
            }
            if(j == nextNotes.size() - 1 || nextNotes.size() == 0) differentNotes.add(previousNotes.get(i));
        }
        return differentNotes;
    }

    public ArrayList<Integer[]> getNotesSuccessful(ArrayList<Integer[]> notes) {
        ArrayList<Integer[]> lastNotesPlayed = getLastNotes(notes, 20);
        ArrayList<Integer[]> lastNotesToPlay = getLastNotes(NotesToPlay, 20);

        if(lastNotesPlayed.size() > 0 && lastNotesToPlay.size() > 1) {
            for(int i = 0; i < lastNotesToPlay.size() - 1; i++) {
                if(i < lastNotesPlayed.size()) {
                    if(lastNotesPlayed.get(i)[1].equals(lastNotesToPlay.get(i + 1)[1])) {
                        Integer[] note = notes.get(notes.size() - 1).clone();
                        note[0] = timer;
                        note[1] = lastNotesPlayed.get(0)[1];
                        NotesSuccessful.add(note);
                    }
                }
            }
        }

        return NotesSuccessful;
    }

    public ArrayList<Integer[]> getLastNotes(ArrayList<Integer[]> notes, int numberOfNotes) {
        ArrayList<Integer[]> notesPlayed = new ArrayList<>();
        int x = 0;
        int lastX = 0;
        for(int i = 0; i < numberOfNotes; i++) {
            Integer[] currentNote = new Integer[3];
            currentNote[0] = 0;
            currentNote[1] = 0;
            if (notes.size() - 1 - x - i >= 0) {
                while (notes.get(notes.size() - 1 - x - i)[0] - timer > 0 && notes.size() - 2 - x - i >= 0 && notes.size() - 2 - x - i < notes.size())
                    x++;
            }
            if(notes.size() - 1 - x - i >= 0 && notes.size() - 1 - x - i < notes.size()) {
                currentNote[0] = notes.get(notes.size() - 1 - x - i)[0];
                currentNote[1] = notes.get(notes.size() - 1 - x - i)[1];
                if (notes.size() - 1 - x - i >= 0) {
                    while (currentNote[1].equals(notes.get(notes.size() - 1 - x - i)[1]) && notes.size() - 2 - x - i >= 0) x++;
                }
                currentNote[0] = x;
                if(i != 0)  currentNote[2] = (x - lastX)*5;
                lastX = x;
                notesPlayed.add(currentNote);
            }

        }

        return notesPlayed;
    }

    public ArrayList<Integer> getNotesListToPlay(ArrayList<Integer[]> notes) {
        ArrayList<Integer> notesListToPlay = new ArrayList<>();

        for(int i = 0; i < notes.size(); i++) {
            if(notes.get(i)[0] - timer >= 0 && notes.get(i)[0] - timer < 5) {
                if(i - 1 >= 0 && notes.get(i)[1].equals(notes.get(i - 1)[1]) && notes.get(i)[0] - notes.get(i - 1)[0] == 5){

                }
                else notesListToPlay.add(notes.get(i)[1]);
                //Log.d("getNotesListToPlay", "Notes in list are : " + notes.get(i)[1]);
            }
        }
        return notesListToPlay;
    }

    public ArrayList<Integer> getNextNote(ArrayList<Integer[]> notesPlayed, ArrayList<Integer[]> notesToPlay, int notesToRecognize, int percentOfAccuracy) {
        ArrayList<Integer> nextNotes = new ArrayList<>();
        int nextNoteIndex = 0;
        int notesRecognized = 0;
        int detectTime = 0;
        for(int i = 0; i < notesToPlay.size(); i++) {
            if (notesToPlay.get(i)[1].equals(notesPlayed.get(0)[1])) {
                notesRecognized = 0;
                for(int x = 0; x < notesToRecognize; x++) {
                    if(i - x >= 0 && x < notesPlayed.size()
                            && notesPlayed.get(x)[1].equals(notesToPlay.get(i - x)[1])) {
                        notesRecognized++;
                    }
                }
                if(100*notesRecognized/notesToRecognize >= percentOfAccuracy) {
                    nextNoteIndex = i + 1;
                    if(nextNoteIndex < notesToPlay.size()) {
                        int j = 0;
                        while (nextNoteIndex + j < notesToPlay.size() && notesToPlay.get(nextNoteIndex)[0].equals(notesToPlay.get(nextNoteIndex + j)[0])) {
                            j++;
                            nextNotes.add(notesToPlay.get(nextNoteIndex)[1]);

                        }
                        indexOfNextNoteToPlay = nextNoteIndex - 1;
                    }
                    i = notesToPlay.size();
                }

            }
        }
        return nextNotes;
    }


    /*
    public Integer getNextNote(ArrayList<Integer[]> notes, ArrayList<Integer[]> notesToPlay, int notesToRecognize) {
        Integer nextNote = null;
        int nextNoteIndex = 0;
        int notesRecognized = 0;
        int detectTime = 0;
        for(int i = 0; i < notesToPlay.size(); i++) {
            for(int x = 0; x < notes.size(); x++) {
                int indexOfNotesPlayed = 0;
                int indexOfNotesToPlay = 0;
                for(int j = 0; j < notesToRecognize; j++) {
                    indexOfNotesPlayed = x + j;
                    if (indexOfNotesPlayed >= notes.size()) indexOfNotesPlayed = indexOfNotesPlayed - notes.size() + 1;
                    indexOfNotesToPlay = i - j;
                    if(indexOfNotesToPlay < 0) indexOfNotesToPlay = notesToPlay.size() + indexOfNotesToPlay - 1;
                    if(notes.get(indexOfNotesPlayed)[1].equals(notesToPlay.get(indexOfNotesToPlay)[1])) {
                        notesRecognized++;
                    }
                }
                if(notesRecognized == notesToRecognize) {
                    nextNoteIndex = indexOfNotesToPlay + indexOfNotesPlayed + 1;
                    if (nextNoteIndex < 0) nextNoteIndex = notesToPlay.size() + nextNoteIndex;
                    if (nextNoteIndex >= notesToPlay.size()) nextNoteIndex = nextNoteIndex - notesToPlay.size();
                    if(nextNoteIndex < notesToPlay.size() && nextNoteIndex >= 0) {
                        nextNote = notesToPlay.get(nextNoteIndex)[1];
                    }
                    x = notes.size();
                    i = notesToPlay.size();
                }
                notesRecognized = 0;
            }
        }
        //Log.d("getNextNote", "Value of nextNote is " + nextNote);
        return nextNote;
    }*/

}
