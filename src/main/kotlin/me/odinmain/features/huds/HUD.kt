package me.odinmain.features.huds

import com.github.stivais.ui.constraints.Constraints
import com.github.stivais.ui.constraints.percent
import com.github.stivais.ui.constraints.sizes.Bounding
import com.github.stivais.ui.elements.Element
import com.github.stivais.ui.elements.scope.ElementScope
import com.github.stivais.ui.transforms.Transforms
import me.odinmain.features.Module
import me.odinmain.features.settings.Setting
import me.odinmain.features.settings.impl.NumberSetting
import kotlin.reflect.KProperty0
import kotlin.reflect.jvm.isAccessible

class HUD(
    val name: String,
    val module: Module,
    val builder: HUDScope.() -> Unit
) {
    val x = NumberSetting("x", 2.5f, 0f, 100f).hide()
    val y = NumberSetting("y", 2.5f, 0f, 100f).hide()
    val scale = NumberSetting("Scale", 1f, 0.2f, 5f, increment = 0.1f)

    /**
     * Settings tied to this HUD, these get saved inside the [setting][me.odinmain.features.settings.impl.HUDSetting]
     */
    val settings: ArrayList<Setting<*>> = arrayListOf(x, y, scale)

    init {
        HUDManager.HUDs.add(this)
    }

    fun registerSettings(vararg settings: Setting<*>) {
        for (setting in settings) {
            if (module.settings.contains(setting)) {
                module.settings.remove(setting)
            }
            this.settings.add(setting)
        }
    }

    fun registerSettings(vararg settings: KProperty0<*>): HUD {
        for (property in settings) {
            property.isAccessible = true
            val delegate = property.getDelegate() as? Setting<*> ?: throw IllegalArgumentException(
                "Invalid Arguments. Must use a delegated setting as the argument for the HUD setting."
            )
            if (module.settings.contains(delegate)) {
                module.settings.remove(delegate)
            }
            this.settings.add(delegate)
            delegate.hide()
        }
        return this
    }

    inner class Representation(
        val preview: Boolean
    ) : Element(Constraints(x.value.percent, y.value.percent, Bounding, Bounding)) {

        override var enabled: Boolean = true
            get() = field && this@HUD.module.enabled

        var scaleTransformation by Transforms.Scale(this@HUD.scale.value, centered = false).also {
            addTransform(it)
        }

        override fun draw() {
            // no-op
        }
        override fun onElementAdded(element: Element) {
            // no-op
            // elements here shouldn't be centered by default
        }

        fun refresh(scope: HUDScope) {
            removeAll()
            builder.invoke(scope)
            scaleTransformation = this@HUD.scale.value
            redrawInternal = true
        }
    }

    // saving util
    fun getSettingByName(name: String?): Setting<*>? {
        for (setting in settings) {
            if (setting.name.equals(name, ignoreCase = true)) {
                return setting
            }
        }
        return null
    }
}

class HUDScope(element: HUD.Representation) : ElementScope<HUD.Representation>(element) {

    inline val preview get() = element.preview

    inline fun needs(crossinline block: () -> Boolean) {
        if (!element.preview) {
            operation {
                element.enabled = block()
                false
            }
        }
    }

    fun refreshHUD() {
        element.refresh(this)
    }
}