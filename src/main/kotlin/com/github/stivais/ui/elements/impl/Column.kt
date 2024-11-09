package com.github.stivais.ui.elements.impl

import com.github.stivais.ui.color.Color
import com.github.stivais.ui.constraints.*
import com.github.stivais.ui.constraints.measurements.Undefined
import com.github.stivais.ui.constraints.sizes.Bounding
import com.github.stivais.ui.constraints.sizes.Copying
import com.github.stivais.ui.elements.Element
import com.github.stivais.ui.elements.scope.ElementScope
import com.github.stivais.ui.elements.scope.ScopeDSL
import com.github.stivais.ui.events.Mouse
import com.github.stivais.ui.utils.loop
import com.github.stivais.ui.utils.replaceUndefined

class Column(
    constraints: Constraints,
    private val paddingX: Size?,
    private val paddingY: Size?,
) : Element(constraints.replaceUndefined(w = Bounding, h = Bounding)) {

    private val positionedElements = hashSetOf<Element>()

    init {
        this.constraints.apply {
            width += paddingX
            height += paddingY
        }
        Mouse.Scrolled(0f).register { (amount) ->
            start += amount / 12
            true
        }
    }

    var start = 0f

    override fun onElementAdded(element: Element) {
        val constraints = element.constraints
        if (constraints.y is Undefined) {
            positionedElements.add(element)
        }
    }

    override fun positionChildren() {
        if (!enabled) return

        val px = paddingX?.get(this, Type.W) ?: 0f
        val py = paddingY?.get(this, Type.H) ?: 0f

        var increment = start
        elements?.loop {
            if (positionedElements.contains(it)) {
                it.position(x + px, y + py + increment)
                increment += (it.height * it.scale) + py
            } else {
                it.position(x + px, y + py)
            }
            it.positionChildren()
        }

        val widthRelies = constraints.width.reliesOnChild()
        val heightRelies = constraints.height.reliesOnChild()
        if (widthRelies) width = constraints.width.get(this, Type.W)
        if (heightRelies) height = constraints.height.get(this, Type.H)
        if (widthRelies || heightRelies) parent?.redrawInternal = true
    }

    override fun draw() {
        renderer.hollowRect(x, y, width, height, 1f, Color.WHITE.rgba)
    }
}

class ColumnScope(column: Column) : ElementScope<Column>(column) {
    @ScopeDSL
    fun section(
        size: Size,
        block: ElementScope<Group>.() -> Unit
    ) = group(size(Copying, size), block)

    @ScopeDSL
    fun divider(
        size: Size,
    ) = group(size(Copying, size))
}