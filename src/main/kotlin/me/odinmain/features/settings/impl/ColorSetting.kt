package me.odinmain.features.settings.impl

import com.github.stivais.ui.animation.Animations
import com.github.stivais.ui.color.*
import com.github.stivais.ui.constraints.*
import com.github.stivais.ui.constraints.measurements.Animatable
import com.github.stivais.ui.constraints.sizes.AspectRatio
import com.github.stivais.ui.constraints.sizes.Bounding
import com.github.stivais.ui.constraints.sizes.Copying
import com.github.stivais.ui.elements.impl.Popup
import com.github.stivais.ui.elements.impl.popup
import com.github.stivais.ui.elements.scope.ElementDSL
import com.github.stivais.ui.elements.scope.LayoutScope
import com.github.stivais.ui.elements.scope.draggable
import com.github.stivais.ui.elements.scope.hoverEffect
import com.github.stivais.ui.renderer.Gradient.LeftToRight
import com.github.stivais.ui.renderer.Gradient.TopToBottom
import com.github.stivais.ui.transforms.Transforms
import com.github.stivais.ui.utils.radius
import com.github.stivais.ui.utils.seconds
import com.google.gson.JsonElement
import com.google.gson.JsonPrimitive
import me.odinmain.features.impl.render.ClickGUI.favoriteColors
import me.odinmain.features.impl.render.ClickGUI.`gray 38`
import me.odinmain.features.settings.Saving
import me.odinmain.features.settings.Setting
import me.odinmain.features.settings.Setting.Renders.Companion.onValueChanged
import me.odinmain.features.settings.Setting.Renders.Companion.setting
import me.odinmain.utils.ui.outline
import me.odinmain.utils.ui.passEvent
import me.odinmain.utils.ui.textInput
import java.awt.Color.HSBtoRGB

