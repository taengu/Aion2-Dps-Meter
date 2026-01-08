package com.tbread

import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.awt.SwingPanel
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.WindowPosition
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState
import com.tbread.config.PcapCapturerConfig
import javafx.application.Application
import javafx.application.Platform
import javafx.embed.swing.JFXPanel
import javafx.scene.Scene
import javafx.scene.paint.Color
import javafx.scene.web.WebEngine
import javafx.scene.web.WebView
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import java.awt.BorderLayout
import java.awt.Dimension
import java.awt.event.ComponentAdapter
import java.awt.event.ComponentEvent
import java.io.File
import javax.swing.JPanel
import javax.swing.SwingUtilities

fun main() = runBlocking {
    Thread.setDefaultUncaughtExceptionHandler { t, e ->
        println("thread dead ${t.name}")
        e.printStackTrace()
    }
//    var deviceIdx = PropertyHandler.getProperty("device")
//    if (deviceIdx == null) {
//        println("사용할 네트워크 기기 번호를 입력하세요.")
//        PcapCapturer.printDevices()
//        deviceIdx = run {
//            while (true) {
//                readLine()?.toIntOrNull()?.takeIf { it in 0..<PcapCapturer.getDeviceSize() }?.let { return@run it }
//                println("유효하지 않은 값입니다. 다시 입력해주세요.")
//            }
//        }.toString()
//        PropertyHandler.setProperty("device", deviceIdx)
//    }
    val channel = Channel<ByteArray>(Channel.UNLIMITED)
    val config = PcapCapturerConfig.loadFromProperties()

    val dataStorage = DataStorage()
    val processor = StreamProcessor(dataStorage)
    val assembler = StreamAssembler(processor)
    val capturer = PcapCapturer(config, channel)

    launch(Dispatchers.Default) {
        for (chunk in channel) {
            assembler.processChunk(chunk)
        }
    }

    launch(Dispatchers.IO) {
        capturer.start()
    }

    Application.launch(BrowserApp::class.java)
}


