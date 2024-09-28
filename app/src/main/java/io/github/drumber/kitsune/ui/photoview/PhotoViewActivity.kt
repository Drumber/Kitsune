package io.github.drumber.kitsune.ui.photoview

import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
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
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope
import androidx.navigation.navArgs
import app.futured.hauler.setOnDragActivityListener
import app.futured.hauler.setOnDragDismissedListener
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import com.google.android.material.transition.platform.MaterialContainerTransform
import com.google.android.material.transition.platform.MaterialContainerTransformSharedElementCallback
import io.github.drumber.kitsune.R
import io.github.drumber.kitsune.databinding.ActivityPhotoViewBinding
import io.github.drumber.kitsune.ui.base.BaseActivity
import io.github.drumber.kitsune.util.extensions.clearLightNavigationBar
import io.github.drumber.kitsune.util.extensions.clearLightStatusBar
import io.github.drumber.kitsune.util.extensions.isNightMode
import io.github.drumber.kitsune.util.extensions.showSomethingWrongToast
import io.github.drumber.kitsune.util.logE
import io.github.drumber.kitsune.util.saveImageInGallery
import io.github.drumber.kitsune.util.ui.initHeightWindowInsetsListener
import io.github.drumber.kitsune.util.ui.initMarginWindowInsetsListener
import io.github.drumber.kitsune.util.ui.initPaddingWindowInsetsListener
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.IOException

class PhotoViewActivity : BaseActivity(
    R.layout.activity_photo_view,
    setAppTheme = false
) {

    private lateinit var binding: ActivityPhotoViewBinding

    private val args by navArgs<PhotoViewActivityArgs>()

    private var isFullscreen: Boolean = false

    private val hideHandler = Handler(Looper.getMainLooper())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        ViewCompat.setTransitionName(findViewById(android.R.id.content), args.transitionName)
        setEnterSharedElementCallback(MaterialContainerTransformSharedElementCallback())
        window.sharedElementEnterTransition = MaterialContainerTransform().apply {
            addTarget(android.R.id.content)
            duration = resources.getInteger(R.integer.material_motion_duration_medium_2).toLong()
        }
        window.sharedElementReturnTransition = MaterialContainerTransform().apply {
            addTarget(android.R.id.content)
            duration = resources.getInteger(R.integer.material_motion_duration_medium_1).toLong()
        }

        binding = ActivityPhotoViewBinding.inflate(layoutInflater)
        setContentView(binding.root)

        if (!isNightMode()) {
            clearLightStatusBar()
            clearLightNavigationBar()
        }

        WindowInsetsControllerCompat(window, binding.root).systemBarsBehavior =
            WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE

        binding.photoView.apply {
            setOnClickListener { toggleSystemUi() }
            setAllowParentInterceptOnEdge(false) // we manage scroll interception on our own
            setOnMatrixChangeListener {
                // disallow touch event interception unless image is scrolled to the top
                binding.nestedScrollView.requestDisallowInterceptTouchEvent(it.top < 0f)
            }
        }

        Glide.with(this)
            .load(args.imageUrl)
            .thumbnail(
                Glide.with(this)
                    .load(args.thumbnailUrl)
                    .dontTransform()
            )
            .listener(object : RequestListener<Drawable> {
                override fun onLoadFailed(
                    e: GlideException?,
                    model: Any?,
                    target: Target<Drawable>,
                    isFirstResource: Boolean
                ): Boolean {
                    binding.progressIndicator.hide()
                    val shouldHaveThumbnailLoaded =
                        !isFirstResource && !args.thumbnailUrl.isNullOrBlank()
                    onImageLoadFailed(!shouldHaveThumbnailLoaded)
                    return false
                }

                override fun onResourceReady(
                    resource: Drawable,
                    model: Any,
                    target: Target<Drawable>?,
                    dataSource: DataSource,
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

            statusBarBackground.initHeightWindowInsetsListener(consume = false)
            progressIndicator.initMarginWindowInsetsListener(top = true, consume = false)
            fullscreenContentControls.initPaddingWindowInsetsListener(
                left = true,
                right = true,
                bottom = true,
                consume = false
            )
        }

        binding.haulerView.setOnDragDismissedListener { finish() }
        binding.haulerView.setOnDragActivityListener { _, rawOffset ->
            // fade background alpha on drag
            val alpha = (1.0f - rawOffset) * 255.0f
            val color = Color.argb(alpha.toInt(), 0, 0, 0)
            binding.root.setBackgroundColor(color)
        }
        // reset background color
        binding.root.setBackgroundColor(Color.BLACK)
    }

    private fun onImageLoadFailed(exit: Boolean) {
        Toast.makeText(this, R.string.error_image_loading, Toast.LENGTH_SHORT).show()
        if (exit) {
            finish()
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
        binding.statusBarBackground.isVisible = false
    }

    private fun showSystemUi() {
        isFullscreen = false
        WindowInsetsControllerCompat(window, binding.root)
            .show(WindowInsetsCompat.Type.statusBars() or WindowInsetsCompat.Type.navigationBars())
        binding.fullscreenContentControls.isVisible = true
        binding.statusBarBackground.isVisible = true
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
            val bitmap = try {
                Glide.with(this@PhotoViewActivity)
                    .asBitmap()
                    .load(args.imageUrl)
                    .submit()
                    .get()
            } catch (e: Exception) {
                runOnUiThread { onImageLoadFailed(false) }
                return@launch
            }

            val success = try {
                saveImageInGallery(bitmap, args.title)
            } catch (e: IOException) {
                logE("Failed to save image in gallery.", e)
                false
            }
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