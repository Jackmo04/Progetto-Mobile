package com.example.cacciaaltesoro.utils

import android.content.Context
import androidx.annotation.StringRes
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource

class StringResource(
    @StringRes val resourceId: Int,
    vararg val formatArgs: Any
) {
    @Composable
    fun asString(): String {
        return stringResource(resourceId, *formatArgs)
    }

    fun asString(context: Context): String {
        return context.getString(resourceId, *formatArgs)
    }
}