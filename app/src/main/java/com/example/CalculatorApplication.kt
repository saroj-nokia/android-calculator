package com.example

import android.app.Application
import android.content.Intent

class CalculatorApplication : Application() {
    override fun onCreate() {
        super.onCreate()

        Thread.setDefaultUncaughtExceptionHandler { thread, throwable ->
            android.util.Log.e("CRASH_LOGGER", "Unhandled crash on thread ${thread.name}", throwable)
            try {
                val intent = Intent(applicationContext, MainActivity::class.java).apply {
                    putExtra("crash_extra", "Thread: ${thread.name}\nException:\n${throwable.stackTraceToString()}")
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
                }
                startActivity(intent)
            } catch (e: Throwable) {}

            try {
                Thread.sleep(500)
            } catch (e: InterruptedException) {}

            Runtime.getRuntime().halt(1)
        }
    }
}
