package me.odinmain.features.settings.impl

import com.github.stivais.aurora.animations.Animation
import com.github.stivais.aurora.constraints.impl.measurements.Pixel
import com.github.stivais.aurora.dsl.*
import com.github.stivais.aurora.elements.ElementScope
import me.odinmain.features.settings.Setting
import me.odinmain.features.settings.Setting.Renders.Companion.setting
import me.odinmain.utils.ui.image

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

    override fun ElementScope<*>.create() = setting {
        text(
            name,
            pos = at(Pixel.ZERO),
            size = 40.percent
        )
        image(
            image = "clickgui/chevron.svg".image(),
            constraints = constrain(Pixel.ZERO.alignOpposite, w = 30.px, h = 30.px)
        ) {
            val rotate = rotation(
                from = 270f,
                to = 90f
            )
            onClick {
                value = !value
                rotate.animate(0.25.seconds, Animation.Style.EaseInOutQuint)
                this@create.redraw()
            }
        }
    }
}