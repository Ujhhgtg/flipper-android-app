package com.flipperdevices.wearable.sync.wear.impl.composable

import androidx.compose.foundation.focusable
import androidx.compose.foundation.gestures.scrollBy
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.input.rotary.onRotaryScrollEvent
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.wear.compose.material.Text
import com.flipperdevices.core.ui.theme.LocalTypography
import com.flipperdevices.wearable.core.ui.components.ComposableFlipperButton
import com.flipperdevices.wearable.sync.wear.impl.R
import com.google.android.horologist.compose.layout.fillMaxRectangle
import kotlinx.coroutines.launch

@Composable
fun ComposableFindPhone(
    onInstall: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val columnScrollState = rememberScrollState()
    val coroutineScope = rememberCoroutineScope()
    val focusRequester = remember { FocusRequester() }

    Column(
        modifier = modifier
            .verticalScroll(columnScrollState)
            .fillMaxRectangle()
            .onRotaryScrollEvent {
                coroutineScope.launch {
                    columnScrollState.scrollBy(it.verticalScrollPixels)
                }
                true
            }
            .focusRequester(focusRequester)
            .focusable(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = stringResource(id = R.string.phone_missing),
            style = LocalTypography.current.bodyM14
        )
        ComposableFlipperButton(
            modifier = Modifier.padding(top = 16.dp),
            text = stringResource(R.string.install_app),
            onClick = onInstall
        )
    }
    LaunchedEffect(Unit) {
        focusRequester.requestFocus()
    }
}
