package io.github.drumber.kitsune.util

import android.content.ContentValues
import android.content.Context
import android.graphics.Bitmap
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import io.github.drumber.kitsune.shared.formatDate
import io.github.drumber.kitsune.shared.logD
import java.util.*

fun Context.saveImageInGallery(image: Bitmap, imageName: String? = null): Boolean {
    val fileName = (if (!imageName.isNullOrBlank()) "${imageName}_" else "") + Date().formatDate("yyyy-MM-dd-HH-mm-ss") + ".jpg"

    val contentValues = ContentValues().apply {
        if (!imageName.isNullOrBlank()) put(MediaStore.MediaColumns.TITLE, imageName)
        put(MediaStore.MediaColumns.DISPLAY_NAME, fileName)
        put(MediaStore.MediaColumns.MIME_TYPE, "image/jpeg")
        put(MediaStore.MediaColumns.DATE_ADDED, System.currentTimeMillis() / 1000)
        put(MediaStore.MediaColumns.DATE_MODIFIED, System.currentTimeMillis() / 1000)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            put(MediaStore.MediaColumns.DATE_TAKEN, System.currentTimeMillis())
            put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_PICTURES)
        }
    }

    val imageUri = contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)
    val outputStream = imageUri?.let { contentResolver.openOutputStream(it) }

    outputStream?.use {
        logD("Saving image to $imageUri")
        image.compress(Bitmap.CompressFormat.JPEG, 100, it)
        return true
    }
    return false
}
