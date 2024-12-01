package me.odinmain.features.settings

import com.github.stivais.aurora.animations.Animation
import com.github.stivais.aurora.constraints.Constraint
import com.github.stivais.aurora.constraints.impl.measurements.Animatable
import com.github.stivais.aurora.constraints.impl.positions.Center
import com.github.stivais.aurora.constraints.impl.size.Bounding
import com.github.stivais.aurora.constraints.impl.size.Copying
import com.github.stivais.aurora.dsl.*
import com.github.stivais.aurora.elements.Element
import com.github.stivais.aurora.elements.ElementScope
import com.github.stivais.aurora.events.AuroraEvent
import com.github.stivais.aurora.transforms.impl.Alpha
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import me.odinmain.features.Module
import kotlin.properties.PropertyDelegateProvider
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty

/**
 * Superclass for settings
 *
 * If you want to implement saving/loading for your setting, you need to implement the [Saving] interface
 *
 * @param name Name of the setting
 * @param hidden If setting shouldn't ever appear in the UI
 * @param description Description for the setting
 */
// todo: remove hidden param
abstract class Setting<T> (
    val name: String,
    var hidden: Boolean = false,
    var description: String = "",
) : ReadWriteProperty<Module, T>, PropertyDelegateProvider<Module, ReadWriteProperty<Module, T>> {

    /**
     * Default value of the setting
     */
    abstract val default: T

    /**
     * Value of the setting
     */
    abstract var value: T

    /**
     * Dependency for if it should be shown in the UI
     *
     * @see DrawableOld
     */
    var visibilityDependency: (() -> Boolean)? = null

    /**
     * Resets the setting to the default value
     */
    open fun reset() {
        value = default
    }

    /**
     * Sets this setting to not show up inside the [UI][me.odinmain.features.impl.render.ClickGUI]
     */
    fun hide(): Setting<T> {
        hidden = true
        return this
    }

    override operator fun provideDelegate(thisRef: Module, property: KProperty<*>): ReadWriteProperty<Module, T> {
        return thisRef.register(this)
    }

    override operator fun getValue(thisRef: Module, property: KProperty<*>): T {
        return value
    }

    override operator fun setValue(thisRef: Module, property: KProperty<*>, value: T) {
        this.value = value
    }

    companion object {

        /**
         * [Gson] for saving and loading settings
         */
        val gson: Gson = GsonBuilder().setPrettyPrinting().create()

        /**
         * Adds a dependency for a setting, for it to only be rendered if it matches true
         *
         * @see DrawableOld
         */
        fun <K : Setting<T>, T> K.withDependency(dependency: () -> Boolean): K {
            visibilityDependency = dependency
            return this
        }
    }

    interface Renders {

        fun ElementScope<*>.create()

        companion object {
            /**
             * Creates a group with the current width needed according the UIs it's used in.
             */
            fun ElementScope<*>.setting(
                height: Constraint.Size = 40.px,
                scope: ElementScope<*>.() -> Unit
            ) {
                group(
                    size(Copying, height),
                    scope
                )
            }

            inline fun ElementScope<*>.onValueChanged(crossinline block: (ValueUpdated) -> Unit) {
                element.registerEvent(ValueUpdated) {
                    block(it)
                    redraw()
                    false
                }
            }
        }


        data object ValueUpdated : AuroraEvent
    }

    inner class Drawable : Element(constrain(x = Center, w = 95.percent, h = Animatable(from = Bounding, to = 0.px))) {

        private var visible = visibilityDependency?.invoke() ?: true

        private var lastValue: Int = value.hashCode()

        private var alphaAnimation = Alpha.Animated(to = 0f, from = 1f)

        init {
            addTransform(alphaAnimation)
            if (!visible) {
                (constraints.height as Animatable).swap()
                alphaAnimation.swap()
            }
            // calc size instantly
            width = constraints.width.calculateSize(this, true)
        }

        override fun draw() {
            if ((visibilityDependency?.invoke() != false) != visible) {
                visible = !visible
                (constraints.height as Animatable).animate(0.25.seconds, Animation.Style.EaseInOutQuint)
                alphaAnimation.animate(0.25.seconds, Animation.Style.EaseInOutQuint)
                parent!!.redraw = true
            }
            val hashCode = value.hashCode()
            if (lastValue != hashCode) {
                lastValue = hashCode
                ui.eventManager.postToAll(Renders.ValueUpdated, this)
            }
        }
    }
}