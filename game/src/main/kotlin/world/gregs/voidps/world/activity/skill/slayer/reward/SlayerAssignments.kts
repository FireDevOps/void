package world.gregs.voidps.world.activity.skill.slayer.reward

import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.client.ui.chat.Colours
import world.gregs.voidps.engine.client.ui.event.interfaceOpen
import world.gregs.voidps.engine.client.ui.interfaceOption
import world.gregs.voidps.engine.client.ui.menu
import world.gregs.voidps.engine.client.ui.open
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.world.activity.skill.slayer.slayerPoints

interfaceOption("Learn", "learn", "slayer_rewards_assignment") {
    player.open("slayer_rewards_learn")
}

interfaceOption("Buy", "buy", "slayer_rewards_assignment") {
    player.open("slayer_rewards")
}

interfaceOpen("slayer_rewards_assignment") { player ->
    refresh(player, id)
}

fun refresh(player: Player, id: String) {
    val points = player.slayerPoints
    player.interfaces.sendText(id, "current_points", points.toString())
    player.interfaces.sendColour(id, "current_points", if (points == 0) Colours.RED else Colours.GOLD)
    for (i in 0 until 5) {
        player.interfaces.sendText(id, "text_$i", "nothing")
    }
    val assignment = player["slayer_assignment", ""]
    if (assignment.isEmpty()) {
        player.interfaces.sendText(id, "reassign_text", "You must have an assignment to use this.")
        player.interfaces.sendText(id, "block_text", "You must have an assignment to use this.")
    } else {
        player.interfaces.sendText(id, "reassign_text", "Cancel task of ${assignment}.")
        player.interfaces.sendText(id, "block_text", "Never assign $assignment again.")
    }
    player.interfaces.sendColour(id, "reassign_text", if (points < 30) Colours.RED else Colours.ORANGE)
    player.interfaces.sendColour(id, "reassign_points", if (points < 30) Colours.RED else Colours.ORANGE)
    player.interfaces.sendColour(id, "block_text", if (points < 100) Colours.RED else Colours.ORANGE)
    player.interfaces.sendColour(id, "block_points", if (points < 100) Colours.RED else Colours.ORANGE)
}