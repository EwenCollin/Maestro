package net.gamescode.ewen.maestro.mainmenu;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;


import androidx.appcompat.app.AppCompatActivity;
import net.gamescode.ewen.maestro.R;
import net.gamescode.ewen.maestro.ViewHolderActivity;

public class MainMenu extends AppCompatActivity {

    private Button startNewGame;
    Context context;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_menu);
        context = this.getApplicationContext();

        startNewGame = (Button) findViewById(R.id.button);

        startNewGame.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(context, ViewHolderActivity.class));
            }
        });

    }

}
