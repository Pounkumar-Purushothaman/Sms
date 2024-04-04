package `in`.pounkumar.sms.service

import android.app.Service
import android.content.Intent
import android.os.Binder
import android.os.IBinder

class HeadlessSmsSendService : Service() {
    override fun onBind(intent: Intent?): IBinder? {
        return Binder()
    }

}