package org.hamereberhan.poe;

import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.view.View;
import android.content.Intent;
import android.net.Uri;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.webkit.JavascriptInterface;
import com.airbnb.lottie.LottieAnimationView;

public class MainActivity extends AppCompatActivity {

    private WebView webView;
    private LottieAnimationView loadingAnimationView;
    private static final int CAMERA_PERMISSION_REQUEST_CODE = 101;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        webView = findViewById(R.id.webview);
        loadingAnimationView = findViewById(R.id.loadingAnimationView);

        webView.setWebViewClient(new MyBrowser());
        webView.getSettings().setJavaScriptEnabled(true);

        // Set up WebChromeClient to handle JavaScript alerts and permission requests
        webView.setWebChromeClient(new MyChromeClient());

        // Inject JavaScript interface
        webView.addJavascriptInterface(new WebAppInterface(), "Android");

        // Enable caching
        webView.getSettings().setCacheMode(WebSettings.LOAD_DEFAULT);
        webView.getSettings().setDatabaseEnabled(true); // Enable app cache without parameters
        // Set app cache path
        webView.getSettings().setDatabasePath(getApplicationContext().getCacheDir().getAbsolutePath());

        // Print WebView settings for debugging
        WebSettings webSettings = webView.getSettings();
        String appCacheStatus = "Enabled"; // App cache is enabled
        String settings = "WebView Settings: \n" +
                "JavaScriptEnabled: " + webSettings.getJavaScriptEnabled() + "\n" +
                "AppCacheStatus: " + appCacheStatus + "\n" +
                "CacheMode: " + webSettings.getCacheMode();
        System.out.println(settings);

        // Load initial URL
        loadInitialURL("https://event.hamereberhan.org/scancode/");
    }

    String url;

    public void test(View view) {
        String url = null;
        if (view.getId() == R.id.qrscan) {
            url = "https://event.hamereberhan.org/scancode/";
        } else if (view.getId() == R.id.dashbord) {
            url = "https://event.hamereberhan.org/dash/";
        }
        loadInitialURL(url);
        webView.loadUrl(url);
        webView.setVisibility(View.GONE); // Hide WebView until fully loaded
        loadingAnimationView.setVisibility(View.VISIBLE); // Show loading animation
    }

    private void loadInitialURL(String url) {
        if (url != null && !url.isEmpty()) {
            webView.loadUrl(url);
            webView.setVisibility(View.GONE); // Hide WebView until fully loaded
            loadingAnimationView.setVisibility(View.VISIBLE); // Show loading animation
        }
    }


    private class MyBrowser extends WebViewClient {
        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            // Load all URLs in the WebView
            view.loadUrl(url);
            return true;
        }

        @Override
        public void onPageFinished(WebView view, String url) {
            // When the page is fully loaded, hide the loading animation
            loadingAnimationView.setVisibility(View.GONE);
            // Make the WebView visible
            webView.setVisibility(View.VISIBLE);
        }
    }

    private class MyChromeClient extends WebChromeClient {
        @Override
        public boolean onJsAlert(WebView view, String url, String message, final android.webkit.JsResult result) {
            // Handle JavaScript alert
            new AlertDialog.Builder(MainActivity.this)
                    .setTitle("JavaScript Alert")
                    .setMessage(message)
                    .setPositiveButton(android.R.string.ok,
                            new AlertDialog.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {
                                    // Confirm the alert
                                    result.confirm();
                                }
                            })
                    .setCancelable(false)
                    .create()
                    .show();

            return true;
        }

        @Override
        public void onPermissionRequest(final android.webkit.PermissionRequest request) {
            // Handle permission request
            MainActivity.this.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    request.grant(request.getResources());
                }
            });
        }
    }

    // JavaScript interface for handling camera permission requests
    public class WebAppInterface {
        @JavascriptInterface
        public void requestCameraPermission() {
            // Request camera permission
            if (checkSelfPermission(android.Manifest.permission.CAMERA) != android.content.pm.PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{android.Manifest.permission.CAMERA}, CAMERA_PERMISSION_REQUEST_CODE);
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == CAMERA_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == android.content.pm.PackageManager.PERMISSION_GRANTED) {
                // Camera permission granted
            } else {
                // Camera permission denied
            }
        }
    }
}