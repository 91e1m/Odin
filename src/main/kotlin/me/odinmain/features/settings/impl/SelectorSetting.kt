package me.odinmain.features.settings.impl

import com.github.stivais.ui.animation.Animations
import com.github.stivais.ui.color.Color
import com.github.stivais.ui.constraints.*
import com.github.stivais.ui.constraints.measurements.Animatable
import com.github.stivais.ui.constraints.sizes.Bounding
import com.github.stivais.ui.constraints.sizes.Copying
import com.github.stivais.ui.elements.scope.ElementDSL
import com.github.stivais.ui.transforms.Transforms
import com.github.stivais.ui.utils.animate
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
 * Setting that lets you pick between an array of strings.
 */
class SelectorSetting(
    name: String,
    default: String,
    var options: ArrayList<String>,
    hidden: Boolean = false,
    description: String = "",
) : Setting<Int>(name, hidden, description), Saving, Setting.Renders {

    constructor(
        name: String,
        options: ArrayList<String>,
        default: String = options[0],
        hidden: Boolean = false,
        description: String = "",
    ) : this(name, default, options, hidden, description)

    override val default: Int = optionIndex(default)

    override var value: Int
        get() = index
        set(value) {
            index = value
        }

    var index: Int = optionIndex(default)
        set(value) {
            field = if (value > options.size - 1) 0 else if (value < 0) options.size - 1 else value
        }

    var selected: String
        get() = options[index]
        set(value) {
            index = optionIndex(value)
        }

    override fun write(): JsonElement {
        return JsonPrimitive(selected)
    }

    override fun read(element: JsonElement?) {
        element?.asString?.let {
            selected = it
        }
    }

    private fun optionIndex(string: String): Int {
        return options.map { it.lowercase() }.indexOf(string.lowercase()).coerceIn(0, options.size - 1)
    }

    override fun ElementDSL.create() = setting(height = Bounding) {
        column(size(w = Copying)) {
            val alpha = Transforms.Alpha.Animated(from = 0f, to = 1f)
            val height = Animatable(from = 0.px, to = Bounding)
            val thickness = Animatable(from = 1.px, to = 1.75.px)

            section(40.px) {
                text(
                    name,
                    pos = at(x = 6.px),
                    size = 40.percent
                )
                block(
                    constraints = constrain(x = -(6.px), w = Bounding + 6.px, h = 75.percent),
                    color = `gray 38`,
                    radius = 5.radius()
                ) {
                    outline(ClickGUI.color, thickness = thickness)

                    val text = text(
                        options[value],
                        pos = at(x = 6.px)
                    )
                    onValueChanged {
                        text.string = options[value]
                    }

                    onClick {
                        alpha.animate(0.25.seconds, Animations.EaseInOutQuint)
                        height.animate(0.25.seconds, Animations.EaseInOutQuint)
                        thickness.animate(0.25.seconds, Animations.EaseInOutQuint)
                        this@column.redraw()
                        true
                    }
                }
            }
            column(size(w = Copying, h = height)) {
                transform(alpha)
                divider(10.px)

                column(size(w = 95.percent)) {
                    block(
                        constraints = copies(),
                        color = `gray 38`,
                        radius = 5.radius()
                    ) {
                        outline(
                            ClickGUI.color,
                            thickness = 1.5.px
                        )
                    }

                    for ((index, option) in options.withIndex()) {
                        block(
                            constraints = size(Copying, h = 32.px),
                            color = Color.Animated(
                                from = Color.TRANSPARENT,
                                to = Color.RGB(150, 150, 150, 0.2f)
                            ),
                            radius = 5.radius()
                        ) {
                            text(option)
                            onClick {
                                value = index
                                alpha.animate(0.25.seconds, Animations.EaseInOutQuint)
                                height.animate(0.25.seconds, Animations.EaseInOutQuint)
                                thickness.animate(0.25.seconds, Animations.EaseInOutQuint)
                                true
                            }
                            onMouseEnterExit {
                                color?.animate(duration = 0.05.seconds)
                            }
                        }
                    }
                }
            }
        }
    }
}