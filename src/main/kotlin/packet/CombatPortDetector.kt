package com.tbread.packet

/**
 * Tracks and locks the TCP port used for combat traffic.
 *
 * IMPORTANT:
 * - This detector does NOT inspect raw packet payloads.
 * - Locking must be triggered ONLY after real combat is confirmed
 *   by StreamProcessor (e.g. damage / skill event parsed).
 */
object CombatPortDetector {

    @Volatile
    private var lockedPort: Int? = null

    /**
     * Returns the currently locked combat port, or null if not locked yet.
     */
    fun currentPort(): Int? = lockedPort

    /**
     * Locks the combat port.
     * Safe to call multiple times — only the first call has effect.
     */
    fun lock(port: Int) {
        if (lockedPort == null) {
            lockedPort = port
            println("🔥 Combat port locked: $port")
        }
    }

    /**
     * Clears the locked port.
     * Optional — only use if you explicitly want to re-detect.
     */
    fun reset() {
        lockedPort = null
    }
}
