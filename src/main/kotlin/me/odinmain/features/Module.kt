package me.odinmain.features

import com.github.stivais.aurora.elements.ElementScope
import me.odinmain.OdinMain
import me.odinmain.features.huds.HUD
import me.odinmain.features.impl.render.ClickGUI
import me.odinmain.features.settings.AlwaysActive
import me.odinmain.features.settings.Setting
import me.odinmain.features.settings.impl.HUDSetting
import me.odinmain.features.settings.impl.Keybinding
import me.odinmain.utils.clock.Executable
import me.odinmain.utils.clock.Executor
import me.odinmain.utils.clock.Executor.Companion.register
import me.odinmain.utils.skyblock.modMessage
import net.minecraft.network.Packet
import net.minecraftforge.common.MinecraftForge
import org.lwjgl.input.Keyboard
import kotlin.reflect.full.hasAnnotation

/**
 * Class that represents a module. And handles all the settings.
 * @author Aton
 */
abstract class Module(
    val name: String,
    key: Int? = Keyboard.KEY_NONE,
    @Transient var description: String = "",
    @Transient val tag: TagType = TagType.NONE,
    toggled: Boolean = false,
) {

    /**
     * Category for this module.
     *
     * It is defined by the package of the module. (For example: me.odin.features.impl.render == [Category.RENDER]).
     * If it is in an invalid package, it will use [Category.RENDER] as a default
     */
    @Transient
    val category: Category = getCategory(this::class.java) ?: Category.RENDER

    /**
     * Reference for if the module is enabled
     *
     * When it is enabled, it is registered to the Forge Eventbus,
     * otherwise it's unregistered unless it has the annotation [@AlwaysActive][AlwaysActive]
     */
    var enabled: Boolean = toggled
        private set

    /**
     * List of settings for the module
     */
    val settings: ArrayList<Setting<*>> = ArrayList()

    /**
     * Main keybinding of the module
     */
    val keybinding: Keybinding? = key?.let { Keybinding(it).apply { onPress = ::onKeybind } }

    protected inline val mc get() = OdinMain.mc

    /**
     * Indicates if the module has the annotation [AlwaysActive],
     * which keeps the module registered to the eventbus, even if disabled
     */
    @Transient
    val alwaysActive = this::class.hasAnnotation<AlwaysActive>()

    init {
        if (alwaysActive) {
            @Suppress("LeakingThis")
            MinecraftForge.EVENT_BUS.register(this)
        }
    }

    /**
     * Gets toggled when module is enabled
     */
    open fun onEnable() {
        if (!alwaysActive) MinecraftForge.EVENT_BUS.register(this)
    }

    /**
     * Gets toggled when module is disabled
     */
    open fun onDisable() {
        if (!alwaysActive) MinecraftForge.EVENT_BUS.unregister(this)
    }

    open fun onKeybind() {
        toggle()
        if (ClickGUI.enableNotification) {
            modMessage("$name ${if (enabled) "§aenabled" else "§cdisabled"}.")
        }
    }

    fun toggle() {
        enabled = !enabled
        if (enabled) onEnable()
        else onDisable()
    }

    fun <K : Setting<*>> register(setting: K): K {
        settings.add(setting)
        return setting
    }

    fun register(vararg setting: Setting<*>) {
        for (i in setting) {
            register(i)
        }
    }

    fun getSettingByName(name: String?): Setting<*>? {
        for (setting in settings) {
            if (setting.name.equals(name, ignoreCase = true)) {
                return setting
            }
        }
        return null
    }

    /**
     * Helper function to make cleaner code, and more performance, since we don't need multiple registers for packet received events.
     *
     * @param type The packet type to listen for.
     * @param shouldRun Get whether the function should run (Will in most cases be used with the "enabled" value)
     * @param func The function to run when the packet is received.
     */
    fun <T : Packet<*>> onPacket(type: Class<T>, shouldRun: () -> Boolean = { alwaysActive || enabled }, func: (T) -> Unit) {
        @Suppress("UNCHECKED_CAST")
        ModuleManager.packetFunctions.add(
            ModuleManager.PacketFunction(type, func, shouldRun) as ModuleManager.PacketFunction<Packet<*>>
        )
    }

    /**
     * Runs the given function when a Chat Packet is sent with a message that matches the given regex filter.
     *
     * @param filter The regex the message should match
     * @param shouldRun Boolean getter to decide if the function should run at any given time, could check if the option is enabled for instance.
     * @param func The function to run if the message matches the given regex and shouldRun returns true.
     *
     * @author Bonsai
     */
    fun onMessage(filter: Regex, shouldRun: () -> Boolean = { alwaysActive || enabled }, func: (String) -> Unit) {
        ModuleManager.messageFunctions.add(ModuleManager.MessageFunction(filter, shouldRun, func))
    }

    fun onWorldLoad(func: () -> Unit) {
        ModuleManager.worldLoadFunctions.add(func)
    }

    fun execute(delay: Long, repeats: Int, profileName: String = "${this.name} Executor", shouldRun: () -> Boolean = { this.enabled || this.alwaysActive }, func: Executable) {
        Executor.LimitedExecutor(delay, repeats, profileName, shouldRun, func).register()
    }

    fun execute(delay: () -> Long, profileName: String = "${this.name} Executor", shouldRun: () -> Boolean = { this.enabled || this.alwaysActive }, func: Executable) {
        Executor(delay, profileName, shouldRun, func).register()
    }

    fun execute(delay: Long, profileName: String = "${this.name} Executor", shouldRun: () -> Boolean = { this.enabled || this.alwaysActive }, func: Executable) {
        Executor(delay, profileName, shouldRun, func).register()
    }

    fun HUD(
        name: String,
        block: ElementScope<HUD.Representation>.() -> Unit
    ): HUD {
        return HUD(
            name,
            this,
            block
        )
    }

    fun HUD.setting(description: String): HUDSetting {
        return HUDSetting(
            name,
            this,
            description
        )
    }

    // this is unused
    @Deprecated("remove")
    enum class TagType {
        NONE, NEW, RISKY, FPSTAX
    }

    private companion object {
        private fun getCategory(clazz: Class<out Module>): Category? {
            val `package` = clazz.`package`.name
            return when {
                `package`.contains("dungeon") -> Category.DUNGEON
                `package`.contains("floor7") -> Category.FLOOR7
                `package`.contains("render") -> Category.RENDER
                `package`.contains("skyblock") -> Category.SKYBLOCK
                `package`.contains("nether") -> Category.NETHER
                else -> null
            }
        }
    }
}