package com.infmme.githubtracker.app;

import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;


public class LoginActivity extends ActionBarActivity {

    private static String OAUTH_URL = "https://github.com/login/oauth/authorize";
    private static String OAUTH_ACCESS_TOKEN_URL = "https://github.com/login/oauth/access_token";

    private static final String CLIENT_ID = "db8b73bb88031a58b9cf";
    private static final String CLIENT_SECRET = "f27086c9422ffa2902866bc7fb7e72fe93a736c5";
    private static final String SCOPES = "notifications,repo,user";
    private static final String CALLBACK_URL = "https://github.com/infm/GitHubTracker";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        String url = OAUTH_URL + "?client_id=" + CLIENT_ID +
                "&scope=" + SCOPES;

        WebView webview = (WebView) findViewById(R.id.loginWebView);
        webview.getSettings().setJavaScriptEnabled(true);
        webview.setWebViewClient(new WebViewClient() {
            public void onPageStarted(WebView view, String url, Bitmap favicon) {
                String accessTokenFragment = "access_token=";
                String accessCodeFragment = "code=";

                // We hijack the GET request to extract the OAuth parameters

                if (url.contains(accessTokenFragment)) {
                    // the GET request contains directly the token
                    String accessToken = url.substring(url.indexOf(accessTokenFragment) +
                                                               accessTokenFragment.length());
                    //TokenStorer.setAccessToken(accessToken);
                    Log.d("Login", "accessToken: " + accessToken);
                } else if (url.contains(accessCodeFragment)) {
                    // the GET request contains an authorization code
                    String accessCode = url.substring(url.indexOf(accessCodeFragment) +
                                                              accessCodeFragment.length());
                    //TokenStorer.setAccessCode(accessCode);
                    Log.d("Login", "accessCode: " + accessCode);
/*
                    String query = "client_id=" + CLIENT_ID +
                            "&client_secret=" + CLIENT_SECRET +
                            "&" + accessCode +
                            "&redirect_uri=" + CALLBACK_URL;
                    view.postUrl(OAUTH_ACCESS_TOKEN_URL, query.getBytes());
*/

                    // Executing POST request
                    try {
                        String content = new AccessTokenGetter().execute(accessCode).get();
                        Log.d("Login", "content is " + content);
                        String accessToken;
                        if (content.contains(accessTokenFragment)) {
                            accessToken = content.substring(accessTokenFragment.length());
                        } else {
                            Log.e("Login", "Access token not found");
                            accessToken = "invalid";
                        }
                        PreferenceManager.getDefaultSharedPreferences(LoginActivity.this)
                                         .edit()
                                         .putString("accessToken", accessToken)
                                         .apply();
                        finish();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    } catch (ExecutionException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
        webview.loadUrl(url);
    }

    private static class AccessTokenGetter extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... params) {
            String accessCode = params[0];

            HttpClient httpclient = new DefaultHttpClient();
            HttpPost httppost = new HttpPost(OAUTH_ACCESS_TOKEN_URL);

            List<NameValuePair> resultPairs = new ArrayList<NameValuePair>(4);
            resultPairs.add(new BasicNameValuePair("client_id", CLIENT_ID));
            resultPairs.add(new BasicNameValuePair("client_secret", CLIENT_SECRET));
            resultPairs.add(new BasicNameValuePair("code", accessCode));
            resultPairs.add(new BasicNameValuePair("redirect_uri", CALLBACK_URL));

            String content = null;
            try {
                HttpResponse response;
                httppost.setEntity(new UrlEncodedFormEntity(resultPairs));
                response = httpclient.execute(httppost);

                // Get the response content
                String line;
                StringBuilder contentBuilder = new StringBuilder();
                BufferedReader rd = new BufferedReader(
                        new InputStreamReader(response.getEntity().getContent()));
                while ((line = rd.readLine()) != null) {
                    contentBuilder.append(line);
                }
                content = contentBuilder.toString();
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            } catch (ClientProtocolException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }

            return content;
        }
    }
}
