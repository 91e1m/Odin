package com.github.stivais.ui.impl

import com.github.stivais.ui.UI
import com.github.stivais.ui.UIScreen.Companion.open
import com.github.stivais.ui.color.Color
import com.github.stivais.ui.constraints.constrain
import com.github.stivais.ui.constraints.percent
import com.github.stivais.ui.constraints.positions.Center
import com.github.stivais.ui.constraints.size
import com.github.stivais.ui.constraints.sizes.AspectRatio
import com.github.stivais.ui.elements.impl.Grid
import com.github.stivais.ui.utils.radius
import me.odinmain.commands.commodore
import me.odinmain.features.impl.dungeon.LeapMenu.leapMenu
import me.odinmain.features.impl.floor7.p3.TerminalSolver.customGuiColor
import me.odinmain.features.impl.floor7.p3.TerminalSolver.panesColor
import me.odinmain.utils.skyblock.modMessage


val `ui command` = commodore("ui") {
    literal("test").runs {
        open(
            UI {
                block(constrain(Center, Center, 45.percent, 25.percent), customGuiColor, radius = 12f.radius()) {
                    Grid(constraints = constrain(Center, Center, 90.percent, 65.percent)).scope {
                        repeat(18) { index ->
                            //if (index !in TerminalSolver.currentTerm.solution) return@repeat
                            block(size(11.11.percent, AspectRatio(1f)), panesColor) {
                                onClick {
                                    modMessage("Clicked $index")
                                    true
                                }
                            }.outline(Color.BLACK)
                        }
                    }
                }

            }
        )
    }
    literal("leap").runs {
        open(leapMenu())
    }
}