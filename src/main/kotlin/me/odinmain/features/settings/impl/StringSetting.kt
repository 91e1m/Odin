package me.odinmain.features.settings.impl

import com.github.stivais.ui.animation.Animations
import com.github.stivais.ui.color.Color
import com.github.stivais.ui.color.brighter
import com.github.stivais.ui.color.color
import com.github.stivais.ui.constraints.*
import com.github.stivais.ui.constraints.measurements.Animatable
import com.github.stivais.ui.constraints.sizes.AspectRatio
import com.github.stivais.ui.constraints.sizes.Copying
import com.github.stivais.ui.elements.scope.ElementDSL
import com.github.stivais.ui.renderer.Image
import com.github.stivais.ui.utils.animate
import com.github.stivais.ui.utils.radius
import com.github.stivais.ui.utils.seconds
import com.google.gson.JsonElement
import com.google.gson.JsonPrimitive
import me.odinmain.features.impl.render.ClickGUI
import me.odinmain.features.impl.render.ClickGUI.`gray 38`
import me.odinmain.features.settings.Saving
import me.odinmain.features.settings.Setting
import me.odinmain.features.settings.Setting.Renders.Companion.setting
import me.odinmain.utils.ui.elements.TextInput
import me.odinmain.utils.ui.textInput

/**
 * Setting that lets you type a string.
 * @author Aton, Stivais
 */
class StringSetting(
    name: String,
    override val default: String = "",
    var length: Int = 20,
    hidden: Boolean = false,
    description: String = "",
) : Setting<String>(name, hidden, description), Saving, Setting.Renders {

    override var value: String = default
        set(value) {
            field = if (value.length <= length) value else return
        }

    private var censors = false

    fun censors(): StringSetting {
        censors = true
        return this
    }

    override fun write(): JsonElement {
        return JsonPrimitive(value)
    }

    override fun read(element: JsonElement?) {
        element?.asString?.let {
            value = it
        }
    }

    override fun ElementDSL.create() = setting(70.px) {
        column(size(w = Copying), padding = 5.px) {
            section(size = 20.px) {
                text(
                    name,
                    pos = at(x = 6.px),
                    size = 80.percent
                )
            }
            val thickness = Animatable(from = 1.px, to = 1.75.px)
            val hover = Color.Animated(from = `gray 38`, to = color { `gray 38`.rgba.brighter(1.2) })

            block(
                size(w = 95.percent, h = 30.px),
                color = hover,
                outlineColor = ClickGUI.color,
                outlineThickness = thickness,
                radius = 5.radius()
            ) {
                outline(
                    ClickGUI.color,
                    thickness,
                )
                onMouseEnterExit {
                    hover.animate(0.25.seconds, Animations.Linear)
                }

                val maxWidth = if (censors) 80.percent else 95.percent
                val input = textInput(
                    text = value,
                    constraints = at(x = 6.px),
                    size = 50.percent,
                    maxWidth = maxWidth,
                    censored = censors,
                    onTextChange = { event ->
                        val str = event.string
                        if (str.length <= length) value = str else event.cancel()
                    }
                ).apply {
                    onFocusGain { thickness.animate(0.25.seconds) }
                    onFocusLost { thickness.animate(0.25.seconds) }
                }
                onClick {
                    input.focusThis()
                    true
                }
                if (censors) {
                    image(
                        Image("/assets/odinmain/clickgui/visibility-show.svg", type = Image.Type.VECTOR),
                        constraints = constrain(-6.px, w = AspectRatio(1f), h = 75.percent)
                    ) {
                        onClick {
                            (input.element as TextInput).censorInput = !(input.element).censorInput
                            if (input.element.censorInput) {
                                element.image = Image("/assets/odinmain/clickgui/visibility-show.svg")
                            } else {
                                element.image = Image("/assets/odinmain/clickgui/visibility-off.svg")
                            }
                            true
                        }
                    }
                }
            }
        }
    }
}