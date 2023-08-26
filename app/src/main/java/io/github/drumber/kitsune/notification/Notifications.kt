package io.github.drumber.kitsune.notification

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import io.github.drumber.kitsune.R
import io.github.drumber.kitsune.domain.model.infrastructure.github.GitHubRelease
import io.github.drumber.kitsune.notification.NotificationChannels.CHANNEL_UPDATE_CHECKER

object Notifications {

    fun showNewVersion(context: Context, release: GitHubRelease) {
        val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse(release.url))
        val releaseIntent = PendingIntent.getActivity(
            context,
            0,
            browserIntent,
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
                PendingIntent.FLAG_CANCEL_CURRENT or PendingIntent.FLAG_IMMUTABLE
            else
                PendingIntent.FLAG_CANCEL_CURRENT
        )

        val notification = NotificationCompat.Builder(context, CHANNEL_UPDATE_CHECKER)
            .setSmallIcon(R.drawable.ic_notification_icon)
            .setContentTitle(context.getString(R.string.info_update_new_version_available))
            .setContentText(context.getString(R.string.info_update_new_version_available_text, release.version))
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(releaseIntent)
            .setAutoCancel(true)
            .build()

        NotificationManagerCompat.from(context).notify(NotificationChannels.ID_NEW_VERSION, notification)
    }

}