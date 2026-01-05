package com.tbread

class ParsedDamagePacket {
        private var actorId = 0
        private var targetId = 0
        private var flag = 0
        private var damage = 0
        private var skillCode = 0
        private var skillCode2 = 0
        private var type = 0
        private var unknown = 0
        private var unknown2 = 0
        private var loop = 0
        private var skipValues = mutableListOf<Int>()
        private val timestamp = System.currentTimeMillis()

        fun setActorId(actorInfo:StreamProcessor.VarIntOutput){
                this.actorId = actorInfo.value
        }
        fun setTargetId(targetInfo:StreamProcessor.VarIntOutput){
                this.targetId = targetInfo.value
        }
        fun setFlag(flagInfo:StreamProcessor.VarIntOutput){
                this.flag = flagInfo.value
        }
        fun setDamage(damageInfo:StreamProcessor.VarIntOutput){
                this.damage = damageInfo.value
        }
        fun setSkillCode(skillCodeInfo:StreamProcessor.VarIntOutput){
                this.skillCode = skillCodeInfo.value
        }
        fun setSkillCode2(skillCode2Info:StreamProcessor.VarIntOutput){
                this.skillCode2 = skillCode2Info.value
        }
        fun setUnknown(unknownInfo:StreamProcessor.VarIntOutput){
                this.unknown = unknownInfo.value
        }
        fun setUnknown2(unknown2Info:StreamProcessor.VarIntOutput){
                this.unknown2 = unknown2Info.value
        }
        fun setLoop(loopInfo:StreamProcessor.VarIntOutput){
                this.loop = loopInfo.value
        }
        fun addSkipData(skipValueInfo:StreamProcessor.VarIntOutput){
                this.skipValues.add(skipValueInfo.value)
        }
        fun setType(typeInfo:StreamProcessor.VarIntOutput){
                this.type = typeInfo.value
        }

        fun getActorId(): Int {
                return this.actorId
        }

        fun getDamage():Int{
                return this.damage
        }

        fun getFlag():Int{
                return this.flag
        }

        fun getSkillCode1():Int{
                return this.skillCode
        }

        fun getSkillCode2():Int{
                return this.skillCode2
        }

        fun getTargetId():Int{
                return this.targetId
        }

        fun getUnknown():Int{
                return this.unknown
        }
        fun getUnknown2():Int{
                return this.unknown2
        }
        fun getLoop():Int{
                return this.loop
        }
        fun getType():Int{
                return this.type
        }



}