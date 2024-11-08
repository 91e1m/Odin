package me.odinmain.features.settings.impl

import com.github.stivais.ui.constraints.Size
import com.github.stivais.ui.constraints.percent
import com.github.stivais.ui.constraints.px
import com.github.stivais.ui.constraints.size
import com.github.stivais.ui.elements.scope.ElementDSL
import com.github.stivais.ui.elements.scope.ElementScope
import com.github.stivais.ui.elements.scope.hoverEffect
import com.github.stivais.ui.utils.radius
import com.github.stivais.ui.utils.seconds
import me.odinmain.features.impl.render.ClickGUI
import me.odinmain.features.impl.render.ClickGUI.`gray 38`
import me.odinmain.features.settings.Setting
import me.odinmain.features.settings.Setting.Renders.Companion.setting

// doesn't save
class UISetting(
    height: Size,
    description: String = "",
    val scope: ElementScope<*>.() -> Unit
) : Setting<Any?>("", false, description), Setting.Renders {
    override val default: Any? = null
    override var value: Any? = null

    private val _height = height

    override fun ElementDSL.create() = setting(_height, scope)
}

@Suppress("FunctionName")
fun ActionSetting(
    name: String,
    description: String = "",
    action: ElementDSL.() -> Unit
): UISetting = UISetting(40.px, description) {
    block(
        constraints = size(95.percent, 75.percent),
        color = `gray 38`,
        radius = 5.radius()
    ) {
        hoverEffect(0.25.seconds)
        outline(ClickGUI.color)
        text(name)

        onClick {
            action.invoke(this)
            true
        }
    }
}