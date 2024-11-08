package com.github.stivais.ui.elements.impl

import com.github.stivais.ui.animation.Animations
import com.github.stivais.ui.constraints.Constraints
import com.github.stivais.ui.constraints.size
import com.github.stivais.ui.constraints.sizes.Bounding
import com.github.stivais.ui.elements.scope.ElementDSL
import com.github.stivais.ui.elements.scope.ElementScope
import com.github.stivais.ui.transforms.Transforms.Alpha
import com.github.stivais.ui.transforms.Transforms.Scale
import com.github.stivais.ui.utils.seconds

class Popup(
    element: Group,
    private val alphaAnimation: Alpha.Animated,
    private val scaleAnimation: Scale.Animated,
    private val smooth: Boolean
) : ElementScope<Group>(element) {
    fun closePopup(smooth: Boolean = this.smooth) {
        var finished = false

        if (smooth) {
            alphaAnimation.animate(0.25.seconds, Animations.EaseInQuint)
            scaleAnimation.animate(0.25.seconds, Animations.EaseInQuint)?.onFinish {
                finished = true
            }
        } else {
            finished = true
        }
        operation {
            if (finished) ui.main.removeElement(element)
            finished
        }
    }
}

fun ElementDSL.popup(
    constraints: Constraints? = size(Bounding, Bounding),
    smooth: Boolean = false,
    block: Popup.() -> Unit,
): Popup {

    val alphaAnimation = Alpha.Animated(from = 0f, to = 1f)
    val scaleAnimation = Scale.Animated(from = 0f, to = 1f, centered = true)

    val group = Group(constraints).apply {
        addTransform(alphaAnimation)
        addTransform(scaleAnimation)
    }
    ui.main.addElement(group)
    redraw()

    if (smooth) {
        alphaAnimation.animate(0.25.seconds, Animations.EaseOutQuint)
        scaleAnimation.animate(0.25.seconds, Animations.EaseOutQuint)
    } else {
        alphaAnimation.swap()
        scaleAnimation.swap()
    }
    return Popup(group, alphaAnimation, scaleAnimation, smooth).also(block)
}