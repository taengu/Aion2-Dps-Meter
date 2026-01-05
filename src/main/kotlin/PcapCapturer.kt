package com.tbread

import com.tbread.config.PcapCapturerConfig
import kotlinx.coroutines.channels.Channel
import org.pcap4j.core.BpfProgram
import org.pcap4j.core.PacketListener
import org.pcap4j.core.PcapNativeException;
import org.pcap4j.core.PcapNetworkInterface
import org.pcap4j.core.Pcaps
import org.pcap4j.packet.TcpPacket
import org.pcap4j.util.ByteArrays
import kotlin.system.exitProcess

class PcapCapturer(private val config: PcapCapturerConfig, private val channel: Channel<ByteArray>) {


    companion object {
        private fun getAllDevices(): List<PcapNetworkInterface> {
            return try {
                Pcaps.findAllDevs() ?: emptyList()
            } catch (e: PcapNativeException) {
                println("${this::class.java.simpleName} : Pcap 핸들러 초기화 실패")
                exitProcess(2)
            }
        }

        fun printDevices() {
            for ((i, device) in getAllDevices().withIndex()) {
                println(i.toString() + " - " + device.description + " : " + device.addresses)
            }
        }

        fun getDeviceSize(): Int {
            return getAllDevices().size
        }
    }


    fun start() {
        val devices = getAllDevices()
        if (config.deviceIdx !in devices.indices) {
            println("${this::class.java.simpleName}: [에러] 잘못된 장치 인덱스입니다.")
            exitProcess(1)
        }
        val nif = devices[config.deviceIdx]
        val handle = nif.openLive(config.snapshotSize, PcapNetworkInterface.PromiscuousMode.PROMISCUOUS, config.timeout)
        val filter = "src net ${config.serverIp} and port ${config.serverPort}"
        handle.setFilter(filter, BpfProgram.BpfCompileMode.OPTIMIZE)
        println("${this::class.java.simpleName} : 필터 설정 $filter")
        val listener = PacketListener { packet ->
            if (packet.contains(TcpPacket::class.java)) {
                val tcpPacket = packet.get(TcpPacket::class.java)
                val payload = tcpPacket.payload
                if (payload != null) {
                    val data = payload.rawData
                    if (data.isNotEmpty()) {
                        channel.trySend(data)
                    }
                }
            }
        }
        try {
            handle.use { h ->
                h.loop(-1, listener)
            }
        } catch (e: InterruptedException) {
            e.printStackTrace()
        }
    }


}