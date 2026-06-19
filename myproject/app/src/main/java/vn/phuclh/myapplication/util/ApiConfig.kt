package vn.phuclh.myapplication.util

import vn.phuclh.myapplication.BuildConfig

object ApiConfig {
    const val BASE_URL = "https://newsapi.org/"
    // Set NEWS_API_KEY in local.properties (never commit the key)
    val NEWS_API_KEY: String get() = BuildConfig.NEWS_API_KEY
}
