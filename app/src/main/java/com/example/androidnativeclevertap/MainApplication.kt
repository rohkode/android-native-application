package com.example.androidnativeclevertap

import android.app.Application
import com.clevertap.android.sdk.CleverTapAPI
import com.clevertap.android.sdk.ActivityLifecycleCallback
//import com.google.firebase.messaging.FirebaseMessaging
import com.clevertap.android.pushtemplates.PushTemplateNotificationHandler;
import com.clevertap.android.sdk.interfaces.NotificationHandler;

class MainApplication : Application() {

    override fun onCreate() {
        ActivityLifecycleCallback.register(this)
        super.onCreate()

        CleverTapAPI.setDebugLevel(CleverTapAPI.LogLevel.DEBUG)
        CleverTapAPI.setNotificationHandler(PushTemplateNotificationHandler() as NotificationHandler);

        val clevertapDefaultInstance = CleverTapAPI.getDefaultInstance(applicationContext)

//        // Optional: Register FCM token manually if needed
//        FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
//            if (task.isSuccessful) {
//                val token = task.result
//                clevertapDefaultInstance?.pushFcmRegistrationId(token, true)
//            }
//        }
    }
}
