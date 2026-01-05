package com.tbread

class StreamProcessor {

    data class VarIntOutput(val value: Int, val length: Int)

    private val mask = 0x0f

    fun onPacketReceived(packet: ByteArray) {
        // 매직패킷 단일로 올때 무시
        if (packet.size <= 3) return
        val packetLengthInfo = readVarInt(packet)
        if (packet.size == packetLengthInfo.value ) {
            parsePerfectPacket(packet.copyOfRange(0, packet.size - 3))
            //더이상 자를필요가 없는 최종 패킷뭉치
            return
        }

        try {
            if (packetLengthInfo.value <= 3) return
            if (packetLengthInfo.value > packet.size) return
            parsePerfectPacket(packet.copyOfRange(0, packetLengthInfo.value - 3))
            //매직패킷이 빠져있는 패킷뭉치

            onPacketReceived(packet.copyOfRange(packetLengthInfo.value - 3, packet.size))
            //남은패킷 재처리
        } catch (e: Exception) {
            println("${this::class.java.simpleName} : [경고] 패킷 소비자 에러")
            e.printStackTrace()
            return
            //구현부끝나면 로거넣고 빼기
        }

    }

    private fun parsePerfectPacket(packet: ByteArray) {
        parsingDamage(packet)
    }

    private fun parsingDamage(packet: ByteArray) {
        if (packet[0] == 0x20.toByte()) return
        var offset = 0
        val packetLengthInfo = readVarInt(packet)
        val pdp = ParsedDamagePacket()

        offset += packetLengthInfo.length

        if (packet[offset] != 0x04.toByte()) return
        if (packet[offset + 1] != 0x38.toByte()) return
        offset += 2
        if (offset>=packet.size) return

        val actorInfo = readVarInt(packet, offset)
        pdp.setActorId(actorInfo)
        offset += actorInfo.length //타겟
        if (offset>=packet.size) return

        val targetInfo = readVarInt(packet, offset)
        pdp.setTargetId(targetInfo)
        offset += targetInfo.length //점프용
        if (offset>=packet.size) return

        val flagInfo = readVarInt(packet, offset)
        pdp.setFlag(flagInfo)
        offset += flagInfo.length //플래그
        if (offset>=packet.size) return

        val damageInfo = readVarInt(packet, offset)
        pdp.setDamage(damageInfo)
        offset += damageInfo.length
        if (offset>=packet.size) return

        if (offset + 5 >= packet.size) return

        val temp = offset

        val skillInfo = readVarInt(packet, offset)
        pdp.setSkillCode(skillInfo)
        offset += skillInfo.length

        val skillInfo2 = readVarInt(packet, offset)
        pdp.setSkillCode2(skillInfo2)
        offset = temp + 5

        val typeInfo = readVarInt(packet, offset)
        pdp.setType(typeInfo)
        offset += typeInfo.length
        if (offset>=packet.size) return

        val andResult = targetInfo.value and mask
        offset += when (andResult) {
            4 -> 8
            5 -> 12
            6 -> 10
            7 -> 14
            else -> return
        }

        if (offset >= packet.size) return

        val unknownInfo = readVarInt(packet, offset)
        pdp.setUnknown(unknownInfo)
        offset += unknownInfo.length
        if (offset>=packet.size) return

        val unknownInfo2 = readVarInt(packet, offset)
        pdp.setUnknown2(unknownInfo2)
        offset += unknownInfo2.length
        if (offset>=packet.size) return

        val loopInfo = readVarInt(packet, offset)
        pdp.setLoop(loopInfo)
        offset += loopInfo.length
        if (offset>=packet.size) return

        for (i in 0 until loopInfo.length) {
            var skipValueInfo = readVarInt(packet, offset)
            pdp.addSkipData(skipValueInfo)
            offset += skipValueInfo.length
            if (offset>=packet.size) return
        }

        println("피격자: ${pdp.getActorId()} 공격자: ${pdp.getDamage()} 스위치용변수: ${pdp.getTargetId()} 스킬: ${pdp.getSkillCode1()} 스킬2: ${pdp.getSkillCode2()}" +
                " 플래그: ${pdp.getFlag()} 타입: ${pdp.getType()}" +
                " unknown : ${pdp.getUnknown()} 데미지: ${pdp.getUnknown2()} loop: ${pdp.getLoop()}")

    }


    private fun toHex(bytes: ByteArray): String {
        //출력테스트용
        return bytes.joinToString(" ") { "%02X".format(it) }
    }

    fun readVarInt(bytes: ByteArray, offset: Int = 0): VarIntOutput {
        //구글 Protocol Buffers 라이브러리에 이미 있나? 코드 효율성에 차이있어보이면 나중에 바꾸는게 나을듯?
        var value = 0
        var shift = 0
        var count = 0

        while (true) {
            if (offset + count >= bytes.size) {
                println("${this::class.java.simpleName} : [에러] 배열 범위 초과")
            }

            val byteVal = bytes[offset + count].toInt()
            count++

            value = value or (byteVal and 0x7F shl shift)

            if ((byteVal and 0x80) == 0) {
                return VarIntOutput(value, count)
            }

            shift += 7
            if (shift >= 64) throw RuntimeException("VarInt too long")
        }
    }
}
