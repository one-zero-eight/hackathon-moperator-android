package ru.innopolis.moperator

import android.app.Application
import android.webkit.WebView
import ru.innopolis.moperator.domain.LocationTracker
import ru.innopolis.moperator.domain.NFCReader
import ru.innopolis.moperator.webapp.AndroidToWebCommunication
import ru.innopolis.moperator.webapp.WebToAndroidCommunication
import ru.innopolis.moperator.webapp.createWebView

class MoperatorApplication : Application() {
    private lateinit var mWebToAndroidCommunication: WebToAndroidCommunication
    private lateinit var mAndroidToWebCommunication: AndroidToWebCommunication
    private lateinit var mWebView: WebView
    private lateinit var mLocationTracker: LocationTracker
    private lateinit var mNFCReader: NFCReader

    override fun onCreate() {
        super.onCreate()
        mLocationTracker = LocationTracker(this)
        mWebToAndroidCommunication = WebToAndroidCommunication(this)
        mWebView = createWebView(this)
        mAndroidToWebCommunication = AndroidToWebCommunication(mWebView)
        mNFCReader = NFCReader(this)
    }

    // Getters
    val webView: WebView
        get() = mWebView

    val androidToWeb: AndroidToWebCommunication
        get() = mAndroidToWebCommunication

    val webToAndroid: WebToAndroidCommunication
        get() = mWebToAndroidCommunication

    val locationTracker: LocationTracker
        get() = mLocationTracker

    val nfcReader: NFCReader
        get() = mNFCReader
}
