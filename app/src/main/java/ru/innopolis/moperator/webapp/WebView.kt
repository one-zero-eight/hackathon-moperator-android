package ru.innopolis.moperator.webapp

import android.annotation.SuppressLint
import android.content.Context
import android.util.Log
import android.webkit.ConsoleMessage
import android.webkit.WebChromeClient
import android.webkit.WebView
import androidx.webkit.WebViewAssetLoader
import ru.innopolis.moperator.MoperatorApplication

@SuppressLint("SetJavaScriptEnabled")
fun createWebView(
    context: Context,
): WebView {

    return WebView(context).apply {
        settings.apply {
            javaScriptEnabled = true
            domStorageEnabled = true
            databaseEnabled = true
        }
        layoutParams = android.view.ViewGroup.LayoutParams(
            android.view.ViewGroup.LayoutParams.MATCH_PARENT,
            android.view.ViewGroup.LayoutParams.MATCH_PARENT
        )

        webViewClient = LocalContentWebViewClient(
            WebViewAssetLoader.Builder()
                .setDomain("moperator.local")
                .addPathHandler("/assets/", WebViewAssetLoader.AssetsPathHandler(context))
                .addPathHandler("/res/", WebViewAssetLoader.ResourcesPathHandler(context))
                .build()
        )

        webChromeClient = object : WebChromeClient() {
            override fun onConsoleMessage(message: ConsoleMessage): Boolean {
                Log.d(
                    "WebViewContent", "${message.message()} -- From line " +
                            "${message.lineNumber()} of ${message.sourceId()}"
                )
                return true
            }
        }

        addJavascriptInterface(
            (context.applicationContext as MoperatorApplication).webToAndroid, "Android"
        )

        loadUrl("https://moperator.innohassle.ru/")
    }
}