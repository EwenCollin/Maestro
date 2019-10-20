package net.gamescode.ewen.maestro;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.Menu;

import net.gamescode.ewen.maestro.mainmenu.MainMenu;
import net.gamescode.ewen.maestro.R;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        int SPLASH_DELAY = 3000;
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        new Handler().postDelayed(new Runnable(){
            @Override
            public void run() {
                /* Create an Intent that will start the Menu-Activity. */
                Intent mainIntent = new Intent(MainActivity.this, Menu.class);
                MainActivity.this.startActivity(new Intent(MainActivity.this, MainMenu.class));
                MainActivity.this.finish();
            }
        }, SPLASH_DELAY);
    }
}
