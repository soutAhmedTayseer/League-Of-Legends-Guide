package com.venom7t.lolguide.data.lptracker.worker

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.venom7t.lolguide.domain.lptracker.usecase.LpChange
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.math.abs

/**
 * Posts a local notification when a followed summoner's LP changed since the
 * last poll. Entirely on-device: [LpTrackerWorker] detects the change itself
 * by diffing two of its own recorded snapshots and calls straight into
 * [android.app.NotificationManager] -- there is no server push behind this
 * notification (Phase 5 plan's "not built this phase" note on Cloud
 * Functions / real server-sent push).
 */
@Singleton
class LpChangeNotifier @Inject constructor(
    @ApplicationContext private val context: Context,
) {

    fun notify(change: LpChange) {
        // A queue placement correction or a one-LP rounding blip is not
        // worth interrupting the user for -- only a real game's worth of
        // movement triggers a notification.
        if (abs(change.leaguePointsDelta) < MIN_NOTIFIABLE_DELTA) return

        ensureChannel()

        val sign = if (change.leaguePointsDelta > 0) "+" else ""
        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle("${change.riotIdName}#${change.riotIdTagline}")
            .setContentText(
                "$sign${change.leaguePointsDelta} LP -- now ${change.newTier} " +
                    "${change.newRank} ${change.newLeaguePoints} LP",
            )
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
            .build()

        // POST_NOTIFICATIONS can be denied on API 33+; NotificationManagerCompat
        // silently no-ops the post rather than crashing when it is, which is
        // the correct behaviour for opportunistic background work.
        NotificationManagerCompat.from(context).notify(
            "${change.puuid}:${change.queueType.name}".hashCode(),
            notification,
        )
    }

    private fun ensureChannel() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return
        val manager = context.getSystemService(NotificationManager::class.java) ?: return
        if (manager.getNotificationChannel(CHANNEL_ID) != null) return
        manager.createNotificationChannel(
            NotificationChannel(CHANNEL_ID, "LP changes", NotificationManager.IMPORTANCE_DEFAULT),
        )
    }

    private companion object {
        const val CHANNEL_ID = "lp_tracker"
        const val MIN_NOTIFIABLE_DELTA = 1
    }
}
