package com.example.yabinc.linkgame;

import android.app.DialogFragment;
import android.app.FragmentTransaction;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity
        implements SelectSizeDialogFragment.SelectSizeDialogListener,
        SelectLevelDialogFragment.SelectLevelDialogListener {

    private static final String LOG_TAG = "MainActivity";
    private GameView mGameView;
    private String[] levelNames;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        levelNames = getResources().getStringArray(R.array.level_names);
        Bitmap animalBitmap = BitmapFactory.decodeResource(getResources(), R.mipmap.animals);
        Bitmap winBitmap = BitmapFactory.decodeResource(getResources(), R.mipmap.win);
        Bitmap loseBitmap = BitmapFactory.decodeResource(getResources(), R.mipmap.lose);
        mGameView = (GameView) findViewById(R.id.gameView);
        mGameView.init(animalBitmap, winBitmap, loseBitmap, levelNames.length);

    }

    public void setTitleByLevel(int level) {
        setTitle(levelNames[level - 1]);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }
        if (id == R.id.action_hint) {
            mGameView.hint();
            return true;
        }
        if (id == R.id.action_restart) {
            mGameView.restart(false);
            return true;
        }
        if (id == R.id.action_select_size) {
            showSelectSizeDialog();
            return true;
        }
        if (id == R.id.action_select_level) {
            showSelectLevelDialog();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (mGameView != null) {
            mGameView.startTimer();
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (mGameView != null) {
            mGameView.stopTimer();
        }
    }

    private void showSelectSizeDialog() {
        GameView.SizeInfo sizeInfo = mGameView.getSize();
        FragmentTransaction ft = getFragmentManager().beginTransaction();
        DialogFragment f = SelectSizeDialogFragment.newInstance(sizeInfo.useDefaultSize, sizeInfo.rows, sizeInfo.cols);
        f.show(ft, "dialog");
    }

    @Override
    public void onSelectSize(boolean useDefault, int rows, int cols) {
        if (!useDefault) {
            if (rows <= 0 || rows >= 40 || cols <= 0 || cols >= 40 || (rows * cols % 2 == 1)) {
                Toast.makeText(this, "Invalid select size, row/col in range [1,39], row * col should be even",
                        Toast.LENGTH_SHORT).show();
                return;
            }
        }
        mGameView.setSize(useDefault, rows, cols);
    }

    private void showSelectLevelDialog() {
        FragmentTransaction ft = getFragmentManager().beginTransaction();
        GameView.LevelInfo levelInfo = mGameView.getLevelInfo();
        DialogFragment f = SelectLevelDialogFragment.newInstance(levelInfo.curLevel, levelInfo.maxLevel, levelNames);
        f.show(ft, "dialog");
    }

    @Override
    public void onSelectLevel(int level) {
        mGameView.setCurLevel(level);
    }
}
