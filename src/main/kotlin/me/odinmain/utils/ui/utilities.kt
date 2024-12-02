package me.odinmain.utils.ui

import com.github.stivais.aurora.animations.Animation
import com.github.stivais.aurora.color.Color
import com.github.stivais.aurora.constraints.Constraint
import com.github.stivais.aurora.constraints.Positions
import com.github.stivais.aurora.dsl.at
import com.github.stivais.aurora.dsl.onMouseEnter
import com.github.stivais.aurora.dsl.onRemove
import com.github.stivais.aurora.dsl.px
import com.github.stivais.aurora.elements.ElementScope
import com.github.stivais.aurora.elements.impl.Text.Companion.shadow
import com.github.stivais.aurora.elements.impl.Text.Companion.textSupplied
import com.github.stivais.aurora.renderer.data.Font
import com.github.stivais.aurora.renderer.data.Image
import com.github.stivais.aurora.transforms.impl.Alpha
import com.github.stivais.aurora.transforms.impl.Scale
import me.odinmain.features.Module
import me.odinmain.features.huds.HUD
import me.odinmain.features.settings.impl.BooleanSetting
import me.odinmain.features.settings.impl.ColorSetting
import me.odinmain.features.settings.impl.SelectorSetting
import me.odinmain.utils.ui.screens.UIHandler

val regularFont = Font("Regular", "/assets/odinmain/fonts/Regular.otf")
val mcFont = Font("Minecraft", "/assets/odinmain/fonts/Minecraft-Regular.otf")

/**
 * Utility function to get an image from a string representing a path inside /assets/odinmain/
 */
fun String.image() = Image("/assets/odinmain/$this")

//// todo: better solution
//infix fun TextScope.and(other: TextScope) {
//    other.element.constraints.x = Linked(element)
//    other.size = size
//}

inline fun ElementScope<*>.buildText(
    string: String,
    crossinline supplier: () -> Any?,
    font: Font,
    color1: Color,
    color2: Color,
    shadow: Boolean,
    pos: Positions = at(),
    size: Constraint.Size = 30.px
) {
    row(pos) {
        text(
            string = "$string ",
            font,
            color1,
            size = size
        ).shadow = shadow
        textSupplied(
            supplier,
            font,
            color2,
            size = size
        ).shadow = shadow
    }
}

/**
 * Makes a HUD, that uses common settings found in text-based HUDs.
 *
 * @param color Default color for the setting provided
 */
@Suppress("FunctionName")
inline fun Module.TextHUD(
    name: String,
    color: Color = Color.RGB(50, 150, 220),
    crossinline block: ElementScope<HUD.Representation>.(Color, Font, shadow: Boolean) -> Unit
): HUD {
    val colorSetting = ColorSetting("Color", color, allowAlpha = false)
    val fontSetting = SelectorSetting("Font", arrayListOf("Regular", "Minecraft"))
    val shadowSetting = BooleanSetting("Shadow", true)

    val hud = HUD(name) {
        val font = when (fontSetting.value) {
            1 -> mcFont
            else -> regularFont
        }
        block(colorSetting.value, font, shadowSetting.value)
    }
    hud.registerSettings(
        colorSetting,
        fontSetting,
        shadowSetting
    )
    return hud
}

/**
 * Allows code to be run after certain amount of time hovering over the element
 */
inline fun ElementScope<*>.onHover(duration: Float, crossinline block: () -> Unit) {
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
fun ElementScope<*>.lifetimeAnimations(
    duration: Float,
    typeInitialized: Animation.Style,
    typeUninitialized: Animation.Style,
) {
    val scaleAnimation = Scale.Animated(from = 0f, to = 1f, centered = true)
    val alphaAnimation = Alpha.Animated(from = 0f, to = 1f)

    transform(scaleAnimation)
    transform(alphaAnimation)

    scaleAnimation.animate(duration, typeInitialized)
    alphaAnimation.animate(duration, typeInitialized)

    onRemove {
        val handler = UIHandler(ui)
        handler.open(init = false)
        scaleAnimation.animate(duration, typeUninitialized)
        alphaAnimation.animate(duration, typeUninitialized)?.onFinish {
            handler.close()
        }
    }
}