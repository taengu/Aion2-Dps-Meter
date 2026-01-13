package com.tbread

import com.tbread.config.PcapCapturerConfig
import com.tbread.packet.PcapCapturer
import com.tbread.packet.StreamAssembler
import com.tbread.packet.StreamProcessor
import com.tbread.webview.BrowserApp
import javafx.application.Application
import javafx.application.Platform
import javafx.stage.Stage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

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
    dataStorage.appendMobCode(8775000,"번견 쿠하푸")
    dataStorage.appendMobCode(77805,"두두카 일꾼")
    dataStorage.appendMobCode(129675,"오래된 어그린트")
    dataStorage.appendMobCode(11700000,"누아쿰")
    //임시로 여기서 추가
    val processor = StreamProcessor(dataStorage)
    val assembler = StreamAssembler(processor)
    val capturer = PcapCapturer(config, channel)
    val calculator = DpsCalculator(dataStorage)

    launch(Dispatchers.Default) {
        for (chunk in channel) {
            assembler.processChunk(chunk)
        }
    }

    launch(Dispatchers.IO) {
        capturer.start()
    }

    Platform.startup{
        val browserApp = BrowserApp(calculator)
        browserApp.start(Stage())
    }
}


