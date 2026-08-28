package com.venom7t.lolguide

import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import timber.log.Timber

/**
 * Registers the app for FCM and receives whatever a future server-sent push
 * would deliver.
 *
 * Nothing sends a push yet -- Phase 5's actual notification path is
 * on-device (LpTrackerWorker -> NotificationManager directly, no server
 * involved, see the Phase 5 plan's "not built this phase" note on Cloud
 * Functions). This service exists so the token exists and is logged,
 * ready for whenever a server-sent feature needs it; there is deliberately
 * no token-upload-to-Firestore call here yet, since nothing on the backend
 * reads it.
 */
class LolGuideMessagingService : FirebaseMessagingService() {

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        Timber.d("FCM token refreshed (length %d)", token.length)
    }

    override fun onMessageReceived(message: RemoteMessage) {
        super.onMessageReceived(message)
        Timber.d("FCM message received: %s", message.messageId)
    }
}
