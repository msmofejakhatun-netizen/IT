package com.restopro.captain

import android.app.Application
import com.restopro.captain.socket.SocketManager
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject

@HiltAndroidApp
class RestoProCaptainApp : Application() {
    @Inject lateinit var socketManager: SocketManager

    override fun onCreate() {
        super.onCreate()
        socketManager.prepare()
    }
}
