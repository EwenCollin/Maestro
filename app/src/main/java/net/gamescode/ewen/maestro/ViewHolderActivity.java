package net.gamescode.ewen.maestro;

import androidx.appcompat.app.AppCompatActivity;

import android.net.Uri;
import android.os.Bundle;

public class ViewHolderActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_holder);

        String newString;
        String action;
        String detectionAlgo;
        String fileUri;
        if (savedInstanceState == null) {
            Bundle extras = getIntent().getExtras();
            if(extras == null) {
                newString= null;
                action = null;
                detectionAlgo = null;
                fileUri = null;
            } else {
                newString= extras.getString("CONTEXT");
                action = extras.getString("ACTION");
                detectionAlgo = extras.getString("ALGO");
                fileUri = extras.getString("FILENAME");
            }
        } else {
            newString= (String) savedInstanceState.getSerializable("CONTEXT");
            action = (String) savedInstanceState.getSerializable("ACTION");
            detectionAlgo = (String) savedInstanceState.getSerializable("ALGO");
            fileUri = (String) savedInstanceState.getSerializable("FILENAME");
        }

        setContentView(new GameView(this, newString, action, detectionAlgo, fileUri));
    }
}
