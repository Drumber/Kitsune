package io.github.drumber.kitsune.util

import android.content.Intent
import android.net.Uri
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.widget.TooltipCompat
import androidx.core.view.isVisible
import androidx.core.widget.doOnTextChanged
import androidx.databinding.BindingAdapter
import at.blogc.android.views.ExpandableTextView
import com.bumptech.glide.Glide
import io.github.drumber.kitsune.R

object BindingAdapter {

    @JvmStatic
    @BindingAdapter("actionButton")
    fun setExpandCollapseButton(expandableTextView: ExpandableTextView, actionView: TextView) {
        expandableTextView.addOnExpandListener(object : ExpandableTextView.OnExpandListener {
            override fun onExpand(view: ExpandableTextView) {
                actionView.setText(R.string.action_read_less)
            }
            override fun onCollapse(view: ExpandableTextView) {
                actionView.setText(R.string.action_read_more)
            }
        })
        expandableTextView.post {
            actionView.isVisible = expandableTextView.lineCount >= expandableTextView.maxLines
        }
        expandableTextView.doOnTextChanged { _, _, _, _ ->
            expandableTextView.post {
                actionView.isVisible = expandableTextView.lineCount >= expandableTextView.maxLines
            }
        }
        actionView.setOnClickListener { expandableTextView.toggle() }
    }

    @JvmStatic
    @BindingAdapter("isVisible")
    fun isVisible(view: View, isVisible: Boolean) {
        view.isVisible = isVisible
    }

    @JvmStatic
    @BindingAdapter("imageUrl")
    fun loadGlideImage(view: ImageView, url: String?) {
        Glide.with(view)
            .load(url)
            .placeholder(R.drawable.ic_insert_photo_48)
            .into(view)
    }

    @JvmStatic
    @BindingAdapter("openOnClick")
    fun openUrl(view: View, url: String?) {
        if (url.isNullOrBlank()) return
        view.setOnClickListener {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
            it.context.startActivity(intent)
        }
    }

    @JvmStatic
    @BindingAdapter("activated")
    fun isActivated(button: Button, isActivated: Boolean) {
        button.isActivated = isActivated
    }

    @JvmStatic
    @BindingAdapter("tooltip")
    fun tooltip(view: View, text: String) {
        TooltipCompat.setTooltipText(view, text)
    }

}