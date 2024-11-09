package com.github.stivais.ui.color

import com.github.stivais.ui.animation.Animating
import com.github.stivais.ui.animation.Animation
import com.github.stivais.ui.animation.Animations
import com.github.stivais.ui.elements.Element
import kotlin.math.roundToInt
import java.awt.Color as JColor

/**
 * # Color
 *
 * The color interface is used to represent a 32-bit integer in ARGB format.
 *
 * It is primarily used in [UIs][com.github.stivais.ui.UI].
 *
 * @see Color.RGB
 * @see Color.HSB
 * @see Color.Animated
 */
interface Color {

    /**
     * Integer representation of this color in ARGB format
     */
    val rgba: Int

    /**
     * Uses internally by [UIs][com.github.stivais.ui.UI]
     */
    fun get(element: Element): Int = rgba

    /**
     * # Color.RGB
     *
     * Low cost implementation of [Color], due to Kotlin's inline classes
     *
     * This is the most common [color][Color], because it mainly represents red, blue and green.
     */
    @JvmInline
    value class RGB(override val rgba: Int) : Color {
        constructor(
            red: Int,
            green: Int,
            blue: Int,
            alpha: Float = 1f
        ) : this(getRGBA(red, green, blue, (alpha * 255).roundToInt()))
    }

    /**
     * # Color.HSB
     *
     * This [Color] implementation represents the color in HSBA format.
     *
     * It only updates the [rgba][Color.rgba] value if any of the hue, saturation or brightness values have been changed.
     */
    open class HSB(hue: Float, saturation: Float, brightness: Float, alpha: Float = 1f) : Color {

        constructor(hsb: FloatArray, alpha: Float = 1f) : this(hsb[0], hsb[1], hsb[2], alpha)

        constructor(other: HSB) : this(other.hue, other.saturation, other.brightness, other.alpha)

        var hue = hue
            set(value) {
                field = value
                needsUpdate = true
            }

        var saturation = saturation
            set(value) {
                field = value
                needsUpdate = true
            }

        var brightness = brightness
            set(value) {
                field = value
                needsUpdate = true
            }

        var alpha = alpha
            set(value) {
                field = value
                needsUpdate = true
            }

        @Transient
        private var needsUpdate: Boolean = true

        override var rgba: Int = 0
            get() {
                if (needsUpdate) {
                    field =
                        (JColor.HSBtoRGB(hue, saturation, brightness) and 0X00FFFFFF) or ((alpha * 255).toInt() shl 24)
                    needsUpdate = false
                }
                return field
            }
            set(value) {
                if (field != value) {
                    field = value
                    val hsb = FloatArray(3)
                    JColor.RGBtoHSB(value.red, value.blue, value.green, hsb)
                    hue = hsb[0]
                    saturation = hsb[1]
                    brightness = hsb[2]
                    alpha = value.alpha / 255f
                }
            }


        override fun equals(other: Any?): Boolean {
            return other is Color && other.rgba == this.rgba
        }

        override fun hashCode(): Int {
            return rgba.hashCode()
        }
    }

    /**
     * # Color.Animated
     *
     * This [Color] implementation allows you to animate between 2 different [colors][Color],
     * allowing for the outcome to be expected.
     *
     * This implements the [Animating.Swapping] interface
     *
     * @see Animating.Swapping
     */
    class Animated(from: Color, to: Color) : Color, Animating.Swapping {

        constructor(from: Color, to: Color, swapIf: Boolean) : this(from, to) {
            if (swapIf) {
                swap()
            }
        }
        /**
         * Current animation for this [Color.Animated]
         *
         * If this is null, that means it isn't animating
         */
        var animation: Animation? = null

        /**
         * The first color to animate from.
         *
         * Note: When animating it swaps between [color1] and [color2]
         */
        var color1: Color = from

        /**
         * The second color to animate from.
         *
         * Note: When animating it swaps between [color1] and [color2]
         */
        var color2: Color = to

        private var current: Int = color1.rgba
        private var from: Int = color1.rgba

        override val rgba: Int
            get() {
                if (animation != null) {
                    val progress = animation!!.get()
                    val to = color2.rgba
                    current = getRGBA(
                        (from.red + (to.red - from.red) * progress).toInt(),
                        (from.green + (to.green - from.green) * progress).toInt(),
                        (from.blue + (to.blue - from.blue) * progress).toInt(),
                        (from.alpha + (to.alpha - from.alpha) * progress).toInt()
                    )
                    if (animation!!.finished) {
                        animation = null
                        swap()
                    }
                    return current
                }
                return color1.rgba
            }

        override fun get(element: Element): Int {
            // add stuff when fbo
            return rgba
        }

        override fun animate(duration: Float, type: Animations): Animation? {
            if (duration == 0f) {
                swap()
                current = color1.rgba // here so it updates if you swap a color and want to animate it later
            } else {
                if (animation != null) {
                    swap()
                    animation = Animation(duration * animation!!.get(), type)
                    from = current
                } else {
                    animation = Animation(duration, type)
                    from = color1.rgba
                }
                return animation!!
            }
            return null
        }

        override fun swap() {
            val temp = color2
            color2 = color1
            color1 = temp
        }
    }

    companion object {
        @JvmField
        val TRANSPARENT = RGB(0, 0, 0, 0f)

        @JvmField
        val WHITE = RGB(255, 255, 255)

        @JvmField
        val BLACK = RGB(0, 0, 0)

        @JvmField
        val RED = RGB(255, 0, 0)

        @JvmField
        val BLUE = RGB(0, 0, 255)

        @JvmField
        val GREEN = RGB(0, 255, 0)

        // Minecraft colors

        @JvmField
        val MINECRAFT_DARK_BLUE = RGB(0, 0, 170)

        @JvmField
        val MINECRAFT_DARK_GREEN = RGB(0, 170, 0)

        @JvmField
        val MINECRAFT_DARK_AQUA = RGB(0, 170, 170)

        @JvmField
        val MINECRAFT_DARK_RED = RGB(170, 0, 0)

        @JvmField
        val MINECRAFT_DARK_PURPLE = RGB(170, 0, 170)

        @JvmField
        val MINECRAFT_GOLD = RGB(255, 170, 0)

        @JvmField
        val MINECRAFT_GRAY = RGB(170, 170, 170)

        @JvmField
        val MINECRAFT_DARK_GRAY = RGB(85, 85, 85)

        @JvmField
        val MINECRAFT_BLUE = RGB(85, 85, 255)

        @JvmField
        val MINECRAFT_GREEN = RGB(85, 255, 85)

        @JvmField
        val MINECRAFT_AQUA = RGB(85, 255, 255)

        @JvmField
        val MINECRAFT_RED = RGB(255, 85, 85)

        @JvmField
        val MINECRAFT_LIGHT_PURPLE = RGB(255, 85, 255)

        @JvmField
        val MINECRAFT_YELLOW = RGB(255, 255, 85)
    }
}
