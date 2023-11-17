package ru.innopolis.moperator.webapp

import android.webkit.WebView
import androidx.compose.runtime.Composable
import androidx.compose.ui.viewinterop.AndroidView

@Composable
fun WebViewContent(webView: WebView) {
    AndroidView(factory = {
        webView
    }, update = {
        it.loadUrl("https://moperator.innohassle.ru/")
    })
}

