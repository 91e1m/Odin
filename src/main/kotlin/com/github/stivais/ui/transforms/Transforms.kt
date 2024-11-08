package com.github.stivais.ui.transforms

import com.github.stivais.ui.animation.Animating
import com.github.stivais.ui.elements.Element
import com.github.stivais.ui.elements.scope.ElementDSL
import com.github.stivais.ui.renderer.Renderer
import kotlin.reflect.KProperty

private fun runScale(element: Element, renderer: Renderer, amount: Float, centered: Boolean) {
    var x = element.x
    var y = element.y
    if (centered) {
        x += element.width / 2f
        y += element.height / 2f
    }
    renderer.translate(x, y)
    renderer.scale(amount, amount)
    renderer.translate(-x, -y)
}

private fun runRotation(element: Element, renderer: Renderer, amount: Float) {
    val x = element.x + element.width / 2f
    val y = element.y + element.height / 2f
    renderer.translate(x, y)
    renderer.rotate(Math.toRadians(amount.toDouble()).toFloat())
    renderer.translate(-x, -y)
}

fun interface Transforms {

    fun apply(element: Element, renderer: Renderer)

    class Scale(var amount: Float, private val centered: Boolean) : Transforms {
        override fun apply(element: Element, renderer: Renderer) {
           runScale(element, renderer, amount, centered)
            element.scale = amount
        }

        operator fun getValue(thisRef: Any?, property: KProperty<*>): Float {
            return amount
        }

        operator fun setValue(thisRef: Any?, property: KProperty<*>, value: Float) {
            amount = value
        }

        class Animated private constructor(
            private val impl: Animating.Swapping.Impl,
            private val centered: Boolean
        ) : Transforms, Animating.Swapping by impl {

            constructor(
                from: Float,
                to: Float,
                centered: Boolean
            ) : this(Animating.Swapping.Impl(from, to), centered)

            override fun apply(element: Element, renderer: Renderer) {
                val amount = impl.get()
                runScale(element, renderer, amount, centered)
                element.scale = amount
            }
        }
    }

    class Alpha(var amount: Float) : Transforms {
        override fun apply(element: Element, renderer: Renderer) {
            if (amount != 1f) {
                renderer.globalAlpha(amount)
            }
        }

        class Animated private constructor(
            private val impl: Animating.Swapping.Impl,
        ) : Transforms, Animating.Swapping by impl {

            constructor(
                from: Float,
                to: Float,
            ) : this(Animating.Swapping.Impl(from, to))

            override fun apply(element: Element, renderer: Renderer) {
                val amount = impl.get()
                if (amount != 1f) {
                    renderer.globalAlpha(amount)
                }
            }
        }
    }

    class Rotation(var amount: Float) : Transforms {
        override fun apply(element: Element, renderer: Renderer) {
            runRotation(element, renderer, amount)
        }

        class Animated private constructor(
            private val impl: Animating.Swapping.Impl,
        ) : Transforms, Animating.Swapping by impl {
            constructor(
                from: Float,
                to: Float,
            ) : this(Animating.Swapping.Impl(from, to))

            override fun apply(element: Element, renderer: Renderer) {
                val amount = impl.get()
                runRotation(element, renderer, amount)
            }
        }
    }
}

fun ElementDSL.scale(from: Float, to: Float, centered: Boolean = true): Transforms.Scale.Animated {
    val transform = Transforms.Scale.Animated(from, to, centered)
    transform(transform)
    return transform
}

fun ElementDSL.alpha(from: Float, to: Float): Transforms.Alpha.Animated {
    val transform = Transforms.Alpha.Animated(from, to)
    transform(transform)
    return transform
}

fun ElementDSL.alpha(amount: Float): Transforms.Alpha {
    val transform = Transforms.Alpha(amount)
    transform(transform)
    return transform
}

fun ElementDSL.rotation(from: Float, to: Float): Transforms.Rotation.Animated {
    val transform = Transforms.Rotation.Animated(from, to)
    transform(transform)
    return transform
}