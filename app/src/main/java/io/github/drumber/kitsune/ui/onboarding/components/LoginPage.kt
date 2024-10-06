package io.github.drumber.kitsune.ui.onboarding.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import io.github.drumber.kitsune.ui.theme.KitsuneTheme

@Composable
fun LoginPage(modifier: Modifier = Modifier) {
    Column(
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier.fillMaxSize()
    ) {
        Text("Login!")
        Button(onClick = {}) {
            Text("Login")
        }
    }
}

@Preview(showBackground = true)
@Composable
fun LoginPagePreview() {
    KitsuneTheme {
        LoginPage()
    }
}
