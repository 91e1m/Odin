package me.odinmain.features.settings.impl

import com.github.stivais.ui.animation.Animations
import com.github.stivais.ui.color.Color
import com.github.stivais.ui.color.color
import com.github.stivais.ui.color.darker
import com.github.stivais.ui.constraints.*
import com.github.stivais.ui.constraints.measurements.Animatable
import com.github.stivais.ui.elements.scope.ElementDSL
import com.github.stivais.ui.elements.scope.hoverEffect
import com.github.stivais.ui.utils.radius
import com.github.stivais.ui.utils.seconds
import com.google.gson.JsonElement
import com.google.gson.JsonPrimitive
import me.odinmain.features.impl.render.ClickGUI
import me.odinmain.features.impl.render.ClickGUI.`gray 38`
import me.odinmain.features.settings.Saving
import me.odinmain.features.settings.Setting
import me.odinmain.features.settings.Setting.Renders.Companion.onValueChanged
import me.odinmain.features.settings.Setting.Renders.Companion.setting

/**
 * A setting that represents a boolean.
 */
class BooleanSetting(
    name: String,
    override val default: Boolean = false,
    hidden: Boolean = false,
    description: String = "",
): Setting<Boolean>(name, hidden, description), Saving, Setting.Renders {

    override var value: Boolean = default

    override fun write(): JsonElement {
        return JsonPrimitive(value)
    }

    override fun read(element: JsonElement?) {
        if (element?.asBoolean != value) {
            value = !value
        }
    }

    override fun ElementDSL.create() = setting {
        text(
            name,
            pos = at(x = 6.px),
            size = 40.percent
        )
        val pointer = Animatable(from = 30.percent.center, to = 70.percent.center)
        val color = Color.Animated(from = `gray 38`, to = ClickGUI.color)

        if (value) {
            pointer.swap()
            color.swap()
        }

        block(
            constrain(x = -(6.px), w = 35.px, h = 50.percent),
            color,
            radius = 10.radius()
        ) {
            onClick {
                value = !value
                true
            }
            block(
                constrain(x = pointer, w = 50.percent, h = 80.percent),
                color = Color.WHITE,
                radius = 8.radius()
            )
            outline(color = color { ClickGUI.color.rgba.darker() }, thickness = 1.5.px)
            hoverEffect()
        }

        onValueChanged {
            color.animate(0.25.seconds, Animations.Linear)
            pointer.animate(0.25.seconds, Animations.EaseInOutQuint)
        }
    }
}