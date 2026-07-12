package vn.phuclh.myapplication

import android.app.Application
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin
import vn.phuclh.myapplication.data.di.appModule
import vn.phuclh.myapplication.platform.scheduleBackgroundSync
import vn.phuclh.myapplication.util.NotificationHelper

class MyApplication : Application() {
    override fun onCreate() {
        super.onCreate()

        startKoin {
            androidContext(this@MyApplication)
            modules(appModule)
        }

        NotificationHelper.createChannel(this)
        scheduleBackgroundSync()
    }
}
