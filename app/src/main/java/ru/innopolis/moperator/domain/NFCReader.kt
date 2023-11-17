package ru.innopolis.moperator.domain

import android.content.Context
import android.content.Intent
import android.nfc.NfcAdapter
import android.nfc.Tag
import android.nfc.tech.MifareClassic
import android.nfc.tech.NfcA
import android.os.Build
import android.util.Log
import ru.innopolis.moperator.MoperatorApplication

fun ByteArray.toHexString() = joinToString("") { "%02x".format(it) }

class MoperatorNFCTag(
    private val id: String?,
    private val atqa: String?,
    private val manufacturerId: String?
) {

    data class Builder(
        var id: String? = null,
        var atqa: String? = null,
        var manufacturerId: String? = null
    ) {
        fun id(id: String) = apply { this.id = id }
        fun atqa(atqa: String) = apply {
            this.atqa = atqa
        }

        fun manufacturerId(manufacturerId: String) = apply { this.manufacturerId = manufacturerId }
        fun build() = MoperatorNFCTag(id, atqa, manufacturerId)
    }

    fun getManufacturerId(): String? {
        return manufacturerId
    }
}

class NFCReader(
    private val mContext: Context,
) {
    fun onIntent(intent: Intent) {
        Log.d("NFC", "NFC intent received")

        when (intent.action) {
            NfcAdapter.ACTION_TECH_DISCOVERED -> onActionTechDiscoveredIntent(intent)
            else -> Log.d("NFC", "NFC intent action is ${intent.action} and is not handled")
        }
    }

    private fun onActionTechDiscoveredIntent(intent: Intent) {
        Log.d("NFC", "NFC intent action is ACTION_TECH_DISCOVERED")
        val tag: Tag? = (
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    intent.getParcelableExtra(NfcAdapter.EXTRA_TAG, Tag::class.java)
                } else {
                    @Suppress("DEPRECATION")
                    intent.getParcelableExtra(NfcAdapter.EXTRA_TAG)
                })

        if (tag == null) {
            Log.d("NFC", "NFC tag is null")
            return
        }

        val builder = MoperatorNFCTag.Builder()

        Log.d(
            "NFC", "NFC tag:\n" +
                    tag.toString() + "\n" +
                    "id: ${tag.id.toHexString()}\n" +
                    "techList: ${tag.techList.contentToString()}\n"
        )
        builder.id(tag.id.toHexString())

        // Check tags
        if (tag.techList.contains("android.nfc.tech.NfcA")) {
            val nfcA = NfcA.get(tag)
            Log.d(
                "NFC", "NFC tag NfcA:\n" +
                        nfcA.toString() + "\n" +
                        "atqa: ${nfcA.atqa.toHexString()}\n" +
                        "sak: ${nfcA.sak}\n" +
                        "maxTransceiveLength: ${nfcA.maxTransceiveLength}"
            )
            builder.atqa(nfcA.atqa.toHexString())
        }

        if (tag.techList.contains("android.nfc.tech.MifareClassic")) {
            val mifareClassic = MifareClassic.get(tag)
            // Log MifareClassic info in one message
            Log.d(
                "NFC",
                "NFC tag MifareClassic:\n" +
                        mifareClassic.toString() + "\n" +
                        "type: ${mifareClassic.type}\n" +
                        "size: ${mifareClassic.size}\n" +
                        "maxTransceiveLength: ${mifareClassic.maxTransceiveLength}\n" +
                        "sectorCount: ${mifareClassic.sectorCount}\n" +
                        "blockCount: ${mifareClassic.blockCount}"
            )

            readBlocksWithDefaultKey(mifareClassic, builder)
        }
        val nfcTag = builder.build()
        val manufacturerId = nfcTag.getManufacturerId()

        if (manufacturerId != null) {
            Log.d("NFC", "NFC tag manufacturerId: ${nfcTag.getManufacturerId()}")
            (mContext.applicationContext as MoperatorApplication)
                .androidToWeb.onTagScanned(manufacturerId)
        }
    }

    private fun readBlocksWithDefaultKey(
        mifareClassic: MifareClassic,
        builder: MoperatorNFCTag.Builder
    ) {
        // Connect to tag
        mifareClassic.connect()
        // Authenticate sector with default key
        if (!mifareClassic.authenticateSectorWithKeyA(0, MifareClassic.KEY_DEFAULT)) {
            Log.d("NFC", "NFC tag MifareClassic: sector 0 authentication with default key failed")
            return
        }
        Log.d("NFC", "NFC tag MifareClassic: sector 0 authenticated with default key")
        // Read first block of first sector
        val blockIndex = mifareClassic.sectorToBlock(0)
        val block = mifareClassic.readBlock(blockIndex)
        // if empty block, skip it
        if (block.all { it == 0.toByte() }) {
            Log.d("NFC", "NFC tag MifareClassic: sector 0 block $blockIndex is empty")
            return
        }
        // if not empty block, parse it
        Log.d("NFC", "NFC tag MifareClassic: sector 0 block $blockIndex: ${block.toHexString()}")
        builder.manufacturerId(block.toHexString())
        // Disconnect from tag
        mifareClassic.close()
    }
}