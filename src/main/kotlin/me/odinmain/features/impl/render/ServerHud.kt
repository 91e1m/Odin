@file:Suppress("UNUSED")

package me.odinmain.features.impl.render

import com.github.stivais.ui.constraints.px
import me.odinmain.features.Module
import me.odinmain.utils.ServerUtils
import me.odinmain.utils.ui.TextHUD
import me.odinmain.utils.ui.and

object ServerHud : Module(
    name = "Performance Display",
    description = "Displays certain performance-related metrics, like ping, TPS and FPS."
) {
    private val fpsHUD by TextHUD(
        "FPS",
        description = "Display's your fps on screen."
    ) { color, font ->
        text(
            text = "FPS ",
            color = color,
            size = 30.px
        ) and text({ getFPS() }, font = font)
    }

    private val pingHUD by TextHUD(
        "Ping",
        description = "Display's your ping on screen."
    ) { color, font ->
        text(
            text = "FPS ",
            color = color,
            size = 30.px
        ) and text({ ServerUtils.averagePing.toInt() }, font = font)
    }

    private val tpsHUD by TextHUD(
        "TPS",
        description = "Display's your tps on screen."
    ) { color, font ->
        text(
            text = "TPS ",
            color = color,
            size = 30.px,
            font = font,
        ) and text({ ServerUtils.averageTps.toInt() }, font = font)
    }

    fun getFPS() = mc.debug.split(" ")[0].toIntOrNull() ?: 0
}