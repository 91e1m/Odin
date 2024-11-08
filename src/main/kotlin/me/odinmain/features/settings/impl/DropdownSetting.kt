package me.odinmain.features.settings.impl

import com.github.stivais.ui.animation.Animations
import com.github.stivais.ui.constraints.at
import com.github.stivais.ui.constraints.constrain
import com.github.stivais.ui.constraints.percent
import com.github.stivais.ui.constraints.px
import com.github.stivais.ui.elements.scope.ElementDSL
import com.github.stivais.ui.transforms.rotation
import com.github.stivais.ui.utils.seconds
import me.odinmain.features.settings.Setting
import me.odinmain.features.settings.Setting.Renders.Companion.setting

/**
 * A setting intended to show or hide other settings in the GUI.
 *
 * @author Bonsai
 */
class DropdownSetting(
    name: String,
    override val default: Boolean = false
): Setting<Boolean>(name, false, ""), Setting.Renders {

    override var value: Boolean = default

    override fun ElementDSL.create() = setting {
        text(
            name,
            pos = at(6.px),
            size = 40.percent
        )
        image(
            image = "/assets/odinmain/clickgui/chevron.svg",
            constraints = constrain(-6.px, w = 30.px, h = 30.px)
        ) {
            val rotate = rotation(
                from = 270f,
                to = 90f
            )
            onClick {
                rotate.animate(0.25.seconds, Animations.EaseInOutQuint)
                value = !value
                this@setting.redraw()
                true
            }
        }
    }
}