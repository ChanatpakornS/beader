package com.beader.core.designsystem.component

import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.beader.core.designsystem.theme.BeaderTheme

@Composable
fun BeaderButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
) {
    Button(onClick = onClick, modifier = modifier, enabled = enabled) {
        Text(text)
    }
}

@Preview
@Composable
private fun BeaderButtonPreview() {
    BeaderTheme {
        BeaderButton(text = "Continue", onClick = {})
    }
}
