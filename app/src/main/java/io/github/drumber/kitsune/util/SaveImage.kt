package io.github.drumber.kitsune.util

import android.content.ContentValues
import android.content.Context
import android.graphics.Bitmap
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import io.github.drumber.kitsune.util.extensions.formatDate
import java.io.File
import java.io.FileOutputStream
import java.util.*

fun Context.saveImageInGallery(image: Bitmap, imageName: String? = null): Boolean {
    val fileName = (if (!imageName.isNullOrBlank()) "${imageName}_" else "") + Date().formatDate("yyyy-MM-dd-HH-mm-ss")

    val outputStream = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
        val contentValues = ContentValues().apply {
            if (!imageName.isNullOrBlank()) put(MediaStore.MediaColumns.TITLE, imageName)
            put(MediaStore.MediaColumns.DISPLAY_NAME, fileName)
            put(MediaStore.MediaColumns.MIME_TYPE, "image/jpeg")
            put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_PICTURES)
        }

        val imageUri = contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)
        imageUri?.let { contentResolver.openOutputStream(it) }
    } else {
        val imagesDirectory = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)
        FileOutputStream(File(imagesDirectory, fileName))
    }

    outputStream?.use {
        logD("Saving image $fileName")
        image.compress(Bitmap.CompressFormat.JPEG, 100, it)
        return true
    }
    return false
}
