package com.restopro.captain

import android.app.Application
import com.restopro.captain.socket.SocketManager
import com.restopro.captain.sync.WorkScheduler
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject

@HiltAndroidApp
class RestoProCaptainApp : Application() {
    @Inject lateinit var socketManager: SocketManager
    @Inject lateinit var workScheduler: WorkScheduler

    override fun onCreate() {
        super.onCreate()
        socketManager.prepare()
        workScheduler.scheduleSync(this)
    }
}
