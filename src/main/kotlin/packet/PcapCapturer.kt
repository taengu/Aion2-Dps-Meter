package com.tbread.packet

import com.tbread.config.PcapCapturerConfig
import kotlinx.coroutines.channels.Channel
import org.pcap4j.core.BpfProgram
import org.pcap4j.core.PacketListener
import org.pcap4j.core.PcapNativeException
import org.pcap4j.core.PcapNetworkInterface
import org.pcap4j.core.Pcaps
import org.pcap4j.packet.Packet
import org.pcap4j.packet.TcpPacket
import org.slf4j.LoggerFactory
import kotlin.system.exitProcess

class PcapCapturer(
    private val config: PcapCapturerConfig,
    private val channel: Channel<ByteArray>
) {

    companion object {
        private val logger = LoggerFactory.getLogger(javaClass.enclosingClass)

        private fun getAllDevices(): List<PcapNetworkInterface> {
            return try {
                Pcaps.findAllDevs() ?: emptyList()
            } catch (e: PcapNativeException) {
                logger.error("Failed to initialize pcap", e)
                exitProcess(2)
            }
        }
    }

    /**
     * ExitLag / VPN traffic appears on the Npcap loopback adapter.
     */
    private fun getLoopbackDevice(): PcapNetworkInterface? {
        return getAllDevices().firstOrNull {
            it.isLoopBack || it.description?.contains("loopback", ignoreCase = true) == true
        }
    }

    fun start() {
        val nif = getLoopbackDevice()
        if (nif == null) {
            logger.error("Failed to find loopback capture device")
            exitProcess(1)
        }

        logger.info("Using capture device: {}", nif.description)

        val handle = nif.openLive(
            config.snapshotSize,
            PcapNetworkInterface.PromiscuousMode.PROMISCUOUS,
            config.timeout
        )

        /*
         * IMPORTANT:
         * - Npcap loopback does NOT support tcp.len, flags, etc.
         * - Keep filter minimal and safe.
         */
        val filter = "tcp"
        handle.setFilter(filter, BpfProgram.BpfCompileMode.OPTIMIZE)
        logger.info("Packet filter set to \"$filter\"")

        val listener = PacketListener { packet: Packet ->
            val tcp = packet.get(TcpPacket::class.java) ?: return@PacketListener
            val payload = tcp.payload ?: return@PacketListener

            val data = payload.rawData
            if (data.isEmpty()) return@PacketListener

            val port = tcp.header.srcPort.valueAsInt()

            /*
             * Forward packets as follows:
             * - Before combat port is locked → allow everything
             * - After lock → only allow matching port
             *
             * CombatPortDetector.lock(port) is called later
             * by StreamProcessor when real combat is parsed.
             */
            val lockedPort = CombatPortDetector.currentPort()
            if (lockedPort == null || lockedPort == port) {
                channel.trySend(data)
            }
        }

        try {
            handle.use { h ->
                h.loop(-1, listener)
            }
        } catch (e: InterruptedException) {
            logger.error("Packet capture interrupted", e)
        }
    }
}
