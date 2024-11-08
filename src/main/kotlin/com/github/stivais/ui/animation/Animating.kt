package com.github.stivais.ui.animation

sealed interface Animating {

    fun animate(duration: Float, type: Animations): Animation?

    interface Swapping : Animating {

        fun swap()

        class Impl(var from: Float, var to: Float) : Swapping {

            private var current: Float = 0f

            private var before: Float? = null

            var animation: Animation? = null
                private set

            fun get(): Float {
                animation?.let { anim ->
                    val progress = anim.get()
                    val from = before ?: from
                    current = from + (to - from) * progress

                    if (anim.finished) {
                        animation = null
                        before = null
                        swap()
                    }
                    return current
                }
                return from
            }

            override fun swap() {
                val temp = to
                to = from
                from = temp
            }

            override fun animate(duration: Float, type: Animations): Animation? {
                if (duration == 0f) {
                    swap()
                } else {
                    if (animation != null) {
                        before = current
                        swap()
                        animation = Animation(duration * animation!!.get(), type)
                    } else {
                        animation = Animation(duration, type)
                    }
                }
                return animation
            }
        }

        companion object {
            /**
             * Constant version for [Animating.Swapping.Impl], intended for delegating and overriding get()
             */
            @JvmStatic
            val Impl = Impl(0f, 0f)
        }
    }
}