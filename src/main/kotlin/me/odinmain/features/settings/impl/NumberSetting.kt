package me.odinmain.features.settings.impl


import com.github.stivais.ui.animation.Animations
import com.github.stivais.ui.constraints.at
import com.github.stivais.ui.constraints.constrain
import com.github.stivais.ui.constraints.measurements.Animatable
import com.github.stivais.ui.constraints.percent
import com.github.stivais.ui.constraints.px
import com.github.stivais.ui.constraints.sizes.Copying
import com.github.stivais.ui.elements.scope.ElementDSL
import com.github.stivais.ui.elements.scope.hoverEffect
import com.github.stivais.ui.utils.radius
import com.github.stivais.ui.utils.seconds
import com.google.gson.JsonElement
import com.google.gson.JsonPrimitive
import me.odinmain.features.impl.render.ClickGUI
import me.odinmain.features.impl.render.ClickGUI.`gray 26`
import me.odinmain.features.settings.Saving
import me.odinmain.features.settings.Setting
import me.odinmain.features.settings.Setting.Renders.Companion.elementWidth
import me.odinmain.features.settings.Setting.Renders.Companion.onValueChanged
import me.odinmain.features.settings.Setting.Renders.Companion.setting
import me.odinmain.utils.round
import kotlin.math.floor
import kotlin.math.round

/**
 * Setting that lets you pick a value between a range
 *
 * @param min The minimum a value can be
 * @param max The maximum a value can be
 * @param increment The increment for the setting
 * @param unit The suffix for value in the UI (It is recommended to set this for better UX)
 */
@Suppress("UNCHECKED_CAST")
class NumberSetting<E>(
    name: String,
    override val default: E = 1.0 as E,
    min: Number = -10000,
    max: Number = 10000,
    increment: Number = 1,
    hidden: Boolean = false,
    description: String = "",
    val unit: String = "",
) : Setting<E>(name, hidden, description), Saving, Setting.Renders where E : Number, E : Comparable<E> {

    override var value: E = default

    /** The amount a setting should increment. */
    private val increment = increment.toDouble()

    /** The minimum a setting can be */
    private val min = min.toDouble()

    /** The maximum a setting can be */
    private val max = max.toDouble()

    private val text: String
        get() {
            val double = value.toDouble()
            val number = if (double - floor(double) == 0.0) value.toInt() else double.round(2)
            return "$number$unit"
        }

    override fun ElementDSL.create() = setting(45.px) {
        text(
            name,
            pos = at(6.px, 10.px),
            size = 35.percent
        )
        val display = text(
            text,
            pos = at(x = -(6.px), y = 10.px),
            size = 35.percent
        )

        val sliderWidth = Animatable.Raw((((value.toDouble() - min) / (max - min)) * (elementWidth.pixels * 0.95)).toFloat())

        block(
            constraints = constrain(y = 75.percent, w = 95.percent, h = 20.percent),
            color = `gray 26`,
            radius = 4.radius()
        ) {
            block(
                constraints = constrain(0.px, 0.px, sliderWidth, Copying),
                color = ClickGUI.color,
                radius = 4.radius()
            ).hoverEffect(handler = this@block)

            var dragging = false
            // used to animate on only click
            var first = true

            onClick {
                first = true
                dragging = true
                val percent = ((ui.mx - element.x).coerceIn(0f, element.width) / element.width).round(2).toFloat()
                set(percent * (max - min) + min)
                true
            }
            onMouseMove {
                if (dragging) {
                    val percent = ((ui.mx - element.x).coerceIn(0f, element.width) / element.width).round(2).toFloat()
                    set(percent * (max - min) + min)
                }
                dragging
            }
            onRelease {
                dragging = false
            }

            onValueChanged {
                val to = ((value.toDouble() - min) / (max - min) * element.width).toFloat()
                if (first || !dragging) {
                    first = false
                    sliderWidth.animate(to = to, 0.75.seconds, Animations.EaseOutQuint)
                } else {
                    sliderWidth.to(to = to)
                }
                display.string = text
            }
        }
    }

    override fun write(): JsonElement {
        return JsonPrimitive(value)
    }

    override fun read(element: JsonElement?) {
        element?.asNumber?.let {
            value = it as E
        }
    }

    fun set(new: Number) {
        value = (round((new.toDouble() / increment)) * increment).coerceIn(min, max) as E
    }
}
