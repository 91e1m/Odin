package me.odinmain.features.impl.dungeon

import me.odinmain.features.Module
import me.odinmain.features.settings.impl.BooleanSetting

object WarpCooldown : Module (
    name = "Warp Cooldown",
    description = "Displays the time until you can warp into a dungeon again."
) {
    private val showUnit by BooleanSetting("Show unit", default = false, description = "Displays unit of time for the cooldown.").hide()

//    private val HUD by TextHUD("Warp HUD") { color, font ->
//        if (preview) {
//            text(
//                "Warp ",
//                color = color,
//                font = font,
//                size = 30.px
//            ) and text("30${if (showUnit) "s" else ""}", font = font)
//        } else {
//            needs { lastUpdate - System.currentTimeMillis() >= 0 }
//            text(
//                "Warp ",
//                color = color,
//                font = font,
//                size = 30.px
//            ) and text({ "${(lastUpdate - System.currentTimeMillis()) / 1000}${if (showUnit) "s" else ""}" }, font = font)
//        }
//    }.registerSettings(
//        ::showUnit
//    ).setting("Displays the cooldown.")

    private var lastUpdate: Long = System.currentTimeMillis()

    init {
        onMessage(Regex("^-*>newLine<-\\[[^]]+] (\\w+) entered (?:MM )?\\w+ Catacombs, Floor (\\w+)!->newLine<-*$")) {
            lastUpdate = System.currentTimeMillis() + 30_000
        }
    }
}