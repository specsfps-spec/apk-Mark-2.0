package com.marklv.jarvis;

import android.Manifest;
import android.app.Activity;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.webkit.PermissionRequest;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceRequest;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.webkit.WebSettings;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.view.Gravity;
import android.view.inputmethod.InputMethodManager;
import android.content.Context;
import android.net.Uri;
import android.content.Intent;

public class MainActivity extends Activity {
    private static final String PREFS = "marklv_settings";
    private static final String KEY_URL = "server_url";
    private LinearLayout root;
    private WebView webView;
    private EditText urlInput;
    private PermissionRequest pendingPermissionRequest;

    @Override public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setStatusBarColor(Color.rgb(7, 9, 15));
        getWindow().setNavigationBarColor(Color.rgb(7, 9, 15));
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
        showConnectScreen();
    }

    private void showConnectScreen() {
        root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setGravity(Gravity.CENTER_HORIZONTAL);
        root.setPadding(dp(24), dp(36), dp(24), dp(24));
        root.setBackgroundColor(Color.rgb(7, 9, 15));

        TextView logo = new TextView(this);
        logo.setText("MARK LV  /  JARVIS");
        logo.setTextColor(Color.rgb(120, 130, 255));
        logo.setTextSize(22);
        logo.setGravity(Gravity.CENTER);
        logo.setLetterSpacing(0.12f);
        root.addView(logo, matchWrap());

        TextView subtitle = new TextView(this);
        subtitle.setText("CONTROLE REMOTO • ANDROID");
        subtitle.setTextColor(Color.LTGRAY);
        subtitle.setTextSize(11);
        subtitle.setGravity(Gravity.CENTER);
        LinearLayout.LayoutParams sp = matchWrap(); sp.topMargin = dp(10); sp.bottomMargin = dp(30);
        root.addView(subtitle, sp);

        TextView label = new TextView(this);
        label.setText("Endereço do painel no computador");
        label.setTextColor(Color.WHITE); label.setTextSize(14);
        root.addView(label, matchWrap());

        urlInput = new EditText(this);
        urlInput.setSingleLine(true);
        urlInput.setTextColor(Color.WHITE);
        urlInput.setHintTextColor(Color.GRAY);
        urlInput.setHint("http://192.168.1.100:8000");
        urlInput.setText(getPreferences(MODE_PRIVATE).getString(KEY_URL, ""));
        urlInput.setInputType( android.text.InputType.TYPE_CLASS_TEXT | android.text.InputType.TYPE_TEXT_VARIATION_URI );
        urlInput.setPadding(dp(14), dp(12), dp(14), dp(12));
        urlInput.setBackgroundTintList(android.content.res.ColorStateList.valueOf(Color.rgb(99,102,241)));
        LinearLayout.LayoutParams ip = matchWrap(); ip.topMargin = dp(10); ip.bottomMargin = dp(16);
        root.addView(urlInput, ip);

        Button connect = new Button(this);
        connect.setText("CONECTAR À JARVIS");
        connect.setTextColor(Color.WHITE);
        connect.setBackgroundTintList(android.content.res.ColorStateList.valueOf(Color.rgb(79,70,229)));
        root.addView(connect, matchWrap());
        connect.setOnClickListener(v -> connectToServer());

        TextView help = new TextView(this);
        help.setText("Use o celular e o PC na mesma rede Wi-Fi. Inicie a JARVIS no computador e informe o IP mostrado no painel, usando a porta 8000. Se o painel estiver configurado com HTTPS, informe https:// e a porta correspondente.");
        help.setTextColor(Color.rgb(160, 168, 185)); help.setTextSize(13);
        help.setGravity(Gravity.CENTER); help.setPadding(0, dp(24), 0, 0);
        root.addView(help, matchWrap());
        setContentView(root);
    }

    private void connectToServer() {
        String url = urlInput.getText().toString().trim();
        if (url.isEmpty()) { urlInput.setError("Informe o endereço do painel"); return; }
        if (!url.startsWith("http://") && !url.startsWith("https://")) url = "http://" + url;
        if (url.endsWith("/")) url = url.substring(0, url.length() - 1);
        getPreferences(MODE_PRIVATE).edit().putString(KEY_URL, url).apply();
        ((InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE)).hideSoftInputFromWindow(urlInput.getWindowToken(), 0);
        showWebView(url);
    }

    private void showWebView(String url) {
        webView = new WebView(this);
        WebSettings s = webView.getSettings();
        s.setJavaScriptEnabled(true);
        s.setDomStorageEnabled(true);
        s.setDatabaseEnabled(true);
        s.setMediaPlaybackRequiresUserGesture(false);
        s.setAllowFileAccess(false);
        s.setMixedContentMode(WebSettings.MIXED_CONTENT_COMPATIBILITY_MODE);
        webView.setBackgroundColor(Color.rgb(7,9,15));
        webView.setWebViewClient(new WebViewClient() {
            @Override public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
                Uri uri = request.getUrl();
                String scheme = uri.getScheme();
                if ("http".equals(scheme) || "https".equals(scheme)) return false;
                try { startActivity(new Intent(Intent.ACTION_VIEW, uri)); } catch (Exception ignored) {}
                return true;
            }
        });
        webView.setWebChromeClient(new WebChromeClient() {
            @Override public void onPermissionRequest(PermissionRequest request) {
                runOnUiThread(() -> {
                    boolean hasMic = Build.VERSION.SDK_INT < 23 || checkSelfPermission(Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED;
                    if (hasMic) request.grant(request.getResources());
                    else { pendingPermissionRequest = request; if (Build.VERSION.SDK_INT >= 23) requestPermissions(new String[]{Manifest.permission.RECORD_AUDIO, Manifest.permission.CAMERA}, 41); }
                });
            }
        });
        setContentView(webView);
        webView.loadUrl(url);
    }

    @Override public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 41 && pendingPermissionRequest != null) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) pendingPermissionRequest.grant(pendingPermissionRequest.getResources());
            else pendingPermissionRequest.deny();
            pendingPermissionRequest = null;
        }
    }

    @Override public void onBackPressed() {
        if (webView != null && webView.canGoBack()) webView.goBack();
        else showConnectScreen();
    }

    private LinearLayout.LayoutParams matchWrap() { return new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT); }
    private int dp(int value) { return (int)(value * getResources().getDisplayMetrics().density + 0.5f); }
}
