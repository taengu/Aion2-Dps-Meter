package com.tbread.logging

import java.io.File
import java.io.FileWriter
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

object CrashLogWriter {
    private val lock = Any()
    private val timestampFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS")
    private val logFile = File("crash.log")

    fun log(message: String, throwable: Throwable? = null) {
        val timestamp = LocalDateTime.now().format(timestampFormatter)
        val threadName = Thread.currentThread().name
        val line = "$timestamp [$threadName] $message"
        synchronized(lock) {
            logFile.parentFile?.mkdirs()
            FileWriter(logFile, true).use { writer ->
                writer.append(line).append('\n')
                throwable?.let { writer.append(it.stackTraceToString()).append('\n') }
            }
        }
    }
}
