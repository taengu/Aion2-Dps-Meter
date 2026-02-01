package com.tbread

import com.tbread.config.PcapCapturerConfig
import com.tbread.packet.*
import com.tbread.webview.BrowserApp
import com.sun.jna.platform.win32.Advapi32
import com.sun.jna.platform.win32.Kernel32
import com.sun.jna.platform.win32.Shell32
import com.sun.jna.platform.win32.WinNT
import com.sun.jna.platform.win32.WinUser
import com.sun.jna.ptr.IntByReference
import javafx.application.Platform
import javafx.stage.Stage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlin.system.exitProcess

fun main() = runBlocking {
    ensureAdminOnWindows()
    Thread.setDefaultUncaughtExceptionHandler { t, e ->
        println("thread dead ${t.name}")
        e.printStackTrace()
    }

    WindowsFirewallListener.openOnStartup()

    val channel = Channel<CapturedPayload>(Channel.UNLIMITED)
    val config = PcapCapturerConfig.loadFromProperties()

    val dataStorage = DataStorage()
    val calculator = DpsCalculator(dataStorage)

    val capturer = PcapCapturer(config, channel)
    val dispatcher = CaptureDispatcher(channel, dataStorage)

    launch(Dispatchers.Default) {
        dispatcher.run()
    }

    launch(Dispatchers.IO) {
        capturer.start()
    }

    Platform.startup {
        val browserApp = BrowserApp(calculator)
        browserApp.start(Stage())
    }
}

private fun ensureAdminOnWindows() {
    val osName = System.getProperty("os.name") ?: return
    if (!osName.startsWith("Windows", ignoreCase = true)) return
    if (isProcessElevated()) return

    val currentProcess = ProcessHandle.current()
    val command = currentProcess.info().command().orElse(null) ?: return
    if (!command.endsWith(".exe", ignoreCase = true)) return
    val commandLower = command.lowercase()
    if (commandLower.endsWith("java.exe") || commandLower.endsWith("javaw.exe")) return
    val args = currentProcess.info().arguments().orElse(emptyArray())
    val parameters = args.joinToString(" ") { "\"$it\"" }

    Shell32.INSTANCE.ShellExecute(
        null,
        "runas",
        command,
        parameters.ifBlank { null },
        null,
        WinUser.SW_SHOWNORMAL
    )
    exitProcess(0)
}

private fun isProcessElevated(): Boolean {
    val token = WinNT.HANDLEByReference()
    val process = Kernel32.INSTANCE.GetCurrentProcess()
    if (!Advapi32.INSTANCE.OpenProcessToken(process, WinNT.TOKEN_QUERY, token)) {
        return false
    }
    return try {
        val elevation = WinNT.TOKEN_ELEVATION()
        val size = IntByReference()
        val ok = Advapi32.INSTANCE.GetTokenInformation(
            token.value,
            WinNT.TOKEN_INFORMATION_CLASS.TokenElevation,
            elevation,
            elevation.size(),
            size
        )
        ok && elevation.TokenIsElevated != 0
    } finally {
        Kernel32.INSTANCE.CloseHandle(token.value)
    }
}