package me.odinmain.features.impl.dungeon

import com.github.stivais.aurora.color.Color
import me.odinmain.features.Module
import me.odinmain.features.huds.HUD.Companion.needs
import me.odinmain.features.huds.HUD.Companion.preview
import me.odinmain.features.settings.impl.BooleanSetting
import me.odinmain.utils.ui.TextHUD
import me.odinmain.utils.ui.buildText

object WarpCooldown : Module (
    name = "Warp Cooldown",
    description = "Displays the time until you can warp into a dungeon again."
) {
    private val showUnit by BooleanSetting("Show unit", default = false, description = "Displays unit of time for the cooldown.").hide()

    private val HUD by TextHUD("Warp HUD") { color, font, shadow ->
        needs { lastUpdate - System.currentTimeMillis() >= 0 }
        buildText(
            string = "Warp",
            supplier = { getString(preview) },
            font, color, Color.WHITE, shadow
        )
    }.registerSettings(::showUnit).setting(description = "Displays the cooldown.")

    private var lastUpdate: Long = System.currentTimeMillis()

    init {
        onMessage(Regex("^-*>newLine<-\\[[^]]+] (\\w+) entered (?:MM )?\\w+ Catacombs, Floor (\\w+)!->newLine<-*$")) {
            lastUpdate = System.currentTimeMillis() + 30_000
        }
    }

    private fun getString(isPreview: Boolean): String {
        return "${if (isPreview) "30" else (lastUpdate - System.currentTimeMillis()) / 1000}${if (showUnit) "s" else ""}"
    }
}