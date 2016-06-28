package com.example.yabinc.sudogame;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity {

    private GameModel gameModel;
    private GameView gameView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        gameView = (GameView) findViewById(R.id.gameView);
        gameModel = new GameModel();
        gameView.init(gameModel);
    }

    static {
        System.loadLibrary("sudo-game-jni");
    }
    public native String getMsgFromJni();
}
