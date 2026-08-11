package vn.phuclh.myapplication

import android.app.Application
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin
import vn.phuclh.myapplication.data.di.androidModule
import vn.phuclh.myapplication.data.di.commonModule
import vn.phuclh.myapplication.platform.scheduleBackgroundSync
import vn.phuclh.myapplication.util.NotificationHelper

class MyApplication : Application() {
    // Scope sống theo vòng đời app, dùng cho việc khởi tạo nền
    private val appScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onCreate() {
        super.onCreate()

        startKoin {
            androidContext(this@MyApplication)
            modules(commonModule, androidModule)
        }

        NotificationHelper.createChannel(this)

        // WorkManager.enqueue chạm ổ đĩa -> đẩy khỏi main thread để không lag lúc mở app
        appScope.launch { scheduleBackgroundSync() }
    }
}
