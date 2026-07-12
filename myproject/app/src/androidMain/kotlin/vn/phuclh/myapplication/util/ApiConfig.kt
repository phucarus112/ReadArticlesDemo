package vn.phuclh.myapplication.util

import vn.phuclh.myapplication.BuildConfig

actual object ApiConfig {
    actual val BASE_URL = "https://newsapi.org/"
    actual val NEWS_API_KEY = BuildConfig.NEWS_API_KEY
}
