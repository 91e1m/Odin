package com.github.stivais.ui.elements.impl

import com.github.stivais.ui.animation.Animations
import com.github.stivais.ui.constraints.Constraints
import com.github.stivais.ui.constraints.Type
import com.github.stivais.ui.constraints.measurements.Animatable
import com.github.stivais.ui.constraints.size
import com.github.stivais.ui.constraints.sizes.Bounding
import com.github.stivais.ui.elements.Element
import com.github.stivais.ui.elements.scope.ElementScope
import com.github.stivais.ui.utils.loop
import com.github.stivais.ui.utils.replaceUndefined
import com.github.stivais.ui.utils.seconds

// at the moment only scrolling with bounding is supported
class Scrollable(
    constraints: Constraints = size(Bounding, Bounding),
) : Element(constraints.replaceUndefined(w = Bounding, h = Bounding)) {

    init {
        scissors = true
    }

    private var offset = 0f
        set(value) {
            if (field == value) return
            redraw = true
            field = value
        }

    private var animation: Animatable.Raw = Animatable.Raw(0f)

    override fun draw() {
        offset = animation.get(this, Type.X)
//        renderer.hollowRect(x, y, width, height, 1f, Color.WHITE.rgba)
    }

    override fun positionChildren() {
        if (!enabled) return
        elements?.loop {
            it.position(x, y + offset)
            it.positionChildren()
        }
        val widthRelies = constraints.width.reliesOnChild()
        val heightRelies = constraints.height.reliesOnChild()
        if (widthRelies) width = constraints.width.get(this, Type.W)
        if (heightRelies) height = constraints.height.get(this, Type.H)
        if (widthRelies || heightRelies) parent?.redrawInternal = true
    }

    fun scroll(amount: Float) {
        animation.animate(
            to = (offset + amount).coerceIn(-(height - offset), 0f),
            0.1.seconds,
            Animations.EaseOutQuad
        )
    }
}

class ScrollableScope(element: Scrollable) : ElementScope<Scrollable>(element) {
    fun scroll(amount: Float) {
        element.scroll(amount)
    }
}