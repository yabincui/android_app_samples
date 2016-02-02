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
        SelectLevelDialogFragment.SelectLevelDialogListener,
        LevelChangeListener {

    private static final String LOG_TAG = "MainActivity";
    private Setting mSetting;
    private GameView mGameView;
    private String[] levelNames;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        mSetting = new Setting(this);
        levelNames = getResources().getStringArray(R.array.level_names);
        LevelInfo levelInfo = mSetting.getLevelInfo();
        if (levelInfo.maxLevel == -1) {
            levelInfo = new LevelInfo(1, 1, levelNames.length);
            mSetting.setLevelInfo(levelInfo);
        }
        GameView.PictureArg pictureArg = new GameView.PictureArg();
        pictureArg.animalBitmap = BitmapFactory.decodeResource(getResources(), R.mipmap.animals);
        pictureArg.winBitmap = BitmapFactory.decodeResource(getResources(), R.mipmap.win);
        pictureArg.loseBitmap = BitmapFactory.decodeResource(getResources(), R.mipmap.lose);
        pictureArg.pauseBitmap = BitmapFactory.decodeResource(getResources(), R.mipmap.pause);
        mGameView = (GameView) findViewById(R.id.gameView);
        mGameView.init(pictureArg, mSetting.getSizeInfo(), levelInfo, this);
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
    public boolean onPrepareOptionsMenu(Menu menu) {
        MenuItem item = menu.findItem(R.id.action_pause_resume);
        if (mGameView.isPaused()) {
            item.setTitle(R.string.menu_resume);
        } else {
            item.setTitle(R.string.menu_pause);
        }

        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        switch (item.getItemId()) {
            case R.id.action_settings:
                break;
            case R.id.action_hint:
                mGameView.hint();
                break;
            case R.id.action_pause_resume:
                if (mGameView.isPaused()) {
                    mGameView.resume();
                } else {
                    mGameView.pause();
                }
                break;
            case R.id.action_restart:
                mGameView.restart();
                break;
            case R.id.action_select_size:
                showSelectSizeDialog();
                break;
            case R.id.action_select_level:
                showSelectLevelDialog();
                break;
            default:
                return super.onOptionsItemSelected(item);
        }
        return true;
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
        SizeInfo sizeInfo = mSetting.getSizeInfo();
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
        SizeInfo info = new SizeInfo(useDefault, rows, cols);
        mSetting.setSizeInfo(info);
        mGameView.setSize(info);
    }

    private void showSelectLevelDialog() {
        FragmentTransaction ft = getFragmentManager().beginTransaction();
        LevelInfo levelInfo = mSetting.getLevelInfo();
        DialogFragment f = SelectLevelDialogFragment.newInstance(levelInfo.curLevel, levelInfo.maxAchievedLevel, levelNames);
        f.show(ft, "dialog");
    }

    @Override
    public void onSelectLevel(int level) {
        LevelInfo levelInfo = mSetting.getLevelInfo();
        levelInfo.curLevel = level;
        mGameView.setLevelInfo(levelInfo);
    }

    @Override
    public void onLevelChange(LevelInfo info) {
        mSetting.setLevelInfo(info);
    }
}