class ColorSetting(
    name: String,
    color: Color,
    val allowAlpha: Boolean = true,
    description: String = "",
    hidden: Boolean = false
) : Setting<Color.HSB>(name, hidden, description), Saving, Setting.Renders {

    override val default: Color.HSB = color.toHSB()

    override var value: Color.HSB = default

    /**
     * Reference for the color picker popup.
     */
    private var popup: Popup? = null

    override fun read(element: JsonElement?) {
        if (element?.asString?.startsWith("#") == true) {
            value = colorFrom(element.asString).toHSB()
        } else {
            element?.asInt?.let { value = Color.RGB(it).toHSB() }
        }
    }

    override fun write(): JsonElement {
        return JsonPrimitive(value.toHexString())
    }

    override fun ElementDSL.create() = setting(40.px) {
        text(
            name,
            pos = at(x = 6.px),
            size = 40.percent
        )
        outline(
            constrain(-(6.px), w = 30.px, h = 50.percent),
            color { value.withAlpha(255).rgba },
            thickness = 1.px,
            radius = 5.radius()
        ) {
            block(
                constraints = indent(2),
                color = value,
                radius = 4.radius()
            )
            onClick {
                popup?.closePopup()
                popup = colorPicker()
                true
            }
            onValueChanged { event ->
                popup?.let {
                    passEvent(event, it)
                }
            }
        }
    }

    private fun ElementDSL.colorPicker() = popup(smooth = true) {
        val mainColor = color { HSBtoRGB(value.hue, value.saturation, 1f) }
        val colorHue = color { HSBtoRGB(value.hue, 1f, 1f) }

        // used to consume event
        onClick { false; }
        draggable(button = 1)

        block(
            copies(),
            color = Color.RGB(22, 22, 22),
            radius = 10.radius()
        ) {
            outline(
                mainColor,
                thickness = 1.px
            )
        }

        // padding in constructor doesn't work, ill fix it in rewrite
        column(size(Bounding, Bounding)) {
            divider(10.px)
            //------------------------//
            // title and close button //
            //------------------------//
            section(20.px) {
                text(
                    name,
                    pos = at(5.percent),
                    size = 75.percent
                )
                image(
                    "/assets/odinmain/clickgui/close_icon.svg",
                    constrain(x = 95.percent.alignRight, w = AspectRatio(1f), h = 80.percent)
                ) {
                    onClick {
                        closePopup()
                        popup = null
                        true
                    }
                }
            }
            divider(7.5.px)

            row(at(x = 12.5.px)) {
                saturationBrightness(mainColor, colorHue)
                divider(7.5.px)
                hueSlider(mainColor)
                if (allowAlpha) {
                    divider(7.5.px)
                    alphaSlider(mainColor, colorHue)
                }
                divider(7.5.px)
            }
            divider(7.5.px)
            hexTextInput(mainColor)
            divider(7.5.px)
            favoriteColors(mainColor)

            divider(12.5.px)
        }
    }

    private fun ElementDSL.saturationBrightness(mainColor: Color, colorHue: Color) = outline(
        size(w = 175.px, h = 175.px),
        mainColor,
        thickness = 1.px,
        radius = 7.5.radius()
    ) {
        block(
            constraints = indent(2),
            colors = Color.WHITE to colorHue,
            radius = 6.radius(),
            gradient = LeftToRight
        ) {
            block(
                constraints = copies(),
                colors = transparentFix to Color.BLACK,
                radius = 6.radius(),
                gradient = TopToBottom
            )
        }

        val pointerX = Animatable.Raw((175 * value.saturation).coerceIn(8f, 167f))
        val pointerY = Animatable.Raw((175 * (1f - value.brightness)).coerceIn(8f, 167f))

        block(
            constrain(pointerX.center, pointerY.center, 10.px, 10.px),
            color = value,
            radius = 5.radius()
        ).outline(Color.WHITE)

        pointerHandler(
            pointerX,
            pointerY,
            apply = { px, py ->
                value.saturation = px
                value.brightness = 1f - py
            },
            block = {
                value.saturation to 1f - value.brightness
            }
        )
    }

    private fun ElementDSL.hueSlider(mainColor: Color) = outline(
        size(w = 16.px, h = 175.px),
        mainColor,
        thickness = 1.px,
        radius = 6.radius()
    ) {
        image(
            "/assets/odinmain/clickgui/HueGradient.png",
            constraints = indent(2),
            radius = 5.radius()
        )

        val pointerY = Animatable.Raw((175 * (1f - value.hue)).coerceIn(8f, 167f))

        block(
            constrain(y = pointerY.center, w = 10.px, h = 10.px),
            color = value,
            radius = 5.radius()
        ).outline(Color.WHITE)

        pointerHandler(
            pointerX = null,
            pointerY,
            apply = { _, py ->
                value.hue = 1f - py
            },
            block = {
                0f to 1f - value.hue
            }
        )
    }

    private fun ElementDSL.alphaSlider(mainColor: Color, colorHue: Color) = outline(
        size(w = 16.px, h = 175.px),
        mainColor,
        thickness = 1.px,
        radius = 6.radius()
    ) {
        block(
            constraints = indent(2),
            colors = colorHue to transparentFix,
            gradient = TopToBottom,
            radius = 5.radius(),
        )

        val pointerY = Animatable.Raw((175 * value.alpha).coerceIn(8f, 167f))

        block(
            constrain(y = pointerY.center, w = 10.px, h = 10.px),
            color = value,
            radius = 5.radius()
        ).outline(Color.WHITE)

        pointerHandler(
            pointerX = null,
            pointerY,
            apply = { _, py ->
                value.alpha = 1f - py
            },
            block = {
                0f to 1f - value.alpha
            }
        )
    }

    /**
     * Creates a text input that lets you edit this color via hexadecimal input
     */
    private fun LayoutScope.hexTextInput(mainColor: Color) = section(30.px) {
        text(
            "Hex",
            pos = at(x = 5.percent),
        )
        block(
            constrain(x = 95.5.percent.alignRight, w = 70.percent, h = 90.percent),
            color = `gray 38`,
            radius = 6.radius()
        ) {
            hoverEffect(0.25.seconds)
            outline(
                mainColor,
                thickness = 1.px
            )
            val input = textInput(
                default = value.toHexString(),
                pos = at(x = 5.percent),
                onTextChange = { event ->
                    val str = event.string
                    val hexLength = if (allowAlpha) 9 else 7
                    // Validate hex input
                    if (str.length > hexLength || (str.isNotEmpty() && !str.startsWith("#")) || (str.length > 1 && !str.substring(1).all { it.isDigit() || it.lowercaseChar() in 'a'..'f' })) {
                        event.cancel()
                    } else if (str.length == hexLength) {
                        try {
                            // Parse and update color from hex immediately
                            val newColor = colorFrom(str).toHSB()
                            value.hue = newColor.hue
                            value.saturation = newColor.saturation
                            value.brightness = newColor.brightness
                            if (allowAlpha) {
                                value.alpha = newColor.alpha
                            }
                        } catch (_: Exception) {}
                    }
                }
            ).apply {
                onValueChanged {
                    this@apply.string = value.toHexString()
                }
                onFocusLost {
                    if (this@apply.string.length != (if (allowAlpha) 9 else 7)) {
                        this@apply.string = value.toHexString()
                    }
                }
            }
            onClick {
                input.focusThis()
                true
            }
        }
    }

    /**
     * Lets you favorite colors, so you can reuse them for other settings
     */
    private fun LayoutScope.favoriteColors(mainColor: Color) = row(constrain(x = 12.5.px, w = Copying)) {
        block(
            size(w = 12.5.percent, h = AspectRatio(1f)),
            color = `gray 38`,
            radius = 6.radius()
        ) {
            hoverEffect(0.25.seconds)
            outline(mainColor)

            // for "pulse" effect
            var clicked = false
            val transform = Transforms.Scale.Animated(from = 1f, to = 0.9f,)
            image(
                "/assets/odinmain/clickgui/heart_icon.svg",
                constraints = size(70.percent, 70.percent),
            ).transform(transform)

            onClick {
                if (!favoriteColors.contains(value)) {
                    if (favoriteColors.size == 5) favoriteColors.removeLast()
                    favoriteColors.add(0, Color.HSB(value))
                }
                clicked = true
                transform.animate(0.1.seconds, Animations.EaseInQuint)
                true
            }
            onRelease {
                if (clicked) {
                    transform.animate(0.15.seconds, Animations.EaseInQuint)
                    clicked = false
                }
            }
        }

        repeat(5) { index ->
            divider(3.percent)

            block(
                size(w = 12.5.percent, h = AspectRatio(1f)),
                color = Color.RGB(22, 22, 22),
                radius = 6.radius()
            ) {
                outline(
                    color { (favoriteColors.getOrNull(index) ?: `gray 38`).rgba }
                )
                block(
                    indent(2),
                    color { (favoriteColors.getOrNull(index) ?: Color.TRANSPARENT).rgba },
                    radius = 5.radius()
                )
                onClick {
                    val favoriteColor = favoriteColors.getOrNull(index)
                    if (favoriteColor != null) {
                        value.hue = favoriteColor.hue
                        value.saturation = favoriteColor.saturation
                        value.brightness = favoriteColor.brightness
                        if (allowAlpha) value.alpha = favoriteColor.alpha
                    }
                    true
                }
            }
        }
    }


    /**
     * Utility for making sliders
     */
    private inline fun ElementDSL.pointerHandler(
        pointerX: Animatable.Raw?,
        pointerY: Animatable.Raw?,
        crossinline apply: (px: Float, py: Float) -> Unit,
        crossinline block: () -> Pair<Float, Float>
    ) {
        var first = false
        var dragging = false

        onClick {
            val (px, py) = element.getMousePosPercent()
            apply(px, py)
            first = true
            dragging = true
            true
        }
        onMouseMove {
            if (dragging) {
                val (px, py) = element.getMousePosPercent()
                apply(px, py)
            }
            dragging
        }
        onRelease {
            dragging = false
        }

        onValueChanged {
            val (x, y) = block()

            val toX = (x * element.width).coerceIn(8f, element.width - 8f)
            val toY = (y * element.height).coerceIn(8f, element.height - 8f)
            if (first || !dragging) {
                pointerX?.animate(toX, 0.1.seconds, Animations.EaseOutQuad)
                pointerY?.animate(toY, 0.1.seconds, Animations.EaseOutQuad)
            } else {
                pointerX?.to(toX)
                pointerY?.to(toY)
            }
            redraw()
        }
    }

    private companion object {
        @JvmStatic
        val transparentFix: Color.RGB = Color.RGB(0, 0, 0, 0.2f)
    }
}
