/*
The MultiplePitchHandler is made to detect multiple frequencies played at the same time.
It uses the TarsosDSP fft and modulus to obtain a spectrogram of the sound.
Then using several methods, it determinates the notes played by sorting the intensities of frequencies.
The more it is intense, the more it has chances to be detected as a fundamental frequency.
*/
package net.gamescode.ewen.maestro;

import android.util.Log;

import be.tarsos.dsp.AudioDispatcher;
import be.tarsos.dsp.AudioEvent;
import be.tarsos.dsp.AudioProcessor;
import be.tarsos.dsp.pitch.PitchDetectionHandler;
import be.tarsos.dsp.pitch.PitchDetectionResult;
import be.tarsos.dsp.util.fft.FFT;

public class MultiplePitchHandler implements PitchDetectionHandler {

    private AudioDispatcher dispatcher;
    private double pitch;

    private float sampleRate = 44100;
    private int bufferSize = 1024 * 4;
    private int overlap = 768 * 4 ;
    float[] amplitudes = new float[bufferSize/2];

    private int numberOfDetections = 3;



    public AudioProcessor fftProcessor = new AudioProcessor(){

        FFT fft = new FFT(bufferSize);

        @Override
        public void processingFinished() {
        }

        @Override
        public boolean process(AudioEvent audioEvent) {
            float[] audioFloatBuffer = audioEvent.getFloatBuffer();
            float[] transformbuffer = new float[bufferSize*2];
            System.arraycopy(audioFloatBuffer, 0, transformbuffer, 0, audioFloatBuffer.length);
            fft.forwardTransform(transformbuffer);
            fft.modulus(transformbuffer, amplitudes);
            return true;
        }
    };



    public float[] getAmplitudes() {
        return amplitudes;
    }

    public float getTotalIntensity() {
        int total = 0;
        for(int i = 0; i < amplitudes.length; i++) {
            total += amplitudes[i];
        }
        return total;
    }

    public float getAverageIntensity() {
        float average = 0;
        for(int i = 0; i < amplitudes.length; i++) {
            average = (average*i + amplitudes[i])/(i+1);
        }
        return average;
    }

    //public int[] getMaximumFrequenciesIndexes(int number, int seuil) {

    //}

    public int[] getFrequenciesFromSpectre() {
        float[][] freqs = convertSpectreToFreqAndIntens(getRelevantValues());
        if(freqs.length < 100) {
            int numberOfHarmoniquesToHave = 4;
            float[] maxFreqI = new float[2];
            boolean[] alreadyChecked = new boolean[freqs.length];
            int[] hasHarmoniques = new int[freqs.length];
            int countOfFreqs = 0;
            for(int j = 0; j < freqs.length; j++) {
                maxFreqI[0] = 0;
                maxFreqI[1] = 0;
                for(int i = 0; i < freqs.length; i++) {
                    if(maxFreqI[1] < freqs[i][1] && freqs[i][0] != -1 && !alreadyChecked[i]) {
                        maxFreqI[0] = i;
                        maxFreqI[1] = freqs[i][1];
                    }
                }
                alreadyChecked[(int)maxFreqI[0]] = true;
                for(int i = 0; i < freqs.length; i++) {
                    for(int x = 0; x < freqs.length; x++) {
                        int progression = Utils.progression((int)freqs[(int) maxFreqI[0]][0], 2, i+1);
                        Log.d("MPHandler", "Got base frequency of : " + freqs[(int) maxFreqI[0]][0] + " , comparing it to : " + freqs[x][0] + " expected frequency of : " + progression);
                        if( progression >= freqs[x][0] - (i+0.5)*freqs[(int) maxFreqI[0]][0]
                                && progression <= freqs[x][0] + (i+0.5)*freqs[(int) maxFreqI[0]][0]
                                && freqs[(int) maxFreqI[0]][0] != freqs[x][0]) {
                            freqs[x][0] = -1;
                            hasHarmoniques[(int)maxFreqI[0]]++;
                        }
                    }

                }
            }
            for(int i = 0; i < freqs.length; i++) {
                if(freqs[i][0] != -1 && hasHarmoniques[i] >= numberOfHarmoniquesToHave) {
                    countOfFreqs++;
                }
            }
            int[] outFreqs = new int[countOfFreqs];
            int x = 0;
            for(int i = 0; i < freqs.length; i++) {
                if(freqs[i][0] != -1 && hasHarmoniques[i] >= numberOfHarmoniquesToHave) {
                    outFreqs[x] = (int) freqs[i][0];
                    x++;
                }
            }
            return outFreqs;
        }
        else {
            int[] outFreqs = new int[1];
            return outFreqs;

        }

    }


    public float[][] convertSpectreToFreqAndIntens(int[] relevantIndexes) {
        float average = getAverageIntensity();
        for(int i = 0; i < relevantIndexes.length; i++) {
            if(amplitudes[relevantIndexes[i]] < average*5) relevantIndexes[i] = 0;
        }
        int[] cleanedIndexes = Utils.cleanupTableInt(relevantIndexes);
        float[][] freqs = new float[cleanedIndexes.length][2];

        for(int i = 0; i < freqs.length; i++) {
            freqs[i][0] = cleanedIndexes[i] * 44100 / (amplitudes.length * 8);
            freqs[i][1] = amplitudes[cleanedIndexes[i]];
        }
        return freqs;
    }

