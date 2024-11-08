package com.github.stivais.ui.elements.impl

import com.github.stivais.ui.color.Color
import com.github.stivais.ui.constraints.*
import com.github.stivais.ui.constraints.measurements.Pixel
import com.github.stivais.ui.constraints.measurements.Undefined
import com.github.stivais.ui.elements.Element
import com.github.stivais.ui.elements.scope.ElementScope
import com.github.stivais.ui.renderer.Font

open class TextElement(
    string: String,
    val font: Font,
    color: Color,
    constraints: Positions = at(Undefined, Undefined),
    size: Size,
) : Element(constraints, color) {

    private val textWidth = 0.px

    init {
        constraints.width = textWidth
        constraints.height = size
    }

    open var text: String = string
        set(value) {
            if (field == value) return
            field = value
            redraw = true
            previousHeight = 0f
        }

    // uses to check if width should be recalculated as it is expensive to do so
    protected var previousHeight = 0f

    override fun preSize() {
        height = constraints.height.get(this, Type.H)
        if (previousHeight != height) {
            previousHeight = height
            val newWidth = getTextWidth()
            (constraints.width as Pixel).pixels = newWidth
        }
    }

    open fun getTextWidth(): Float {
        return renderer.textWidth(text, height, font)
    }

    override fun draw() {
        renderer.text(text, x, y, height, color!!.get(this), font)
    }

    class Supplied(
        val supplier: () -> Any?,
        font: Font,
        color: Color,
        constraints: Positions = at(Undefined, Undefined),
        size: Size
    ) : TextElement(supplier().toString(), font, color, constraints, size) {

        override fun draw() {
            text = supplier().toString()
            super.draw()
        }
    }
}

class TextScope(text: TextElement) : ElementScope<TextElement>(text) {

    var string: String
        get() = element.text
        set(value) {
            element.text = value
        }

    var size: Size
        get() = element.constraints.height
        set(value) {
            element.constraints.height = value
        }
}
