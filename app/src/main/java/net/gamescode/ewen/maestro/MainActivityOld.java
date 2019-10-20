package net.gamescode.ewen.maestro;

import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.TextView;

import be.tarsos.dsp.AudioDispatcher;
import be.tarsos.dsp.AudioEvent;
import be.tarsos.dsp.io.android.AudioDispatcherFactory;
import be.tarsos.dsp.pitch.PitchDetectionHandler;
import be.tarsos.dsp.pitch.PitchDetectionResult;
import be.tarsos.dsp.pitch.PitchProcessor;

public class MainActivityOld extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        AudioDispatcher dispatcher = AudioDispatcherFactory.fromDefaultMicrophone(22050, 1024, 0);

        if (dispatcher != null) {
            dispatcher.addAudioProcessor(new PitchProcessor(PitchProcessor.PitchEstimationAlgorithm.FFT_YIN, 22050, 1024, new PitchDetectionHandler() {

                @Override
                public void handlePitch(PitchDetectionResult pitchDetectionResult,
                                        AudioEvent audioEvent) {
                    final float pitchInHz = pitchDetectionResult.getPitch();
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            //TextView text1 = (TextView) findViewById(R.id.textView1);
                            //text1.setText("" + pitchInHz + " Hz");
                            //TextView text2 = (TextView) findViewById(R.id.textView1);
                            int note = Utils.frequencyToNoteN(pitchInHz);
                            String noteS = Utils.noteNToNoteSfr(note);
                            //text2.setText("" + noteS);
                        }
                    });

                }
            }));

            new Thread(dispatcher, "Audio Dispatcher").start();
        }

    }

}
