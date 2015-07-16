package com.yabinc.networkusage;

import android.app.Activity;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

import com.yabinc.logger.Log;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;

public class HttpPageActivity extends Activity {

    public static String TAG = "HttpPageActivity";

    public static String URL = "http://picasaweb.google.com/data/feed/base/featured?alt=rss&kind=photo&access=public&slabel=featured&hl=en_US&imgmax=1600";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_http_page);
        TextView urlText = (TextView) findViewById(R.id.http_page_url);
        urlText.setText(URL);

        retrievePage(URL);
    }

    private void retrievePage(String url) {
        ConnectivityManager connectivityManager =
                (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
        if (networkInfo != null && networkInfo.isConnected()) {
            new DownloadHttpPageTask().execute(url);
        } else {
            Log.d(TAG, "No network connection available.");
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_http_page, menu);
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

    private class DownloadHttpPageTask extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... urls) {
            try {
                return downloadUrl(urls[0]);
            } catch (IOException e) {
                Log.d(TAG, "Unable to retrieve web page.");
            }
            return null;
        }

        @Override
        protected void onPostExecute(String s) {
            TextView contentView = (TextView) findViewById(R.id.http_page_content);
            contentView.setText(s);
        }

        private String downloadUrl(String stringUrl) throws IOException {
            InputStream inputStream = null;
            try {
                URL url = new URL(stringUrl);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setReadTimeout(60000);
                conn.setConnectTimeout(60000);
                conn.setRequestMethod("GET");
                conn.setDoInput(true);
                conn.connect();
                int response = conn.getResponseCode();
                Log.d(TAG, "responseCode: " + response);
                inputStream = conn.getInputStream();
                String result = readIt(inputStream);
                return result;
            } finally {
                if (inputStream != null) {
                    inputStream.close();
                }
            }
        }

        private String readIt(InputStream inputStream) throws IOException, UnsupportedEncodingException {
            Reader reader = null;
            reader = new InputStreamReader(inputStream, "UTF-8");
            char[] buffer = new char[4096];
            StringBuilder builder = new StringBuilder();
            while (true) {
                int ret = reader.read(buffer);
                if (ret == -1) {
                    break;
                }
                builder.append(buffer, 0, ret);
            }
            return builder.toString();
        }
    }
}
