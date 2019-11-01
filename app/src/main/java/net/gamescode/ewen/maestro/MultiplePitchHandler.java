/*
The MultiplePitchHandler is made to detect multiple frequencies played at the same time.
It uses the TarsosDSP fft and modulus to obtain a spectrogram of the sound.
Then using several methods, it determinates the notes played by sorting the intensities of frequencies.
The more it is intense, the more it has chances to be detected as a fundamental frequency.
*/
package net.gamescode.ewen.maestro;

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
