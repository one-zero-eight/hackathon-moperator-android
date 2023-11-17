package ru.innopolis.moperator

import android.content.Intent
import android.nfc.NfcAdapter
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.addCallback
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import ru.innopolis.moperator.webapp.WebViewContent


class MainActivity : ComponentActivity() {

    // need to register this anywhere before onCreateView, idealy as a field
    private val permissionRequester = registerForActivityResult(
        // you can use RequestPermission() contract if you only need 1 permission
        ActivityResultContracts.RequestMultiplePermissions()
    ) { map ->
        // If you requested 1 permission, change `map` to `isGranted`
        // Keys are permissions Strings, values are isGranted Booleans
        // An easy way to check if "any" permission was granted is map.containsValue(true)
        // You can use your own logic for multiple permissions,
        // but they have to follow the same checks here:

        for (entry in map.entries) {
            val permission = entry.key
            val isGranted = entry.value
            when {
                isGranted -> {
                    // permission granted; notify all observers
                    onPermissionGranted(permission)
                }

                else -> {
                    // permission denied, can't be requested again
                    // tell user why this permission is needed
                    // and take him to your app's info screen to manually change permissions
                    Log.d("Permission", "Permission denied: $permission")
                }
            }
        }
    }

    private fun onPermissionGranted(permission: String) {
        // permission granted; notify all observers
        when {
            permission == android.Manifest.permission.ACCESS_FINE_LOCATION || permission == android.Manifest.permission.ACCESS_COARSE_LOCATION -> {
                Log.d("Permission", "Location permission granted")
                // update LocationTracker
                (this.applicationContext as MoperatorApplication).locationTracker.setupLocationListeners()
            }
        }
    }

    private fun requestPermissions() {
        permissionRequester.launch(
            arrayOf(
                android.Manifest.permission.ACCESS_FINE_LOCATION,
                android.Manifest.permission.ACCESS_COARSE_LOCATION
            )
        )
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val webView = (this.applicationContext as MoperatorApplication).webView
        requestPermissions()

        onBackPressedDispatcher.addCallback {
            // Check whether there's history.
            if (webView.canGoBack()) {
                webView.goBack()
            } else {
                // If there is no history, close
                finish()
            }
        }

        setContent {
            WebViewContent(webView)
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)

        if (NfcAdapter.ACTION_TECH_DISCOVERED == intent.action ||
            NfcAdapter.ACTION_NDEF_DISCOVERED == intent.action ||
            NfcAdapter.ACTION_TAG_DISCOVERED == intent.action
        ) {
            (this.applicationContext as MoperatorApplication).nfcReader.onIntent(intent)
        }
    }

}