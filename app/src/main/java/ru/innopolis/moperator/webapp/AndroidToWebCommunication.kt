package ru.innopolis.moperator.webapp

import android.location.Location
import android.util.Log
import android.webkit.WebView

class AndroidToWebCommunication(
    private val webView: WebView
) {
    fun onLocationChanged(location: Location) {
        Log.d("AndroidToWeb", "Location changed: $location")
        val json =
            "{lat: ${location.latitude}, lng: ${location.longitude}, alt: ${location.altitude}, acc: ${location.accuracy}}"
        webView.evaluateJavascript("Web.onLocationChanged('${json})')", null)
    }

    fun onTagScanned(tag: String) {
        Log.d("AndroidToWeb", "Tag scanned: $tag")
        webView.evaluateJavascript("Web.onTagScanned('${tag}')", null)
    }
}