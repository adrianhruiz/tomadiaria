package com.adrianhruiz.tomadiaria.notifications

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.adrianhruiz.tomadiaria.TomaDiariaApp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class BootRescheduleReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != Intent.ACTION_BOOT_COMPLETED && intent.action != Intent.ACTION_MY_PACKAGE_REPLACED) {
            return
        }

        val pendingResult = goAsync()
        val app = context.applicationContext as TomaDiariaApp
        val appContext = context.applicationContext
        CoroutineScope(Dispatchers.IO).launch {
            try {
                if (app.container.authRepository.currentUserId == null) return@launch
                DoseRescheduler.rescheduleFutureDoses(appContext, app.container.scheduleRepository)
            } catch (e: Exception) {
                Log.w("BootRescheduleReceiver", "Failed to reschedule doses after boot", e)
            } finally {
                pendingResult.finish()
            }
        }
    }
}
