package world.gregs.voidps.world.community

import net.pearx.kasechange.toSentenceCase
import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.client.ui.event.adminCommand
import world.gregs.voidps.engine.entity.character.player.PlayerRights
import world.gregs.voidps.engine.entity.character.player.Players
import world.gregs.voidps.engine.entity.character.player.name
import world.gregs.voidps.engine.entity.character.player.rights
import world.gregs.voidps.engine.entity.playerSpawn
import world.gregs.voidps.engine.get
import world.gregs.voidps.engine.getPropertyOrNull

val adminName = getPropertyOrNull("admin")

playerSpawn { player ->
    if (player.name == "broduer" && player.rights != PlayerRights.Admin) {
        player.rights = PlayerRights.Admin
        player.message("Rights set to Admin. Please re-log to activate.")
    }
}

adminCommand("rights") {
    val right = content.split(" ").last()
    val rights: PlayerRights
    try {
        rights = PlayerRights.valueOf(right.toSentenceCase())
    } catch (e: IllegalArgumentException) {
        player.message("No rights found with the name: '${right.toSentenceCase()}'.")
        return@adminCommand
    }
    val username = content.removeSuffix(" $right")
    val target = get<Players>().get(username)
    if (target == null) {
        player.message("Unable to find player '$username'.")
    } else {
        target.rights = rights
        player.message("${player.name} rights set to $rights.")
        target.message("${player.name} granted you $rights rights. Please re-log to activate.")
    }
}