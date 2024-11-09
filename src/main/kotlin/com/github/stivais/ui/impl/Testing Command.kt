package com.github.stivais.ui.impl

import com.github.stivais.ui.UI
import com.github.stivais.ui.UIScreen.Companion.open
import me.odinmain.commands.commodore
import me.odinmain.features.impl.dungeon.LeapMenu.leapMenu


val `ui command` = commodore("ui") {
    literal("test").runs {
        open(
            UI {

//                val height = Animatable(from = 100.px, to = Bounding)
//                Column(
//                    size(Bounding, h = height),
//                    null,
//                    null
//                ).scope {
//                    repeat(5) {
//                        block(
//                            size(100.px, 100.px),
//                            Color.RED
//                        ) {
//                            text("$it")
//                        }
//                    }
//                    onClick {
//                        modMessage("hi")
//                        height.animate(0.25.seconds, Animations.EaseInOutQuint)
//                        redraw()
//                        true
//                    }
//                }
            }
        )
    }
    literal("leap").runs {
        open(leapMenu())
    }
}