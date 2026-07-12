package vn.phuclh.myapplication.platform

import androidx.work.*
import org.koin.java.KoinJavaComponent.getKoin
import vn.phuclh.myapplication.data.worker.NewsSyncWorker
import java.util.concurrent.TimeUnit

// actual: Android implementation dùng WorkManager
actual fun scheduleBackgroundSync() {
    val context = getKoin().get<android.content.Context>()
    val constraints =
        Constraints
            .Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()

    val request =
        PeriodicWorkRequestBuilder<NewsSyncWorker>(15, TimeUnit.MINUTES)
            .setConstraints(constraints)
            .build()

    WorkManager.getInstance(context).enqueueUniquePeriodicWork(
        "news_sync",
        ExistingPeriodicWorkPolicy.UPDATE,
        request,
    )
}
