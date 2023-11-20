package ru.innopolis.moperator.webapp

import android.content.Context
import android.webkit.JavascriptInterface
import android.widget.Toast
import ru.innopolis.moperator.MoperatorApplication

class WebToAndroidCommunication(
    private val mContext: Context,
) {
    @JavascriptInterface
    fun showToast(toast: String) {
        Toast.makeText(mContext, toast, Toast.LENGTH_SHORT).show()
    }

    @JavascriptInterface
    fun getLocation(): String {
        return (mContext.applicationContext as MoperatorApplication).locationTracker
            .location.toString()
    }

    @JavascriptInterface
    fun setToken(token: String) {
        (mContext.applicationContext as MoperatorApplication).notificator.setToken(token)
    }
}
