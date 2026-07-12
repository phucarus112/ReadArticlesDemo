package vn.phuclh.myapplication.util

import java.net.URLDecoder
import java.net.URLEncoder

actual fun encodeUrl(url: String): String = URLEncoder.encode(url, "UTF-8")

actual fun decodeUrl(url: String): String = URLDecoder.decode(url, "UTF-8")