    public int[] getRelevantValues() {
        int lastValueIndex = 0;
        int[] relevantValues = new int[amplitudes.length];
        int relevantI = 0;
        for(int i = 0; i < amplitudes.length; i++) {
            if(amplitudes[i] > amplitudes[lastValueIndex]) {
                int maxIndex = 10;
                int tableSize = 10;
                while(maxIndex == tableSize) {
                    tableSize++;
                    maxIndex = Utils.getMaximumValueIndex(Utils.copyFromIndexAndRange(amplitudes, lastValueIndex, tableSize));
                }
                if(lastValueIndex + maxIndex > amplitudes.length) i = amplitudes.length;
                else if(amplitudes[lastValueIndex + maxIndex] - amplitudes[lastValueIndex] > amplitudes[lastValueIndex]){
                    relevantValues[relevantI] = lastValueIndex + maxIndex;
                    relevantI++;
                    lastValueIndex = lastValueIndex + maxIndex + 1;
                    i = lastValueIndex + 1;
                }
            }
            else { lastValueIndex = i;
            }
        }
        return relevantValues;
    }

    /*
    public int[] getRelevantValues() {
        int lastValueIndex = 0;
        int valueCountA = 0;
        int valueCountB = 0;
        boolean resetBin = false;
        int[] relevantValues = new int[amplitudes.length];
        int relevantI = 0;
        float smallestValue = 0;
        float average = getAverageIntensity();
        for(int i = 0; i < amplitudes.length - 1; i++) {
            if(amplitudes[i] > amplitudes[lastValueIndex]) {
                valueCountA++;
            }
            else resetBin = true;
            if(resetBin) {
                resetBin = false;
                if(valueCountA > 1 && amplitudes[lastValueIndex] > smallestValue + average) {
                    relevantValues[relevantI] = lastValueIndex;
                    relevantI++;
                    valueCountA = 0;
                }
            }
            if(amplitudes[i] < amplitudes[lastValueIndex]) {
                valueCountB++;
            }
            else resetBin = true;
            if(resetBin) {
                resetBin = false;
                if(valueCountB > 1) {
                    smallestValue = amplitudes[lastValueIndex];
                    valueCountB = 0;
                }

            }
            lastValueIndex = i;
        }
        return relevantValues;
    }*/


    /*
    public int[] getRelevantValues() {
        int startBin = 0;
        int endBin = 1;
        float average = getAverageIntensity();
        boolean resetBin = false;
        int[] relevantValues = new int[amplitudes.length];
        int relevantI = 0;
        for(int i = 0; i < amplitudes.length - 1; i++) {
            if(amplitudes[i+1] >= amplitudes[startBin]) {
                endBin++;
            }
            else resetBin = true;
            if(resetBin) {
                if(endBin - startBin > 0) {
                    int[] binIndexes = new int[endBin - startBin];
                    int k = 0;
                    for(int j = startBin; j < endBin; j++) {
                        binIndexes[k] = j;
                        k++;
                    }
                    relevantValues[relevantI] = Utils.getMaximumValueIndex( Utils.copyFromIndexes(amplitudes, binIndexes));
                    relevantI++;
                }
                resetBin = false;
                startBin = endBin;
                endBin++;
            }
        }
        return relevantValues;
    }*/

    public int[] getFrequenciesSorted() {
        int[] indexes = Utils.getMaximumValuesIndexes(amplitudes, numberOfDetections);
        int[] frequencies = getFrequencies();
        int[] frequenciesOutput = new int[frequencies.length];
        for(int i = 0; i < frequencies.length; i++) {
            for(int j = 0; j < frequencies.length; j++) {
                if(Math.abs(Utils.frequencyToNoteN(frequencies[i], 0) - Utils.frequencyToNoteN(frequencies[j], 0)) == 12) {
                    if(amplitudes[indexes[i]] > amplitudes[indexes[j]]) {
                        frequenciesOutput[i] = frequencies[i];
                    }
                    else frequenciesOutput[i] = frequencies[j];
                }
                else if(Math.abs(Utils.frequencyToNoteN(frequencies[i], 0) - Utils.frequencyToNoteN(frequencies[j], 0)) == 1) {
                    if(amplitudes[indexes[i]] > amplitudes[indexes[j]]) {
                        frequenciesOutput[i] = frequencies[i];
                    }
                    else frequenciesOutput[i] = frequencies[j];
                }
                else {
                    frequenciesOutput[i] = frequencies[i];
                }
            }
        }
        return frequenciesOutput;
    }

    public int[] getFrequencies() {
        int[] indexes = getBinIndexes();
        int[] frequencies = new int[indexes.length];
        for(int x = 0; x < indexes.length; x++) {
            if(indexes[x] != 0) frequencies[x] = indexes[x] * 44100 / (amplitudes.length * 8);
        }
        return frequencies;
    }

    public float[] getBinAmplitudes() {
        int[] indexes = getBinIndexes();
        float[] indexesValues = new float[bufferSize/2];
        int indexer = 0;
        for(int i = 0; i < indexesValues.length; i++) {
            if(indexes[indexer] == i) indexesValues[i] = amplitudes[i];
            else indexesValues[i] = 0;
        }
        return indexesValues;
    }

    public int[] getBinIndexes() {
        int[] indexes = new int[10];
        indexes = Utils.getMaximumValuesIndexes(amplitudes, numberOfDetections);
        return indexes;
    }



    @Override
    public void handlePitch(PitchDetectionResult pitchDetectionResult, AudioEvent audioEvent) {

    }
}
