package io.github.drumber.kitsune.ui.onboarding

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.asFlow
import com.chibatching.kotpref.livedata.asLiveData
import io.github.drumber.kitsune.preference.KitsunePref
import io.github.drumber.kitsune.ui.theme.KitsuneTheme

class OnboardingActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            val appTheme by KitsunePref.asLiveData(KitsunePref::appTheme)
                .asFlow()
                .collectAsState(initial = KitsunePref.appTheme)
            val useDynamicColorTheme by KitsunePref.asLiveData(KitsunePref::useDynamicColorTheme)
                .asFlow()
                .collectAsState(initial = KitsunePref.useDynamicColorTheme)
            val context = LocalContext.current
            context.setTheme(appTheme.themeRes)

            KitsuneTheme(dynamicColor = useDynamicColorTheme) {
                Scaffold( modifier = Modifier.fillMaxSize() ) { innerPadding ->
                    Greeting(
                        name = "Android",
                        modifier = Modifier.padding(innerPadding)
                    )
                }
            }
        }
    }
}

@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Column {
        Text(
            text = "Hello $name!",
            modifier = modifier.padding(bottom = 16.dp)
        )
        Button(onClick = {}, modifier) {
            Text("Click me!")
        }
    }
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    KitsuneTheme {
        Greeting("Android")
    }
}