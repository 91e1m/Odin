package me.odinmain.features.settings

import com.github.stivais.ui.animation.Animations
import com.github.stivais.ui.constraints.Size
import com.github.stivais.ui.constraints.measurements.Animatable
import com.github.stivais.ui.constraints.measurements.Pixel
import com.github.stivais.ui.constraints.px
import com.github.stivais.ui.constraints.size
import com.github.stivais.ui.constraints.sizes.Bounding
import com.github.stivais.ui.constraints.sizes.Copying
import com.github.stivais.ui.elements.Element
import com.github.stivais.ui.elements.scope.ElementDSL
import com.github.stivais.ui.elements.scope.LayoutScope
import com.github.stivais.ui.events.Event
import com.github.stivais.ui.transforms.Transforms
import com.github.stivais.ui.utils.seconds
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import me.odinmain.features.Module
import me.odinmain.features.settings.Setting.Renders.Companion.elementWidth
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

        fun ElementDSL.create()

        companion object {
            /**
             * Creates a group (or column) with the current width needed according the UIs it's used in.
             */
            fun ElementDSL.setting(
                height: Size = 40.px,
                scope: ElementDSL.() -> Unit
            ) {
                group(size(Copying, height), scope)
            }

            fun ElementDSL.settingColumn(
                height: Size = Bounding,
                scope: LayoutScope.() -> Unit
            ) {
                column(size(Copying, height), null, scope)
            }

            /**
             * Current width for settings elements inside the UIs it's used in
             */
            var elementWidth: Pixel = 240.px

            inline fun ElementDSL.onValueChanged(crossinline block: (ValueUpdated) -> Unit) {
                element.registerEvent(ValueUpdated) {
                    block(this)
                    redraw()
                    false
                }
            }
        }


        data object ValueUpdated : Event
    }

    inner class Drawable : Element(size(elementWidth, Animatable(from = Bounding, to = 0.px))) {

        private var visible = visibilityDependency?.invoke() ?: true

//        private var lastValue: T = value

        private var lastValue: Int = value.hashCode()

        private var alphaAnimation = Transforms.Alpha.Animated(to = 0f, from = 1f)

        init {
            addTransform(alphaAnimation)
            if (!visible) {
                (constraints.height as Animatable).swap()
                alphaAnimation.swap()
            }
        }

        override fun draw() {
            if ((visibilityDependency?.invoke() != false) != visible) {
                visible = !visible
                (constraints.height as Animatable).animate(0.25.seconds, Animations.EaseInOutQuint)
                alphaAnimation.animate(0.25.seconds, Animations.EaseInOutQuint)
                redraw = true
            }
            val hashCode = value.hashCode()
            if (lastValue != hashCode) {
                lastValue = hashCode
                ui.eventManager.dispatchToAll(Renders.ValueUpdated, this)
            }
        }
    }
}