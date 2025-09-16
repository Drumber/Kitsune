package io.github.drumber.kitsune.ui.webview

import android.content.Intent
import android.graphics.Bitmap
import android.os.Bundle
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.webkit.WebChromeClient
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.activity.addCallback
import androidx.core.net.toUri
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import io.github.drumber.kitsune.R
import io.github.drumber.kitsune.data.repository.AccessTokenRepository
import io.github.drumber.kitsune.data.source.local.auth.model.LocalAccessToken
import io.github.drumber.kitsune.databinding.FragmentWebViewBinding
import io.github.drumber.kitsune.util.extensions.copyToClipboard
import io.github.drumber.kitsune.util.extensions.openUrl
import io.github.drumber.kitsune.util.ui.initPaddingWindowInsetsListener
import io.github.drumber.kitsune.util.ui.initWindowInsetsListener
import org.koin.android.ext.android.inject

class WebViewFragment : Fragment() {

    private var _binding: FragmentWebViewBinding? = null
    private val binding get() = _binding!!

    private val args: WebViewFragmentArgs by navArgs()

    private val accessTokenRepository: AccessTokenRepository by inject()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentWebViewBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.toolbar.apply {
            initWindowInsetsListener(false)
            subtitle = args.url
            setNavigationOnClickListener {
                findNavController().navigateUp()
            }
            setOnMenuItemClickListener(::onToolbarMenuItemClicked)
        }

        binding.webViewWrapper.initPaddingWindowInsetsListener(
            left = true,
            right = true,
            consume = false
        )

        binding.webView.apply {
            settings.javaScriptEnabled = true
            settings.domStorageEnabled = true
            webViewClient = KitsuWebViewClient(accessTokenRepository::getAccessToken)
            webChromeClient = KitsuWebChromeClient()
            if (savedInstanceState == null) {
                loadUrl(args.url)
            } else {
                restoreState(savedInstanceState)
            }
        }

        requireActivity().onBackPressedDispatcher.addCallback(this) {
            if (binding.webView.canGoBack()) {
                binding.webView.goBack()
            } else {
                findNavController().navigateUp()
            }
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        binding.webView.saveState(outState)
    }

    private fun onToolbarMenuItemClicked(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.menu_open_in_browser -> {
                binding.webView.url?.let { openUrl(it) }
            }

            R.id.menu_copy_url -> {
                binding.webView.url?.let { copyToClipboard("URL", it) }
            }

            else -> return false
        }
        return true
    }

    inner class KitsuWebChromeClient : WebChromeClient() {
        override fun onReceivedTitle(view: WebView?, title: String?) {
            super.onReceivedTitle(view, title)
            binding.toolbar.title = title
        }
    }

    inner class KitsuWebViewClient(
        private val getAccessToken: () -> LocalAccessToken?
    ) : WebViewClient() {

        override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
            super.onPageStarted(view, url, favicon)

            if (url?.toUri()?.host in validKitsuHosts) {
                val accessToken = getAccessToken()
                if (accessToken != null) {
                    view?.evaluateJavascript(getAccessTokenInjectionCode(accessToken), null)
                }
            }
            binding.toolbar.subtitle = url
        }

        override fun onPageFinished(view: WebView?, url: String?) {
            super.onPageFinished(view, url)
            binding.loadingIndicator.isVisible = false
        }

        override fun doUpdateVisitedHistory(view: WebView?, url: String?, isReload: Boolean) {
            super.doUpdateVisitedHistory(view, url, isReload)
            binding.toolbar.subtitle = url
        }

        override fun shouldOverrideUrlLoading(
            view: WebView?,
            request: WebResourceRequest?
        ): Boolean {
            if (request?.url?.host in validKitsuHosts) {
                return false
            }
            val browseIntent = Intent(Intent.ACTION_VIEW, request?.url)
            startActivity(browseIntent)
            return true
        }

        private fun getAccessTokenInjectionCode(accessToken: LocalAccessToken): String {
            val expiresAt = (accessToken.createdAt + accessToken.expiresIn) * 1000
            val emberSession =
                "{\"authenticated\":{\"authenticator\":\"authenticator:oauth2\",\"access_token\":\"${accessToken.accessToken}\",\"token_type\":\"Bearer\",\"expires_in\":${accessToken.expiresIn},\"refresh_token\":\"${accessToken.refreshToken}\",\"scope\":\"public\",\"created_at\":${accessToken.createdAt},\"expires_at\":${expiresAt}}}"
            return "window.localStorage.setItem(\"ember_simple_auth:session\", '$emberSession');"
        }
    }

    companion object {
        private val validKitsuHosts = listOf("kitsu.app", "kitsu.io")
    }
}