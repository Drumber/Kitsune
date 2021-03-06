package io.github.drumber.kitsune.ui.details.photoview

import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope
import androidx.navigation.navArgs
import app.futured.hauler.setOnDragActivityListener
import app.futured.hauler.setOnDragDismissedListener
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import io.github.drumber.kitsune.GlideApp
import io.github.drumber.kitsune.R
import io.github.drumber.kitsune.databinding.ActivityPhotoViewBinding
import io.github.drumber.kitsune.ui.base.BaseActivity
import io.github.drumber.kitsune.util.extensions.*
import io.github.drumber.kitsune.util.saveImageInGallery
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class PhotoViewActivity : BaseActivity(
    R.layout.activity_photo_view,
    edgeToEdge = true,
    updateSystemUiColors = false,
    setAppTheme = false
) {

    private lateinit var binding: ActivityPhotoViewBinding

    private val args by navArgs<PhotoViewActivityArgs>()

    private var isFullscreen: Boolean = false

    private val hideHandler = Handler(Looper.getMainLooper())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityPhotoViewBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setStatusBarColorRes(R.color.translucent_system_overlay)
        if (Build.VERSION.SDK_INT >= 27) {
            window.navigationBarColor =
                ContextCompat.getColor(this, R.color.translucent_system_overlay)
        }
        if (!isNightMode()) {
            clearLightStatusBar()
            clearLightNavigationBar()
        }

        WindowInsetsControllerCompat(window, binding.root).systemBarsBehavior =
            WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE

        binding.photoView.setOnClickListener { toggleSystemUi() }

        GlideApp.with(this)
            .load(args.imageUrl)
            .thumbnail(
                GlideApp.with(this)
                    .load(args.thumbnailUrl)
                    .dontTransform()
            )
            .listener(object : RequestListener<Drawable> {
                override fun onLoadFailed(
                    e: GlideException?,
                    model: Any?,
                    target: Target<Drawable>?,
                    isFirstResource: Boolean
                ): Boolean {
                    onImageLoadFailed()
                    return false
                }
                override fun onResourceReady(
                    resource: Drawable?,
                    model: Any?,
                    target: Target<Drawable>?,
                    dataSource: DataSource?,
                    isFirstResource: Boolean
                ): Boolean {
                    binding.progressIndicator.hide()
                    return false
                }
            })
            .dontTransform()
            .into(binding.photoView)

        binding.apply {
            btnClose.resetAutoHideOnTouch()
            btnSave.resetAutoHideOnTouch()
            btnOpen.resetAutoHideOnTouch()

            btnClose.setOnClickListener { finish() }
            btnSave.setOnClickListener { saveImage() }
            btnOpen.setOnClickListener {
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(args.imageUrl))
                startActivity(intent)
            }
        }

        binding.haulerView.setOnDragDismissedListener { finish() }
        binding.haulerView.setOnDragActivityListener { _, rawOffset ->
            // fade background alpha on drag
            val alpha = (1.0f - rawOffset) * 255.0f
            binding.photoBackground.background.alpha = alpha.toInt()
        }
        // reset alpha
        binding.photoBackground.background.alpha = 255
    }

    private fun onImageLoadFailed() {
        Toast.makeText(this, R.string.error_image_loading, Toast.LENGTH_SHORT).show()
        finish()
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
        setOnTouchListener { _, _ ->
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

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == WRITE_EXTERNAL_STORAGE_REQUEST_CODE
            && grantResults.isNotEmpty()
            && grantResults[0] == PackageManager.PERMISSION_GRANTED
        ) {
            saveImage()
        } else if (requestCode == WRITE_EXTERNAL_STORAGE_REQUEST_CODE) {
            Toast.makeText(
                this,
                R.string.error_requires_external_storage_permission,
                Toast.LENGTH_LONG
            ).show()
        }
    }

    private fun saveImage() {
        // check if permission is granted
        if (shouldRequestWritePermission()) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE),
                WRITE_EXTERNAL_STORAGE_REQUEST_CODE
            )
            return
        }

        lifecycleScope.launch(Dispatchers.IO) {
            val bitmap = GlideApp.with(this@PhotoViewActivity)
                .asBitmap()
                .load(args.imageUrl)
                .submit()
                .get()

            val success = saveImageInGallery(bitmap, args.title)
            withContext(Dispatchers.Main) {
                if (success) {
                    Toast.makeText(
                        this@PhotoViewActivity,
                        R.string.info_image_saved_in_gallery,
                        Toast.LENGTH_SHORT
                    ).show()
                } else {
                    showSomethingWrongToast()
                }
            }
        }
    }

    /**
     * Check if external storage write permission must be requested.
     * On Android 10+ this is no longer required, see
     * [here](https://developer.android.com/training/data-storage/shared/media#scoped_storage_enabled).
     */
    private fun shouldRequestWritePermission(): Boolean {
        return Build.VERSION.SDK_INT < Build.VERSION_CODES.Q &&
                ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE
                ) != PackageManager.PERMISSION_GRANTED
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

        private const val WRITE_EXTERNAL_STORAGE_REQUEST_CODE = 1
    }
}