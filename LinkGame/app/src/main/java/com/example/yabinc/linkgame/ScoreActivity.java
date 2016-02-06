package com.example.yabinc.linkgame;

import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.TextView;

public class ScoreActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_score);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        setTitle(R.string.title_activity_score);
        Setting setting = new Setting(this);
        ScoreInfo info = setting.getScoreInfo();

        TextView textViewPlayTimes = (TextView) findViewById(R.id.show_play_times);
        textViewPlayTimes.setText("" + info.playTimes);
        TextView textViewTotalWinLevel = (TextView) findViewById(R.id.show_total_win_level);
        textViewTotalWinLevel.setText("" + info.totalWinLevel);
        TextView textViewMaxWinLevelInOneTime = (TextView) findViewById(R.id.show_max_win_level_in_one_time);
        textViewMaxWinLevelInOneTime.setText("" + info.maxWinLevelInOneTime);
        TextView textViewTotalScore = (TextView) findViewById(R.id.show_total_score);
        textViewTotalScore.setText("" + info.totalScore);
        TextView textViewMaxScoreInOneTime = (TextView) findViewById(R.id.show_max_score_in_one_time);
        textViewMaxScoreInOneTime.setText("" + info.maxScoreInOneTime);
    }

}
