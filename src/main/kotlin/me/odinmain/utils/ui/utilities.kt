package me.odinmain.utils.ui

import com.github.stivais.ui.UIScreen
import com.github.stivais.ui.animation.Animations
import com.github.stivais.ui.color.Color
import com.github.stivais.ui.constraints.*
import com.github.stivais.ui.constraints.positions.Linked
import com.github.stivais.ui.elements.impl.TextScope
import com.github.stivais.ui.elements.scope.ElementDSL
import com.github.stivais.ui.renderer.Font
import com.github.stivais.ui.transforms.alpha
import com.github.stivais.ui.transforms.scale
import me.odinmain.features.Module
import me.odinmain.features.huds.HUDScope
import me.odinmain.features.settings.impl.ColorSetting
import me.odinmain.features.settings.impl.HUDSetting
import me.odinmain.features.settings.impl.SelectorSetting
import me.odinmain.utils.ui.elements.TextInput

val regularFont = Font("Regular", "/assets/odinmain/fonts/Regular.otf")
val mcFont = Font("Minecraft", "/assets/odinmain/fonts/Minecraft-Regular.otf")

// todo: better solution
infix fun TextScope.and(other: TextScope) {
    other.element.constraints.x = Linked(element)
    other.size = size
}

fun ElementDSL.outline(
    constraints: Constraints,
    color: Color,
    thickness: Measurement = 1.px,
    radius: FloatArray? = null
) {
    block(constraints, Color.TRANSPARENT, color, thickness, radius)
}

/**
 * Makes a HUD, that uses common settings found in text-based HUDs.
 *
 * @param color Default color for the setting provided
 */
@Suppress("FunctionName")
inline fun Module.TextHUD(
    name: String,
    description: String,
    color: Color = Color.RGB(50, 150, 220),
    crossinline block: HUDScope.(Color, Font) -> Unit
): HUDSetting {
    val colorSetting = ColorSetting("Color", color, allowAlpha = false)
    val fontSetting = SelectorSetting("Font", arrayListOf("Regular", "Minecraft"))
    // copy of selector setting, where each entry is a different font representing it

    val hudSetting = HUD(name, description) {
        val font = when (fontSetting.value) {
            1 -> mcFont
            else -> regularFont
        }
        block(colorSetting.value, font)
    }
    hudSetting.value.registerSettings(
        colorSetting,
        fontSetting,
    )
    return hudSetting
}

/**
 * Allows code to be run after certain amount of time hovering over the element
 */
inline fun ElementDSL.onHover(duration: Float, crossinline block: () -> Unit) {
    onMouseEnter {
        val start = System.nanoTime()
        operation {
            if (System.nanoTime() - start >= duration) {
                block()
                return@operation true
            }
            !element.isInside(ui.mx, ui.my) || !element.renders
        }
    }
}

/**
 * Scale and alpha animation that gets ran when element is initialized and is uninitialized.
 *
 * @param duration length of the animation when ran
 * @param typeInitialized style of the animation when element is created
 * @param typeUninitialized style of the animation when element is removed
 */
fun ElementDSL.lifetimeAnimations(
    duration: Float,
    typeInitialized: Animations,
    typeUninitialized: Animations,
) {
    val scaleAnimation = scale(from = 0f, to = 1f, centered = true)
    val alphaAnimation = alpha(from = 0f, to = 1f)

    scaleAnimation.animate(duration, typeInitialized)
    alphaAnimation.animate(duration, typeInitialized)

    onRemove {
        UIScreen.closeAnimHandler = ui.window as UIScreen
        scaleAnimation.animate(duration, typeUninitialized)
        alphaAnimation.animate(duration, typeUninitialized)?.onFinish {
            UIScreen.closeAnimHandler = null
        }
    }
}

fun ElementDSL.textInput(
    text: String = "",
    placeholder: String = "",
    constraints: Positions? = null,
    size: Size = 50.percent,
    maxWidth: Size? = null,
    censored: Boolean = false,
    onTextChange: (event: TextInput.TextChanged) -> Unit
) = create(TextScope(TextInput(text, placeholder, constraints, size, maxWidth, censored, onTextChange = onTextChange)))