package com.example.yabinc.sudogame;

import android.app.DialogFragment;
import android.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity implements SelectFixCountAndReasonableFragment.SelectFixCountAndReasonableDialogListener{

    private GameModel mGameModel;
    private GameView mGameView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mGameView = (GameView) findViewById(R.id.gameView);
        mGameModel = new GameModel();
        mGameView.init(mGameModel);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {
            case R.id.action_start_new:
                mGameView.startNewGame();
                break;
            case R.id.action_restart:
                mGameView.restartCurrentGame();
                break;
            case R.id.action_select_fixcount_and_reasonable:
                showSelectFixCountAndReasonableDialog();
                break;
            case R.id.action_hint:
                mGameView.hint();
                break;
            default:
                return super.onOptionsItemSelected(item);
        }
        return true;
    }

    private void showSelectFixCountAndReasonableDialog() {
        int fixCount = mGameModel.getFixCount();
        boolean reasonable = mGameModel.getReasonable();
        FragmentTransaction ft = getFragmentManager().beginTransaction();
        DialogFragment f = SelectFixCountAndReasonableFragment.newInstance(fixCount, reasonable);
        f.show(ft, "dialog");
    }

    @Override
    public void onSelectFixCountAndReasonable(int fixCount, boolean reasonable) {
        if (fixCount == -1 || fixCount < 10 || fixCount > 80) {
            Toast.makeText(this, "Invalid fix count, valid rnge is [10, 80]",
                    Toast.LENGTH_SHORT).show();
            return;
        }
        mGameModel.setFixCount(fixCount);
        mGameModel.setReasonable(reasonable);
        mGameView.startNewGame();
    }
}
