package vn.phuclh.myapplication.data.worker

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import vn.phuclh.myapplication.domain.repository.ArticleRepository
import vn.phuclh.myapplication.util.NotificationHelper

class NewsSyncWorker(
    context: Context,
    workerParams: WorkerParameters,
) : CoroutineWorker(context, workerParams),
    KoinComponent {
    private val repository: ArticleRepository by inject()

    override suspend fun doWork(): Result {
        val unseenBefore = repository.getUnseenCount()

        repository.refreshArticles().onFailure {
            return Result.retry()
        }

        val newCount = repository.getUnseenCount() - unseenBefore
        if (newCount > 0) {
            NotificationHelper.showNewArticlesNotification(applicationContext, newCount)
        }

        return Result.success()
    }
}
