package io.github.drumber.kitsune.ui.permissions

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.result.ActivityResultLauncher
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import io.github.drumber.kitsune.R

fun Context.isNotificationPermissionGranted(): Boolean {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        ActivityCompat.checkSelfPermission(
            this,
            Manifest.permission.POST_NOTIFICATIONS
        ) == PackageManager.PERMISSION_GRANTED
    } else {
        // no need to request permission on android versions below 33
        true
    }
}

@SuppressLint("InlinedApi")
fun Activity.requestNotificationPermission(
    requestPermissionLauncher: ActivityResultLauncher<String>,
    onRationaleDialogDismiss: (() -> Unit)? = null
) {
    if (isNotificationPermissionGranted()) return

    if (
        ActivityCompat.shouldShowRequestPermissionRationale(
            this,
            Manifest.permission.POST_NOTIFICATIONS
        )
    ) {
        MaterialAlertDialogBuilder(this)
            .setTitle(R.string.dialog_request_notification_permission_title)
            .setMessage(R.string.dialog_request_notification_permission)
            .setNegativeButton(R.string.action_cancel) { dialog, _ ->
                dialog.dismiss()
                onRationaleDialogDismiss?.invoke()
            }
            .setPositiveButton(R.string.action_allow) { dialog, _ ->
                dialog.dismiss()
                requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
            .show()
    } else {
        requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
    }
}

fun Context.showNotificationPermissionRejectedDialog(): AlertDialog = MaterialAlertDialogBuilder(this)
    .setTitle(R.string.dialog_notification_permission_rejected_title)
    .setMessage(R.string.dialog_notification_permission_rejected)
    .setPositiveButton(R.string.action_ok) { dialog, _ ->
        dialog.dismiss()
    }
    .show()
