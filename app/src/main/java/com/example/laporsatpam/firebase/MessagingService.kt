package com.example.satpamtanggap.firebase

import android.util.Log
import androidx.annotation.NonNull
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.google.firebase.messaging.ktx.remoteMessage

class MessagingService: FirebaseMessagingService() {

    override fun onNewToken(@NonNull token: String) {
        super.onNewToken(token)

    }

    override fun onMessageReceived(@NonNull remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)

    }

}