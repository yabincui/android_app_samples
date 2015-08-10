package com.yabinc.networkusage;

import android.app.Activity;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

public class NetworkStateActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_network_state);
        checkNetworkState();
    }

    public void onRecheckClick(View view) {
        checkNetworkState();
    }

    private void checkNetworkState() {
        ConnectivityManager connectivityManager = (ConnectivityManager)
                getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo[] networkInfos = connectivityManager.getAllNetworkInfo();
        StringBuilder stringBuilder = new StringBuilder();
        for (int i = 0; i < networkInfos.length; ++i) {
            stringBuilder.append(networkInfos[i].getTypeName())
                    .append(": ")
                    .append(networkInfos[i].isConnected() ? "connected" : "not connected")
                    .append('\n');
        }
        TextView stateInfoText = (TextView) findViewById(R.id.network_state_info);
        stateInfoText.setText(stringBuilder.toString());
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_network_state, menu);
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

        return super.onOptionsItemSelected(item);
    }
}
