package com.example.cacciaaltesoro.utils.nfc

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.nfc.NfcAdapter
import android.nfc.Tag
import android.os.Bundle
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.platform.LocalContext

fun Context.findActivity() : Activity? = when (this) {
    is Activity -> this
    is ContextWrapper -> baseContext.findActivity()
    else -> null
}

@Composable
fun NfcReaderLifecycle(
    isActive: Boolean,
    onTagDiscovered: (Tag) -> Unit
) {
    val context = LocalContext.current
    val currentOnTagDiscovered by rememberUpdatedState(onTagDiscovered)

    DisposableEffect(isActive, context) {
        val activity = context.findActivity()
        val nfcAdapter = activity?.let { NfcAdapter.getDefaultAdapter(it) }

        if (isActive && activity != null && nfcAdapter != null) {
            val options = Bundle().apply {
                putInt(NfcAdapter.EXTRA_READER_PRESENCE_CHECK_DELAY, 250)
            }
            nfcAdapter.enableReaderMode(
                activity,
                { tag -> currentOnTagDiscovered(tag) },
                NfcAdapter.FLAG_READER_NFC_A or
                        NfcAdapter.FLAG_READER_NFC_B or
                        NfcAdapter.FLAG_READER_NFC_F or
                        NfcAdapter.FLAG_READER_NFC_V or
                        NfcAdapter.FLAG_READER_NFC_BARCODE,
                options
            )
        }

        onDispose {
            if (isActive && activity != null && nfcAdapter != null) {
                nfcAdapter.disableReaderMode(activity)
            }
        }
    }
}