package vn.phuclh.myapplication.platform

import org.koin.java.KoinJavaComponent.getKoin
import vn.phuclh.myapplication.util.NotificationHelper

// actual: Android implementation dùng NotificationHelper
actual fun showNewArticlesNotification(count: Int) {
    val context = getKoin().get<android.content.Context>()
    NotificationHelper.showNewArticlesNotification(context, count)
}
