package com.tbread.entity

enum class JobClass(val className: String, val basicSkillCode: Int) {
    WARRIOR("검성", 9952),
    PALADIN("수호성", 0),
    RANGER("궁성", 60832),
    ROGUE("살성", 33872),
    SORCERER("마도성", 0),
    CLERIC("치유성", 0),
    SUMMONER("정령성", 19216),
    SHAMAN("호법성", 0)
}