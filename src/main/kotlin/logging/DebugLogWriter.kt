package com.tbread.logging

import com.tbread.packet.PropertyHandler
import org.slf4j.Logger
import org.slf4j.helpers.MessageFormatter
import java.io.File
import java.io.FileWriter
import java.time.LocalTime
import java.time.format.DateTimeFormatter

object DebugLogWriter {
    const val SETTING_KEY = "dpsMeter.debugLoggingEnabled"

    private val lock = Any()
    private val timeFormatter = DateTimeFormatter.ofPattern("HH:mm:ss.SSS")
    private val logFile = File("debug.log")

    @Volatile
    private var enabled = false

    fun loadFromSettings() {
        val stored = PropertyHandler.getProperty(SETTING_KEY)
        setEnabled(stored?.toBoolean() ?: false)
    }

    fun setEnabled(enabled: Boolean) {
        this.enabled = enabled
    }

    fun isEnabled(): Boolean = enabled

    fun debug(logger: Logger, message: String, vararg args: Any?) {
        write("DEBUG", logger.name, message, args)
    }

    fun info(logger: Logger, message: String, vararg args: Any?) {
        write("INFO", logger.name, message, args)
    }

    private fun write(level: String, loggerName: String, message: String, args: Array<out Any?>) {
        if (!enabled) return
        val result = MessageFormatter.arrayFormat(message, args)
        val formattedMessage = result.message ?: ""
        val timestamp = LocalTime.now().format(timeFormatter)
        val line = "$timestamp [${Thread.currentThread().name}] $level $loggerName - $formattedMessage"
        synchronized(lock) {
            logFile.parentFile?.mkdirs()
            FileWriter(logFile, true).use { writer ->
                writer.append(line).append('\n')
                result.throwable?.let { throwable ->
                    writer.append(throwable.stackTraceToString()).append('\n')
                }
            }
        }
    }
}
