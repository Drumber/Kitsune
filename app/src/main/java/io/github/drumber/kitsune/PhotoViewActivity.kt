package io.github.drumber.kitsune

import android.annotation.SuppressLint
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope
import androidx.navigation.navArgs
import io.github.drumber.kitsune.databinding.ActivityPhotoViewBinding
import io.github.drumber.kitsune.ui.base.BaseActivity
import io.github.drumber.kitsune.util.extensions.*
import io.github.drumber.kitsune.util.saveImageInGallery
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class PhotoViewActivity : BaseActivity(R.layout.activity_photo_view, true, false) {

    private lateinit var binding: ActivityPhotoViewBinding

    private val args by navArgs<PhotoViewActivityArgs>()

    private var isFullscreen: Boolean = false

    private val hideHandler = Handler(Looper.getMainLooper())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityPhotoViewBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setStatusBarColorRes(R.color.translucent_system_overlay)
        if(Build.VERSION.SDK_INT >= 27) {
            window.navigationBarColor = ContextCompat.getColor(this, R.color.translucent_system_overlay)
        }
        if (!isNightMode()) {
            clearLightStatusBar()
            clearLightNavigationBar()
        }

        WindowInsetsControllerCompat(window, binding.root).systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE

        binding.photoView.setOnClickListener { toggleSystemUi() }

        GlideApp.with(this)
            .load(args.imageUrl)
            .dontTransform()
            .into(binding.photoView)

        binding.apply {
            btnSave.resetAutoHideOnTouch()
            btnOpen.resetAutoHideOnTouch()

            btnSave.setOnClickListener { saveImage() }
            btnOpen.setOnClickListener {
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(args.imageUrl))
                startActivity(intent)
            }
        }
    }

    override fun onPostCreate(savedInstanceState: Bundle?) {
        super.onPostCreate(savedInstanceState)

        hideHandler.removeCallbacks(hideRunnable)
        hideHandler.postDelayed(hideRunnable, UI_ANIMATION_DELAY)
    }

    private val hideRunnable = Runnable {
        hideSystemUi()
    }

    /**
     * Resets any hide callback to avoid hiding controls while the user is interacting.
     */
    private fun resetAutoHideTime() {
        if (!isFullscreen) {
            hideHandler.removeCallbacks(hideRunnable)
            hideHandler.postDelayed(hideRunnable, AUTO_HIDE_DELAY_MILLIS)
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun View.resetAutoHideOnTouch() {
        setOnTouchListener { v, event ->
            resetAutoHideTime()
            false
        }
    }

    private fun hideSystemUi() {
        isFullscreen = true
        WindowInsetsControllerCompat(window, binding.root)
            .hide(WindowInsetsCompat.Type.statusBars() or WindowInsetsCompat.Type.navigationBars())
        binding.fullscreenContentControls.isVisible = false
    }

    private fun showSystemUi() {
        isFullscreen = false
        WindowInsetsControllerCompat(window, binding.root)
            .show(WindowInsetsCompat.Type.statusBars() or WindowInsetsCompat.Type.navigationBars())
        binding.fullscreenContentControls.isVisible = true
    }

    private fun toggleSystemUi() {
        if (isFullscreen) {
            hideHandler.removeCallbacks(hideRunnable)
            if (AUTO_HIDE) {
                hideHandler.postDelayed(hideRunnable, AUTO_HIDE_DELAY_MILLIS)
            }
            showSystemUi()
        } else {
            hideSystemUi()
        }
    }

    private fun saveImage() {
        lifecycleScope.launch(Dispatchers.IO) {
            val bitmap = GlideApp.with(this@PhotoViewActivity)
                .asBitmap()
                .load(args.imageUrl)
                .submit()
                .get()

            val success = saveImageInGallery(bitmap, args.title)
            withContext(Dispatchers.Main) {
                if (success) {
                    Toast.makeText(this@PhotoViewActivity, R.string.info_image_saved_in_gallery, Toast.LENGTH_SHORT).show()
                } else {
                    showSomethingWrongToast()
                }
            }
        }
    }

    companion object {
        /**
         * Whether or not the system UI should be auto-hidden after
         * [AUTO_HIDE_DELAY_MILLIS] milliseconds.
         */
        private const val AUTO_HIDE = true

        /**
         * If [AUTO_HIDE] is set, the number of milliseconds after
         * hiding the system UI.
         */
        private const val AUTO_HIDE_DELAY_MILLIS = 5000L

        /**
         * Hide system UI after this amount of milliseconds.
         */
        private const val UI_ANIMATION_DELAY = 500L
    }
}