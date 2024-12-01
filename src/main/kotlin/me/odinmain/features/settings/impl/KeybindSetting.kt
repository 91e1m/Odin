package me.odinmain.features.settings.impl

import com.github.stivais.aurora.animations.Animation
import com.github.stivais.aurora.constraints.impl.measurements.Animatable
import com.github.stivais.aurora.constraints.impl.measurements.Pixel
import com.github.stivais.aurora.constraints.impl.size.Bounding
import com.github.stivais.aurora.dsl.*
import com.github.stivais.aurora.elements.ElementScope
import com.github.stivais.aurora.elements.impl.Block.Companion.outline
import com.github.stivais.aurora.elements.impl.Text.Companion.string
import com.github.stivais.aurora.elements.impl.popup
import com.google.gson.JsonElement
import com.google.gson.JsonPrimitive
import me.odinmain.features.impl.render.ClickGUI
import me.odinmain.features.impl.render.ClickGUI.`gray 38`
import me.odinmain.features.settings.Saving
import me.odinmain.features.settings.Setting
import me.odinmain.features.settings.Setting.Renders.Companion.onValueChanged
import me.odinmain.features.settings.Setting.Renders.Companion.setting
import org.lwjgl.input.Keyboard.*
import org.lwjgl.input.Mouse

class KeybindSetting(
    name: String,
    override val default: Keybinding,
    description: String,
) : Setting<Keybinding>(name, false, description), Saving, Setting.Renders {

    constructor(name: String, key: Int, description: String) : this(name, Keybinding(key), description)

    override var value: Keybinding = default

    private val keyName: String
        get() {
            val key = value.key
            return when {
                key > 0 -> getKeyName(key) ?: "Error"
                key < 0 -> {
                    when (val button = key + 100) {
                        0 -> "Left Button"
                        1 -> "Right Button"
                        2 -> "Middle Button"
                        else -> "Button $button"
                    }
                }
                else -> "None"
            }
        }

    override fun reset() {
        value.key = default.key
    }

    override fun write(): JsonElement {
        return JsonPrimitive(value.key)
    }

    override fun read(element: JsonElement?) {
        element?.asInt?.let {
            value.key = it
        }
    }

    override fun ElementScope<*>.create() = setting {
        text(
            name,
            pos = at(x = Pixel.ZERO),
            size = 40.percent,
        )

        val outline = Animatable(from = 1.px, to = 2.5.px)

        block(
            constraints = constrain(x = Pixel.ZERO.alignOpposite, w = Bounding + 6.px, h = 70.percent),
            color = `gray 38`,
            radius = 5.radius()
        ) {
            outline(
                color = ClickGUI.color,
                thickness = outline
            )
            hoverEffect(
                factor = 1.25f
            )
            text(
                string = keyName
            ) {
                onValueChanged {
                    string = keyName
                }
            }
            onClick {
                // creates a popup which consumes any input and closes the popup and sets the keybind

                val popup = popup(copies(), smooth = false) {
                    onFocusChanged {
                        outline.animate(0.25.seconds, style = Animation.Style.Linear)
                    }
                    onClick(nonSpecific = true) { (button) ->
                        value.key = -100 + button
                        ui.unfocus()
                        closePopup()
                        true
                    }
                    onKeycodePressed { (code) ->
                        value.key = when (code) {
                            KEY_ESCAPE, KEY_BACK -> 0
                            KEY_NUMPADENTER, KEY_RETURN -> value.key
                            else -> code
                        }
                        ui.unfocus()
                        closePopup()
                        true
                    }
                }
                ui.focus(popup.element)
            }
        }
    }

    /**
     * Action to do, when keybinding is pressed
     *
     * Note: Action is always invoked, even if module isn't enabled.
     */
    fun onPress(block: () -> Unit): KeybindSetting {
        value.onPress = block
        return this
    }
}

data class Keybinding(var key: Int) {

    /**
     * Intended to active when keybind is pressed.
     */
    var onPress: (() -> Unit)? = null

    /**
     * @return `true` if [key] is held down.
     */
    fun isDown(): Boolean {
        return if (key == 0) false else (if (key < 0) Mouse.isButtonDown(key + 100) else isKeyDown(key))
    }
}