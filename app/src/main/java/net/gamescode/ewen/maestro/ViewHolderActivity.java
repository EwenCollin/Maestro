package net.gamescode.ewen.maestro;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;

public class ViewHolderActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_holder);
        setContentView(new GameView(this));
    }
}
