package vn.phuclh.myapplication.data.worker

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import vn.phuclh.myapplication.domain.repository.ArticleRepository
import vn.phuclh.myapplication.util.NotificationHelper

@HiltWorker
class NewsSyncWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted workerParams: WorkerParameters,
    private val repository: ArticleRepository
) : CoroutineWorker(context, workerParams) {

    override suspend fun doWork(): Result {
        val unseenBefore = repository.getUnseenCount()

        repository.refreshArticles().onFailure {
            return Result.retry()
        }

        val unseenAfter = repository.getUnseenCount()
        val newCount = unseenAfter - unseenBefore

        if (newCount > 0) {
            NotificationHelper.showNewArticlesNotification(applicationContext, newCount)
        }

        return Result.success()
    }
}
