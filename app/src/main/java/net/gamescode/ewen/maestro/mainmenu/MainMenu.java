package net.gamescode.ewen.maestro.mainmenu;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.AssetFileDescriptor;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;


import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import net.gamescode.ewen.maestro.R;
import net.gamescode.ewen.maestro.Utils;
import net.gamescode.ewen.maestro.ViewHolderActivity;

import java.io.IOException;

public class MainMenu extends AppCompatActivity {

    private Button startNewGame;
    private Button startMetronom;
    private Button soundTest;
    private Button recordNotes;
    private Button browseFiles;
    private RadioGroup selectAction;
    private RadioGroup selectAlgo;
    private EditText textFile;
    private String midiUri;
    Context context;
    private static final int READ_REQUEST_CODE = 42;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_menu);
        context = this.getApplicationContext();


        textFile = (EditText)findViewById(R.id.editTextFile);

        selectAction = (RadioGroup) findViewById(R.id.action);
        selectAlgo = (RadioGroup) findViewById(R.id.algo);



        browseFiles = (Button) findViewById(R.id.buttonBrowseFiles);

        browseFiles.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.KITKAT)
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
                intent.addCategory(Intent.CATEGORY_OPENABLE);
                intent.setType("audio/*");
                startActivityForResult(intent, READ_REQUEST_CODE);

            }
        });

        startNewGame = (Button) findViewById(R.id.button);

        startNewGame.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent gameViewIntent = new Intent(context, ViewHolderActivity.class);
                gameViewIntent.putExtra("CONTEXT", "basicDetection");
                String[] extras = getSelection();
                gameViewIntent.putExtra("ACTION", extras[0]);
                gameViewIntent.putExtra("ALGO", extras[1]);
                gameViewIntent.putExtra("FILENAME", midiUri);

                startActivity(gameViewIntent);
            }
        });

        startMetronom = (Button) findViewById(R.id.buttonMetronom);

        startMetronom.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent gameViewIntent = new Intent(context, ViewHolderActivity.class);
                gameViewIntent.putExtra("CONTEXT", "smartMetronom");
                String[] extras = getSelection();
                gameViewIntent.putExtra("ACTION", extras[0]);
                gameViewIntent.putExtra("ALGO", extras[1]);
                gameViewIntent.putExtra("FILENAME", midiUri);

                startActivity(gameViewIntent);
            }
        });

        soundTest = (Button) findViewById(R.id.buttonSoundtest);


        soundTest.setOnClickListener(new View.OnClickListener() {
            MediaPlayer player = new MediaPlayer();
            @Override
            public void onClick(View v) {
                String path = "piano/Piano.pp." + "A3" + ".wav";
                try {
                    player.release();
                    player = new MediaPlayer();
                    AssetFileDescriptor afd = context.getAssets().openFd(path);
                    player.setDataSource(afd.getFileDescriptor(),afd.getStartOffset(),afd.getLength());
                    player.prepare();
                    player.start();
                } catch(IOException e) {
                    e.printStackTrace();
                }
            }
        });

        recordNotes = (Button) findViewById(R.id.buttonRecord);

        recordNotes.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent gameViewIntent = new Intent(context, ViewHolderActivity.class);
                gameViewIntent.putExtra("CONTEXT", "recordNotes");
                String[] extras = getSelection();
                gameViewIntent.putExtra("ACTION", extras[0]);
                gameViewIntent.putExtra("ALGO", extras[1]);
                gameViewIntent.putExtra("FILENAME", midiUri);
                startActivity(gameViewIntent);
            }
        });

    }

    private String[] getSelection() {
        String[] selection = new String[2];
        RadioButton selectedRadio = (RadioButton)findViewById(selectAction.getCheckedRadioButtonId());
        if(selectedRadio != null) {
            if(selectedRadio.getText().equals("AutoPlay")) selection[0] = "AutoPlay";
            else if(selectedRadio.getText().equals("Spectrogram")) selection[0] = "Spectrogram";
            else if(selectedRadio.getText().equals("Listen")) selection[0] = "basicAction";
            else if(selectedRadio.getText().equals("Record")) selection[0] = "Record";
            else if(selectedRadio.getText().equals("Play record")) selection[0] = "PlayRecord";
            else if(selectedRadio.getText().equals("Draw FFT")) selection[0] = "DrawFFT";
            else selection[0] = "basicAction";
        }
        else selection[0] = "basicAction";
        selectedRadio = (RadioButton)findViewById(selectAlgo.getCheckedRadioButtonId());
        if(selectedRadio != null) {
            if(selectedRadio.getText().equals("FFT_YIN")) selection[1] = "FFT_YIN";
            else if(selectedRadio.getText().equals("DYNAMIC_WAVELET")) selection[1] = "DYNAMIC_WAVELET";
            else if(selectedRadio.getText().equals("Multiple Pitch Handler")) selection[1] = "MPH";
            else selection[1] = "FFT_YIN";
        }
        else selection[1] = "FFT_YIN";
        return selection;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode,
                                 Intent resultData) {


        if (requestCode == READ_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            Uri uri = null;
            if (resultData != null) {
                uri = resultData.getData();
                textFile.setText(uri.toString());
                midiUri = uri.toString();
            }
        }
    }


}
