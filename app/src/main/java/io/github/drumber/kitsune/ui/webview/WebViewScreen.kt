package io.github.drumber.kitsune.ui.webview

import android.graphics.Bitmap
import android.os.Bundle
import android.webkit.WebChromeClient
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.net.toUri
import io.github.drumber.kitsune.R
import io.github.drumber.kitsune.data.source.local.auth.model.LocalAccessToken

@Composable
fun WebViewScreen(
    initialUrl: String,
    savedInstanceState: Bundle?,
    getAccessToken: () -> LocalAccessToken?,
    onNavigateUp: () -> Unit,
    onWebViewReady: (WebView) -> Unit,
    openUrl: (String) -> Unit,
    copyToClipboard: (String, String) -> Unit,
    modifier: Modifier = Modifier
) {
    val pageTitle = remember { mutableStateOf<String?>(null) }
    val currentUrl = remember { mutableStateOf(initialUrl) }
    val isLoading = remember { mutableStateOf(savedInstanceState == null) }
    var showMenu by remember { mutableStateOf(false) }

    Scaffold(
        modifier = modifier,
        topBar = {
            WebViewTopAppBar(
                title = pageTitle.value,
                url = currentUrl.value,
                showMenu = showMenu,
                onMenuToggle = { showMenu = it },
                onNavigateUp = onNavigateUp,
                onOpenInBrowser = { openUrl(currentUrl.value) },
                onCopyUrl = { copyToClipboard("URL", currentUrl.value) }
            )
        }
    ) { paddingValues ->
        Box(modifier = Modifier.fillMaxSize().padding(paddingValues)) {
            AndroidView(
                factory = { context ->
                    WebView(context).apply {
                        settings.javaScriptEnabled = true
                        settings.domStorageEnabled = true
                        webChromeClient = object : WebChromeClient() {
                            override fun onReceivedTitle(view: WebView?, title: String?) {
                                super.onReceivedTitle(view, title)
                                pageTitle.value = title
                            }
                        }
                        webViewClient = KitsuWebViewClient(
                            getAccessToken = getAccessToken,
                            openUrl = openUrl,
                            onPageStarted = { url ->
                                isLoading.value = true
                                currentUrl.value = url ?: currentUrl.value
                            },
                            onPageFinished = { isLoading.value = false },
                            onUrlChanged = { url -> currentUrl.value = url ?: currentUrl.value }
                        )
                        if (savedInstanceState != null) {
                            restoreState(savedInstanceState)
                        } else {
                            loadUrl(initialUrl)
                        }
                        onWebViewReady(this)
                    }
                },
                modifier = Modifier.fillMaxSize()
            )
            if (isLoading.value) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun WebViewTopAppBar(
    title: String?,
    url: String,
    showMenu: Boolean,
    onMenuToggle: (Boolean) -> Unit,
    onNavigateUp: () -> Unit,
    onOpenInBrowser: () -> Unit,
    onCopyUrl: () -> Unit
) {
    TopAppBar(
        title = {
            Column {
                Text(
                    text = title ?: "",
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    style = MaterialTheme.typography.titleMedium
                )
                Text(
                    text = url,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    style = MaterialTheme.typography.titleSmall
                )
            }
        },
        navigationIcon = {
            IconButton(onClick = onNavigateUp) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = stringResource(R.string.action_close)
                )
            }
        },
        actions = {
            Box {
                IconButton(onClick = { onMenuToggle(true) }) {
                    Icon(
                        imageVector = Icons.Default.MoreVert,
                        contentDescription = stringResource(R.string.action_more_options)
                    )
                }
                DropdownMenu(expanded = showMenu, onDismissRequest = { onMenuToggle(false) }) {
                    DropdownMenuItem(
                        text = { Text(stringResource(R.string.action_open_in_browser)) },
                        onClick = { onOpenInBrowser(); onMenuToggle(false) }
                    )
                    DropdownMenuItem(
                        text = { Text(stringResource(R.string.action_copy_url)) },
                        onClick = { onCopyUrl(); onMenuToggle(false) }
                    )
                }
            }
        }
    )
}

private val validKitsuHosts = listOf("kitsu.app", "kitsu.io")

private class KitsuWebViewClient(
    private val getAccessToken: () -> LocalAccessToken?,
    private val openUrl: (String) -> Unit,
    private val onPageStarted: (String?) -> Unit,
    private val onPageFinished: () -> Unit,
    private val onUrlChanged: (String?) -> Unit
) : WebViewClient() {

    override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
        super.onPageStarted(view, url, favicon)
        if (url?.toUri()?.host in validKitsuHosts) {
            val accessToken = getAccessToken()
            if (accessToken != null) {
                view?.evaluateJavascript(getAccessTokenInjectionCode(accessToken), null)
            }
        }
        onPageStarted(url)
    }

    override fun onPageFinished(view: WebView?, url: String?) {
        super.onPageFinished(view, url)
        onPageFinished()
    }

    override fun doUpdateVisitedHistory(view: WebView?, url: String?, isReload: Boolean) {
        super.doUpdateVisitedHistory(view, url, isReload)
        onUrlChanged(url)
    }

    override fun shouldOverrideUrlLoading(view: WebView?, request: WebResourceRequest?): Boolean {
        if (request?.url?.host in validKitsuHosts) {
            return false
        }
        request?.url?.toString()?.let { openUrl(it) }
        return true
    }

    private fun getAccessTokenInjectionCode(accessToken: LocalAccessToken): String {
        val expiresAt = (accessToken.createdAt + accessToken.expiresIn) * 1000
        val session = buildString {
            append("{\"authenticated\":{\"authenticator\":\"authenticator:oauth2\"")
            append(",\"access_token\":\"${accessToken.accessToken}\"")
            append(",\"token_type\":\"Bearer\"")
            append(",\"expires_in\":${accessToken.expiresIn}")
            append(",\"refresh_token\":\"${accessToken.refreshToken}\"")
            append(",\"scope\":\"public\"")
            append(",\"created_at\":${accessToken.createdAt}")
            append(",\"expires_at\":$expiresAt}}")
        }
        return "window.localStorage.setItem(\"ember_simple_auth:session\", '$session');"
    }
}
