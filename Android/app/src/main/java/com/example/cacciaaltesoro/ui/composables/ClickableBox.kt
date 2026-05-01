package com.example.cacciaaltesoro.ui.composables

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color


@Composable
fun ClickableBox(onClick: () -> Unit, modifier: Modifier = Modifier, content: @Composable (BoxScope.() -> Unit)) {
    Box(
        modifier = modifier
    ) {
        content()

        Box(
            modifier = Modifier
                .matchParentSize()
                .background(Color.Transparent)
                .clickable { onClick() }
        )
    }
}