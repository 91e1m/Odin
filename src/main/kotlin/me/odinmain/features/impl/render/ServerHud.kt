@file:Suppress("UNUSED")

package me.odinmain.features.impl.render

import me.odinmain.features.Module

object ServerHud : Module(
    name = "Performance Display",
    description = "Displays certain performance-related metrics, like ping, TPS and FPS."
) {
//    private val fpsHUD by TextHUD("FPS") { color, font ->
//        text(
//            text = "FPS ",
//            color = color,
//            size = 30.px
//        ) and text({ getFPS() }, font = font)
//    }.setting("Display's your fps on screen.")
//
//    private val pingHUD by TextHUD("Ping") { color, font ->
//        text(
//            text = "FPS ",
//            color = color,
//            size = 30.px
//        ) and text({ ServerUtils.averagePing.toInt() }, font = font)
//    }.setting("Display's your ping on screen.")
//
//    private val tpsHUD by TextHUD("TPS") { color, font ->
//        text(
//            text = "TPS ",
//            color = color,
//            size = 30.px,
//            font = font,
//        ) and text({ ServerUtils.averageTps.toInt() }, font = font)
//    }.setting("Display's your tps on screen.")

    fun getFPS() = mc.debug.split(" ")[0].toIntOrNull() ?: 0
}