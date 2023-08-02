package me.odinclient.ui.clickgui

import cc.polyfrost.oneconfig.renderer.font.Fonts
import me.odinclient.features.Category
import me.odinclient.features.ModuleManager.modules
import me.odinclient.features.impl.general.ClickGUIModule
import me.odinclient.ui.clickgui.elements.ModuleButton
import me.odinclient.ui.clickgui.util.ColorUtil.moduleButtonColor
import me.odinclient.ui.clickgui.util.ColorUtil.textColor
import me.odinclient.utils.render.gui.GuiUtils.capitalizeFirst
import me.odinclient.utils.render.gui.MouseUtils.isAreaHovered
import me.odinclient.utils.render.gui.MouseUtils.mouseX
import me.odinclient.utils.render.gui.MouseUtils.mouseY
import me.odinclient.utils.render.gui.nvg.*
import kotlin.math.floor

class Panel(
    var category: Category,
) {
    val displayName = category.name.capitalizeFirst()

    private var dragging = false
    val moduleButtons: ArrayList<ModuleButton> = ArrayList()

    var x = ClickGUIModule.panelX[category]!!.value
    var y = ClickGUIModule.panelY[category]!!.value

    var extended: Boolean = ClickGUIModule.panelExtended[category]!!.enabled

    var length = 0f

    private var x2 = 0f
    private var y2 = 0f

    init {
        drawNVG {
            for (module in modules.sortedByDescending { getTextWidth(it.name, 18f, Fonts.MEDIUM) }) {
                if (module.category != this@Panel.category) continue
                moduleButtons.add(ModuleButton(module, this@Panel))
            }
        }
    }

    fun draw(nvg: NVG) {
        if (dragging) {
            x = floor(x2 + mouseX)
            y = floor(y2 + mouseY)
        }

        nvg {
            rect(x, y, width, height, moduleButtonColor, 5f, 5f, 0f, 0f)
            text(displayName, x + width / 2f, y + height / 2f, textColor, 22f, Fonts.SEMIBOLD, TextAlign.Middle)

            var startY = height
            if (extended && moduleButtons.isNotEmpty()) {
                for (i  in 0 until moduleButtons.size) {
                    moduleButtons[i].y = startY
                    startY += moduleButtons[i].draw(nvg)
                }
                length = startY + 5f
            }

            rect(x, y + startY, width, 10f, moduleButtons.last().color, 0f, 0f, 5f, 5f)
            dropShadow(x, y, width, startY + 10f, 12.5f, 6f, 5f)
        }
    }

    fun mouseClicked(mouseButton: Int): Boolean {
        if (isHovered) {
            if (mouseButton == 0) {
                x2 = x - mouseX
                y2 = y - mouseY
                dragging = true
                return true
            } else if (mouseButton == 1) {
                extended = !extended
                return true
            }
        } else if (isMouseOverExtended) {
            for (i in moduleButtons.size - 1 downTo 0) {
                if (moduleButtons[i].mouseClicked(mouseButton)) {
                    return true
                }
            }
        }
        return false
    }

    fun mouseReleased(state: Int) {
        if (state == 0) dragging = false

        ClickGUIModule.panelX[category]!!.value = x
        ClickGUIModule.panelY[category]!!.value = y
        ClickGUIModule.panelExtended[category]!!.enabled = extended

        if (extended) {
            for (i in moduleButtons.size - 1 downTo 0) {
                moduleButtons[i].mouseReleased(state)
            }
        }
    }

    fun keyTyped(typedChar: Char, keyCode: Int): Boolean {
        if (extended) {
            for (i in moduleButtons.size - 1 downTo 0) {
                if (moduleButtons[i].keyTyped(typedChar, keyCode)) return true
            }
        }
        return false
    }

    private val isHovered
        get() = isAreaHovered(x, y, width, height)

    private val isMouseOverExtended
        get() = extended && isAreaHovered(x, y, width, length)

    companion object {
        const val width = 240f
        const val height = 40f
    }
}