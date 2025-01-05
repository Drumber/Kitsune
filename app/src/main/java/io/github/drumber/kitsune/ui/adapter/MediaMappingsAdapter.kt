package io.github.drumber.kitsune.ui.adapter

import android.app.SearchManager
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import com.bumptech.glide.Glide
import io.github.drumber.kitsune.R
import io.github.drumber.kitsune.data.presentation.model.mapping.Mapping
import io.github.drumber.kitsune.data.presentation.model.mapping.getExternalUrl
import io.github.drumber.kitsune.data.presentation.model.mapping.getSiteName
import io.github.drumber.kitsune.databinding.ItemMediaMappingBinding
import io.github.drumber.kitsune.util.extensions.showSomethingWrongToast
import io.github.drumber.kitsune.shared.logD
import io.github.drumber.kitsune.shared.logE

class MediaMappingsAdapter(
    private val context: Context,
    val dataSource: MutableList<Mapping>
) : BaseAdapter() {

    override fun getCount(): Int = dataSource.size

    override fun getItem(position: Int) = dataSource[position]

    override fun getItemId(position: Int): Long = position.toLong()

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        val binding = if (convertView == null) {
            ItemMediaMappingBinding.inflate(LayoutInflater.from(context), parent, false)
        } else {
            ItemMediaMappingBinding.bind(convertView)
        }

        val mapping = getItem(position)
        binding.tvSiteName.text = mapping.getSiteName() ?: mapping.externalSite ?: "?"
        binding.tvSiteUrl.text = mapping.getExternalUrl() ?: mapping.externalId ?: "-"

        mapping.getExternalUrl()?.let { url ->
            try {
                val domain = Uri.parse(url).host
                Glide.with(context)
                    .load("https://icons.duckduckgo.com/ip3/$domain.ico")
                    .placeholder(R.drawable.ic_website)
                    .into(binding.ivSiteIcon)
            } catch (e: Exception) {
                logD("Failed to load favicon for url: $url", e)
            }
        }

        binding.root.setOnClickListener {
            val url = mapping.getExternalUrl()
            if (url != null) {
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                try {
                    context.startActivity(intent)
                } catch (e: Exception) {
                    logE("Failed to open URL: $url", e)
                    context.showSomethingWrongToast()
                }
            } else {
                // do a web search
                val query = "${mapping.externalSite} ${mapping.externalId}"
                val intent = Intent(Intent.ACTION_WEB_SEARCH)
                intent.putExtra(SearchManager.QUERY, query)
                try {
                    context.startActivity(intent)
                } catch (e: Exception) {
                    logE("Failed to do a web search for: $query", e)
                    context.showSomethingWrongToast()
                }
            }
        }

        binding.root.setOnLongClickListener {
            val url = mapping.getExternalUrl() ?: mapping.externalId ?: return@setOnLongClickListener false

            // copy url to clipboard
            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            val clip = ClipData.newPlainText(mapping.getSiteName() ?: "URL", url)
            clipboard.setPrimaryClip(clip)
            return@setOnLongClickListener true
        }

        return binding.root
    }
}