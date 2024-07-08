package world.gregs.voidps.world.activity.skill.slayer

import world.gregs.voidps.engine.entity.character.Character
import world.gregs.voidps.engine.entity.character.npc.NPC
import world.gregs.voidps.engine.entity.character.player.Player

private fun isUndead(category: String) =
    category == "shade" || category == "zombie" || category == "skeleton" || category == "ghost" || category == "zogre" || category == "ankou"

val Character.undead: Boolean
    get() = if (this is NPC) isUndead(race) else false

val Player.hasSlayerTask: Boolean
    get() = this["slayer_task", false]

val NPC.race: String
    get() = this.def["race", ""]

var Player.slayerPoints: Int
    get() = this["slayer_points", 0]
    set(value) {
        this["slayer_points"] = value
    }

fun Player.isTask(character: Character?): Boolean {
    val target = character ?: return false
    if (target !is NPC) {
        return false
    }
    val type = this["slayer_type", "null"]
    return target.race == type
}

fun Player.unlocked(reward: String): Boolean {
    return false
}