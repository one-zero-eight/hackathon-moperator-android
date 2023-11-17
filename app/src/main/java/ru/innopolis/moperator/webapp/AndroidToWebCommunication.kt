package ru.innopolis.moperator.webapp

import android.webkit.WebView
import android.location.Location
import android.util.Log

class AndroidToWebCommunication(
    private val webView: WebView
) {
    fun onLocationChanged(location: Location) {
        Log.d("Location", "Location changed: $location")
        val json =
            "{lat: ${location.latitude}, lng: ${location.longitude}, alt: ${location.altitude}, acc: ${location.accuracy}}"
        webView.evaluateJavascript("Web.onLocationChanged('${json})')", null)
    }
}