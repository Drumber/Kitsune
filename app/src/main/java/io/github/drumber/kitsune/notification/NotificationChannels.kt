package io.github.drumber.kitsune.notification

import android.content.Context
import androidx.core.app.NotificationChannelCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.app.NotificationManagerCompat.IMPORTANCE_HIGH
import io.github.drumber.kitsune.R

object NotificationChannels {

    const val CHANNEL_UPDATE_CHECKER = "update_checker"
    const val ID_NEW_VERSION = 10

    fun registerNotificationChannels(context: Context) {
        val notificationManager = NotificationManagerCompat.from(context)
        notificationManager.createNotificationChannelsCompat(
            listOf(
                notificationChannel(CHANNEL_UPDATE_CHECKER, IMPORTANCE_HIGH) {
                    setName(context.getString(R.string.notification_updates))
                    setShowBadge(false)
                }
            )
        )
    }

    private fun notificationChannel(
        id: String,
        importance: Int,
        block: NotificationChannelCompat.Builder.() -> Unit
    ): NotificationChannelCompat {
        return NotificationChannelCompat.Builder(id, importance).apply(block).build()
    }

}