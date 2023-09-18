package io.github.drumber.kitsune.ui.permissions

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import androidx.activity.result.ActivityResultLauncher
import androidx.core.app.ActivityCompat
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import io.github.drumber.kitsune.R

fun Context.isNotificationPermissionGranted(): Boolean {
    return ActivityCompat.checkSelfPermission(
        this,
        Manifest.permission.POST_NOTIFICATIONS
    ) == PackageManager.PERMISSION_GRANTED
}

fun Activity.requestNotificationPermission(requestPermissionLauncher: ActivityResultLauncher<String>) {
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
            }
            .setPositiveButton(R.string.action_allow) { dialog, _ ->
                dialog.dismiss()
                requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
    } else {
        requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
    }
}

fun Context.showNotificationPermissionRejectedDialog() = MaterialAlertDialogBuilder(this)
    .setTitle(R.string.dialog_notification_permission_rejected_title)
    .setMessage(R.string.dialog_notification_permission_rejected)
    .setPositiveButton(R.string.action_ok) { dialog, _ ->
        dialog.dismiss()
    }
    .show()
