package com.tbread.packet

import org.slf4j.LoggerFactory

object CombatPortDetector {
    private val logger = LoggerFactory.getLogger(CombatPortDetector::class.java)
    @Volatile private var lockedPort: Int? = null
    @Volatile private var lockedDevice: String? = null
    private val candidates = LinkedHashMap<Int, String?>()
    private val deviceConnections = LinkedHashMap<String, MutableSet<Pair<Int, Int>>>()

    private fun normalizeDevice(deviceName: String?): String? =
        deviceName?.trim()?.takeIf { it.isNotBlank() }

    @Synchronized
    private fun lock(port: Int, deviceName: String?) {
        if (lockedPort == null) {
            lockedPort = port
            lockedDevice = normalizeDevice(deviceName)
            logger.info("🔥 Combat port locked: {}", port)
            candidates.clear()
            deviceConnections.clear()
        }
    }

    @Synchronized
    fun registerCandidate(port: Int, deviceName: String?) {
        if (lockedPort != null) return
        val trimmedDevice = normalizeDevice(deviceName)
        val existing = candidates[port]
        if (existing.isNullOrBlank() && !trimmedDevice.isNullOrBlank()) {
            candidates[port] = trimmedDevice
            return
        }
        candidates.putIfAbsent(port, trimmedDevice)
    }

    @Synchronized
    fun confirmCandidate(portA: Int, portB: Int, deviceName: String?) {
        if (lockedPort != null) return
        val port = when {
            candidates.containsKey(portA) -> portA
            candidates.containsKey(portB) -> portB
            else -> null
        } ?: return
        val trimmedDevice = normalizeDevice(deviceName)
        val candidateDevice = candidates[port]
        val resolvedDevice = trimmedDevice ?: candidateDevice
        val deviceKey = resolvedDevice ?: "unknown"
        val connection = minOf(portA, portB) to maxOf(portA, portB)
        deviceConnections.getOrPut(deviceKey) { LinkedHashSet() }.add(connection)
        val hasSingleConnectionDevice = deviceConnections.values.any { it.size == 1 }
        val currentDeviceConnections = deviceConnections[deviceKey]?.size ?: 0
        if (hasSingleConnectionDevice && currentDeviceConnections > 1) {
            return
        }
        lock(port, resolvedDevice)
    }

    @Synchronized
    fun clearCandidates() {
        if (lockedPort == null) {
            candidates.clear()
            deviceConnections.clear()
        }
    }

    fun currentPort(): Int? = lockedPort
    fun currentDevice(): String? = lockedDevice

    @Synchronized
    fun reset() {
        if (lockedPort != null) {
            logger.info("Combat port lock cleared")
        }
        lockedPort = null
        lockedDevice = null
        candidates.clear()
        deviceConnections.clear()
    }
}
