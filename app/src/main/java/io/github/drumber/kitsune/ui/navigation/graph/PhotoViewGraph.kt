package io.github.drumber.kitsune.ui.navigation.graph

import android.Manifest
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.core.content.ContextCompat
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import androidx.navigation.toRoute
import coil3.SingletonImageLoader
import coil3.request.ErrorResult
import coil3.request.ImageRequest
import coil3.request.SuccessResult
import coil3.toBitmap
import io.github.drumber.kitsune.R
import io.github.drumber.kitsune.ui.navigation.Routes
import io.github.drumber.kitsune.ui.photoview.PhotoViewScreen
import io.github.drumber.kitsune.util.logE
import io.github.drumber.kitsune.util.saveImageInGallery
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.IOException

fun NavGraphBuilder.photoViewGraph(navController: NavHostController) {
    composable<Routes.PhotoView> { backStackEntry ->
        val route = backStackEntry.toRoute<Routes.PhotoView>()
        PhotoViewDestination(navController, route)
    }
}

@Composable
private fun PhotoViewDestination(
    navController: NavHostController,
    route: Routes.PhotoView
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    val savedMessage = stringResource(R.string.info_image_saved_in_gallery)
    val errorMessage = stringResource(R.string.error_image_loading)
    val permissionMessage = stringResource(R.string.error_requires_external_storage_permission)

    val saveImage: () -> Unit = {
        scope.launch(Dispatchers.IO) {
            val request = ImageRequest.Builder(context).data(route.imageUrl).build()
            val bitmap = when (val result = SingletonImageLoader.get(context).execute(request)) {
                is SuccessResult -> result.image.toBitmap()
                is ErrorResult -> {
                    withContext(Dispatchers.Main) {
                        Toast.makeText(context, errorMessage, Toast.LENGTH_SHORT).show()
                    }
                    return@launch
                }
            }
            val success = try {
                context.saveImageInGallery(bitmap, route.title)
            } catch (e: IOException) {
                logE("Failed to save image in gallery.", e)
                false
            }
            withContext(Dispatchers.Main) {
                Toast.makeText(
                    context,
                    if (success) savedMessage else errorMessage,
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
        Unit
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            saveImage()
        } else {
            Toast.makeText(context, permissionMessage, Toast.LENGTH_LONG).show()
        }
    }

    PhotoViewScreen(
        imageUrl = route.imageUrl,
        title = route.title,
        onClose = { navController.navigateUp() },
        onSaveImage = {
            // On Android 10+ scoped storage removes the need for a write permission.
            val needsPermission = Build.VERSION.SDK_INT < Build.VERSION_CODES.Q &&
                    ContextCompat.checkSelfPermission(
                        context,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE
                    ) != android.content.pm.PackageManager.PERMISSION_GRANTED
            if (needsPermission) {
                permissionLauncher.launch(Manifest.permission.WRITE_EXTERNAL_STORAGE)
            } else {
                saveImage()
            }
        },
        onOpenInBrowser = {
            try {
                context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(route.imageUrl)))
            } catch (_: Exception) { /* no browser installed */ }
        }
    )
}
