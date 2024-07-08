package world.gregs.voidps.world.activity.skill.slayer.reward

import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.client.ui.chat.Colours
import world.gregs.voidps.engine.client.ui.event.interfaceOpen
import world.gregs.voidps.engine.client.ui.interfaceOption
import world.gregs.voidps.engine.client.ui.open
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.world.activity.skill.slayer.slayerPoints

interfaceOption("Learn", "learn", "slayer_rewards") {
    player.open("slayer_rewards_learn")
}

interfaceOption("Assignments", "assignment", "slayer_rewards") {
    player.open("slayer_rewards_assignment")
}

interfaceOpen("slayer_rewards") { player ->
    refreshText(player, id)
}

fun refreshText(player: Player, id: String) {
    val points = player.slayerPoints
    player.interfaces.sendText(id, "current_points", points.toString())
    player.interfaces.sendColour(id, "current_points", if (points == 0) Colours.RED else Colours.GOLD)
    player.interfaces.sendColour(id, "buy_xp_text", if (points < 400) Colours.RED else Colours.ORANGE)
    player.interfaces.sendColour(id, "buy_xp_points", if (points < 400) Colours.RED else Colours.ORANGE)
    player.interfaces.sendColour(id, "buy_ring_text", if (points < 75) Colours.RED else Colours.ORANGE)
    player.interfaces.sendColour(id, "buy_ring_points", if (points < 75) Colours.RED else Colours.ORANGE)
    val colour = if (points < 35) Colours.RED else Colours.ORANGE
    player.interfaces.sendColour(id, "buy_runes_text", colour)
    player.interfaces.sendColour(id, "buy_runes_points", colour)
    player.interfaces.sendColour(id, "buy_bolts_text", colour)
    player.interfaces.sendColour(id, "buy_bolts_points", colour)
    player.interfaces.sendColour(id, "buy_arrows_text", colour)
    player.interfaces.sendColour(id, "buy_arrows_points", colour)
}

interfaceOption("Buy XP", "buy_xp_*", "slayer_rewards") {
}

interfaceOption("Buy Ring", "buy_ring_*", "slayer_rewards") {
}

interfaceOption("Buy Runes", "buy_runes_*", "slayer_rewards") {
    player.message("Here are your runes. Use them wisely.")
}

interfaceOption("Buy Bolts", "buy_bolts_*", "slayer_rewards") {
}

interfaceOption("Buy Arrows", "buy_arrows_*", "slayer_rewards") {
}