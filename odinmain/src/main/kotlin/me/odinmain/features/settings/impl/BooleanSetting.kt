package me.odinmain.features.settings.impl

import com.github.stivais.ui.animation.Animations
import com.github.stivais.ui.color.Color
import com.github.stivais.ui.color.brighter
import com.github.stivais.ui.color.color
import com.github.stivais.ui.constraints.at
import com.github.stivais.ui.constraints.constrain
import com.github.stivais.ui.constraints.measurements.Animatable
import com.github.stivais.ui.constraints.measurements.center
import com.github.stivais.ui.constraints.percent
import com.github.stivais.ui.constraints.px
import com.github.stivais.ui.elements.scope.ElementScope
import com.github.stivais.ui.elements.scope.button
import com.github.stivais.ui.impl.ClickGUITheme
import com.github.stivais.ui.impl.`gray 38`
import com.github.stivais.ui.utils.animate
import com.github.stivais.ui.utils.radii
import com.github.stivais.ui.utils.seconds
import com.google.gson.JsonElement
import com.google.gson.JsonPrimitive
import me.odinmain.features.impl.render.ClickGUIModule
import me.odinmain.features.settings.Saving
import me.odinmain.features.settings.Setting

/**
 * A setting that represents a boolean.
 */
class BooleanSetting(
    name: String,
    override val default: Boolean = false,
    hidden: Boolean = false,
    description: String = "",
): Setting<Boolean>(name, hidden, description), Saving {

    override var value: Boolean = default

    var enabled: Boolean by this::value

    override fun write(): JsonElement {
        return JsonPrimitive(enabled)
    }

    override fun read(element: JsonElement?) {
        if (element?.asBoolean != enabled) {
            enabled = !enabled
        }
    }

    override fun ElementScope<*>.createElement() {
        setting(40.px) {
            text(
                text = name,
                pos = at(x = 6.px),
                size = 40.percent
            )

            if (ClickGUIModule.booleanSettingStyle == 0) {
                val pointerPos = Animatable(from = 30.percent.center, to = 70.percent.center)
                block(
                    constraints = constrain(x = -6.px, w = 35.px, h = 50.percent),
                    color = color(from = `gray 38`, to = ClickGUITheme),
                    radius = 9.radii()
                ) {
                    outline(color = Color { ClickGUITheme.rgba.brighter(0.75) }, thickness = 1.5.px)
                    block(
                        constraints = constrain(x = pointerPos, w = 50.percent, h = 80.percent),
                        color = Color.WHITE,
                        radius = 8.radii()
                    )
                    onClick {
                        color!!.animate(0.25.seconds)
                        pointerPos.animate(0.25.seconds, Animations.EaseInOutQuint)
                        value = !value
                        true
                    }
                }
            } else {
                button(
                    constraints = constrain(x = -(6.px), w = 20.px, h = 50.percent),
                    color = color(from = `gray 38`, to = ClickGUITheme),
                    on = value,
                    radii = 5.radii()
                ) {
                    onClick(0) {
                        value = !value
                        true
                    }
                    outline(color = ClickGUITheme)
                }
            }
        }
    }
}