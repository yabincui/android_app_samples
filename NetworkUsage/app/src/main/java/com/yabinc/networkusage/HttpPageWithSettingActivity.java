package com.yabinc.networkusage;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.Menu;
import android.view.MenuItem;
import android.webkit.WebView;

import com.yabinc.logger.Log;

import org.xmlpull.v1.XmlPullParserException;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;

public class HttpPageWithSettingActivity extends Activity {
    public static final String TAG = "HttpPageWithSettingActivity";

    public static final String WIFI = "Wi-Fi";
    public static final String ANY = "Any";
    private static final String URL = "http://stackoverflow.com/feeds/tag?tagnames=android&sort=newest";

    private static String networkPreference;
    private static boolean showSummaryPreference;
    private boolean wifiConnected;
    private boolean mobileConnected;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_http_page_with_setting);
    }

    @Override
    protected void onStart() {
        super.onStart();
        networkPreference = PreferenceManager.getDefaultSharedPreferences(this).getString("listPref", "Wi-Fi");
        showSummaryPreference = PreferenceManager.getDefaultSharedPreferences(this).getBoolean("summaryPref", false);
        Log.d(TAG, "networkPreference = " + networkPreference);
        Log.d(TAG, "showSummaryPreference = " + showSummaryPreference);
        ConnectivityManager connectivityManager = (ConnectivityManager)
                getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
        if (networkInfo != null && networkInfo.isConnected()) {
            wifiConnected = (networkInfo.getType() == ConnectivityManager.TYPE_WIFI);
            mobileConnected = (networkInfo.getType() == ConnectivityManager.TYPE_MOBILE);
        } else {
            wifiConnected = false;
            mobileConnected = false;
        }
        Log.d(TAG, "wifiConnected = " + wifiConnected);
        Log.d(TAG, "mobileConnected = " + mobileConnected);
        loadPage();
    }

    private void loadPage() {
        if ((networkPreference.equals("Any") && (wifiConnected || mobileConnected))
                || (networkPreference.equals("Wi-Fi") && wifiConnected)) {
            Log.d(TAG, "Download http page");
            new DownloadHttpPageTask().execute(URL);
        } else {
            Log.d(TAG, "Show error page");
            showErrorPage();
        }
    }

    private void showErrorPage() {
        WebView webView = (WebView) findViewById(R.id.webview);
        webView.loadData("Network connection error.", "text/html", "null");
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_http_page_with_setting, menu);
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
            Intent intent = new Intent(this, SettingActivity.class);
            startActivity(intent);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private class DownloadHttpPageTask extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... urls) {
            try {
                return downloadStackOverflowUrl(urls[0]);
            } catch (IOException e) {
                return "Unable to retrieve url: " + urls[0];
            } catch (XmlPullParserException e) {
                return "Unable to parse xml: " + urls[0];
            }
        }

        @Override
        protected void onPostExecute(String s) {
            WebView webView = (WebView) findViewById(R.id.webview);
            webView.loadData(s, "text/html", null);
        }
    }

    private String downloadStackOverflowUrl(String stringUrl) throws IOException, XmlPullParserException {
        String content = downloadUrl(stringUrl);
        StackoverflowXmlParser parser = new StackoverflowXmlParser();
        byte[] bytes = content.getBytes();
        List<StackoverflowXmlParser.Entry> entries = parser.parse(new ByteArrayInputStream(bytes));
        StringBuilder stringBuilder = new StringBuilder();
        for (StackoverflowXmlParser.Entry entry : entries) {
            stringBuilder.append("<p><a href='");
            stringBuilder.append(entry.link);
            stringBuilder.append("'>" + entry.title + "</a></p>");
            if (showSummaryPreference) {
                stringBuilder.append(entry.summary);
            }
        }
        return stringBuilder.toString();
    }

    private String downloadUrl(String stringUrl) throws IOException {
        InputStream inputStream = null;
        try {
            java.net.URL url = new URL(stringUrl);
            HttpURLConnection conn = (HttpURLConnection)url.openConnection();
            conn.setReadTimeout(60000);
            conn.setConnectTimeout(60000);
            conn.setRequestMethod("GET");
            conn.setDoInput(true);
            conn.connect();
            int response = conn.getResponseCode();
            Log.d(TAG, "response Code: " + response);
            inputStream = conn.getInputStream();
            String result = readIt(inputStream);
            return result;
        } finally {
            if (inputStream != null) {
                inputStream.close();
            }
        }
    }

    private String readIt(InputStream inputStream) throws IOException {
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
