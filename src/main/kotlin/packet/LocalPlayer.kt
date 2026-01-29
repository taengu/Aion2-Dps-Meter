package com.tbread.packet

/**
 * Holds information about the local player (you).
 * The playerId will be learned automatically from packets.
 */
object LocalPlayer {

    /**
     * Your in-game character name.
     * MUST match exactly (case-sensitive).
     */
    @Volatile
    var characterName: String? = null

    /**
     * Unique actor / player ID extracted from packets.
     */
    @Volatile
    var playerId: Long? = null
}
