package io.github.drumber.kitsune.ui.webview

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.WebView
import androidx.activity.addCallback
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import io.github.drumber.kitsune.data.repository.AccessTokenRepository
import io.github.drumber.kitsune.ui.compose.composeView
import io.github.drumber.kitsune.util.extensions.copyToClipboard
import io.github.drumber.kitsune.util.extensions.openUrl
import org.koin.android.ext.android.inject

class WebViewFragment : Fragment() {

    private val args: WebViewFragmentArgs by navArgs()
    private val accessTokenRepository: AccessTokenRepository by inject()

    /** Retained across recompositions to enable save-state and back-press handling. */
    private var webView: WebView? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View = composeView {
        WebViewScreen(
            initialUrl = args.url,
            savedInstanceState = savedInstanceState,
            getAccessToken = accessTokenRepository::getAccessToken,
            onNavigateUp = { findNavController().navigateUp() },
            onWebViewReady = { wv -> webView = wv },
            openUrl = { url -> openUrl(url) },
            copyToClipboard = { label, text -> copyToClipboard(label, text) }
        )
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        // Back press: navigate within WebView history before popping the Fragment.
        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner) {
            val wv = webView
            if (wv != null && wv.canGoBack()) {
                wv.goBack()
            } else {
                findNavController().navigateUp()
            }
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        webView?.saveState(outState)
    }

    override fun onDestroyView() {
        webView = null
        super.onDestroyView()
    }
}