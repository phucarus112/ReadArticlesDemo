package vn.phuclh.myapplication

import android.content.Context

// LEAK: singleton object giữ static reference đến Context (Activity)
// Activity bị destroy (xoay màn hình) nhưng singleton vẫn còn sống
// → GC không thể thu hồi Activity → memory leak
object Event {
    private var listeners = mutableListOf<Context>()

    fun register(context: Context) {
        listeners.add(context)
    }
}
