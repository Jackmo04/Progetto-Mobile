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
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner

fun Context.findActivity() : Activity? = when (this) {
    is Activity -> this
    is ContextWrapper -> baseContext.findActivity()
    else -> null
}

@Composable
fun NfcReaderLifecycle(
    isActive: Boolean,
    onTagDiscovered: (Tag) -> Unit,
    onNfcDisabled: () -> Unit
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    val currentOnTagDiscovered by rememberUpdatedState(onTagDiscovered)
    val currentOnNfcDisabled by rememberUpdatedState(onNfcDisabled)

    DisposableEffect(isActive, context, lifecycleOwner) {
        val activity = context.findActivity()
        val nfcAdapter = activity?.let { NfcAdapter.getDefaultAdapter(it) }

        fun checkNfcStatus() {
            if (isActive && nfcAdapter != null && !nfcAdapter.isEnabled) {
                currentOnNfcDisabled()
            }
        }

        checkNfcStatus()

        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                checkNfcStatus()

                if (isActive && activity != null && nfcAdapter != null && nfcAdapter.isEnabled) {
                    val options = Bundle().apply {
                        putInt(NfcAdapter.EXTRA_READER_PRESENCE_CHECK_DELAY, 250)
                    }
                    nfcAdapter.enableReaderMode(
                        activity,
                        { tag -> currentOnTagDiscovered(tag) },
                        NfcAdapter.FLAG_READER_NFC_A or NfcAdapter.FLAG_READER_NFC_B,
                        options
                    )
                }
            } else if (event == Lifecycle.Event.ON_PAUSE) {
                if (isActive && activity != null && nfcAdapter != null) {
                    nfcAdapter.disableReaderMode(activity)
                }
            }
        }

        lifecycleOwner.lifecycle.addObserver(observer)

        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
            if (isActive && activity != null && nfcAdapter != null) {
                nfcAdapter.disableReaderMode(activity)
            }
        }
    }
}