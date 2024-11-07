package me.odinmain.features

import com.github.stivais.ui.UI
import com.github.stivais.ui.UIScreen.Companion.init
import com.github.stivais.ui.constraints.constrain
import com.github.stivais.ui.constraints.sizes.Bounding
import me.odinmain.OdinMain.mc
import me.odinmain.events.impl.*
import me.odinmain.features.impl.dungeon.*
import me.odinmain.features.impl.dungeon.dungeonwaypoints.DungeonWaypoints
import me.odinmain.features.impl.dungeon.puzzlesolvers.PuzzleSolvers
import me.odinmain.features.impl.floor7.TerminalSimulator
import me.odinmain.features.impl.floor7.TickTimers
import me.odinmain.features.impl.floor7.WitherDragons
import me.odinmain.features.impl.floor7.p3.*
import me.odinmain.features.impl.nether.*
import me.odinmain.features.impl.render.*
import me.odinmain.features.impl.skyblock.*
import me.odinmain.features.settings.impl.KeybindSetting
import me.odinmain.utils.clock.Executor
import me.odinmain.utils.profile
import net.minecraft.network.Packet
import net.minecraftforge.client.event.RenderWorldLastEvent
import net.minecraftforge.event.world.WorldEvent
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import net.minecraftforge.fml.common.gameevent.TickEvent

/**
 * # Module Manager
 *
 * This object stores all [Modules][Module] and provides functionality to [HUDs][Module.HUD]
 */
object ModuleManager {

    val HUDs = arrayListOf<Module.HUD>()

    internal val hudUI = UI().init()

    fun setupHUD(hud: Module.HUD) {
        val drawable = hud.Drawable(constrain(hud.x, hud.y, Bounding, Bounding), preview = false)
        hudUI.main.addElement(drawable)
        hud.builder(Module.HUDScope(drawable))
    }

    private var previousWidth: Int = 0
    private var previousHeight: Int = 0

    @SubscribeEvent
    fun onRender(event: RenderWorldLastEvent) {
        val w = mc.framebuffer.framebufferWidth
        val h = mc.framebuffer.framebufferHeight
        if (w != previousWidth || h != previousHeight) {
            hudUI.resize(w, h)
            previousWidth = w
            previousHeight = h
        }
        hudUI.render()
    }

    // todo: cleanup
    data class PacketFunction<T : Packet<*>>(
        val type: Class<T>,
        val function: (T) -> Unit,
        val shouldRun: () -> Boolean,
    )

    data class MessageFunction(val filter: Regex, val shouldRun: () -> Boolean, val function: (String) -> Unit)
    data class MessageFunctionCancellable(
        val filter: Regex,
        val shouldRun: () -> Boolean,
        val function: (ChatPacketEvent) -> Unit
    )

    // todo: cleanup
    data class TickTask(var ticksLeft: Int, val server: Boolean, val function: () -> Unit)

    // todo: cleanup
    val packetFunctions = mutableListOf<PacketFunction<Packet<*>>>()
    val messageFunctions = mutableListOf<MessageFunction>()
    val cancellableMessageFunctions = mutableListOf<MessageFunctionCancellable>()
    val worldLoadFunctions = mutableListOf<() -> Unit>()
    val tickTasks = mutableListOf<TickTask>()

    // todo: cleanup
    val executors = ArrayList<Pair<Module, Executor>>()

    val modules: ArrayList<Module> = arrayListOf(
        // dungeon
        DungeonRequeue, BlessingDisplay, PosMessages, ExtraStats, KeyHighlight, Mimic, TeammatesHighlight,
        TerracottaTimer, BloodCamp, SecretClicked, DungeonWaypoints, LeapMenu, PuzzleSolvers,
        WarpCooldown, MapInfo, SwapSound,

        // floor 7
        TerminalSolver, TerminalTimes, MelodyMessage, TickTimers, InactiveWaypoints, WitherDragons,
        TerminalSimulator, TerminalSounds, ArrowAlign,

        // render
        BPSDisplay, CustomHighlight, CPSDisplay, DragonHitboxes, GyroWand, NameChanger,
        PersonalDragon, RenderOptimizer, ServerHud, Waypoints, CanClip, Animations, SpaceHelmet,
        BlockOverlay, VisualWords, DVD, Sidebar, HideArmor, ClickGUI,

        //skyblock
        NoCursorReset, AutoSprint, BlazeAttunement, ChatCommands, DeployableTimer, DianaHelper, ArrowHit,
        RagAxe, MobSpawn, Splits, WardrobeKeybinds, InvincibilityTimer, ItemsHighlight, PlayerDisplay,
        FarmKeys, PetKeybinds, CommandKeybinds, SpringBoots, AbilityTimers,

        // kuudra
        BuildHelper, FreshTimer, KuudraDisplay, NoPre, PearlWaypoints, RemovePerks, SupplyHelper, TeamHighlight,
        VanqNotifier, KuudraReminders, KuudraRequeue,
    )

    init {
        for (module in modules) {
            module.keybinding?.let {
                module.register(KeybindSetting("Keybind", it, "Toggles the module"))
            }
        }
    }

    fun addModules(vararg module: Module) {
        for (i in module) {
            modules.add(i)
            i.keybinding?.let { i.register(KeybindSetting("Keybind", it, "Toggles the module")) }
        }
    }

    @SubscribeEvent
    fun onTick(event: TickEvent.ClientTickEvent) {
        if (event.phase != TickEvent.Phase.START) return
        tickTasks.removeAll {
            if (it.ticksLeft <= 0) {
                it.function()
                return@removeAll true
            }
            it.ticksLeft--
            false
        }
    }

    @SubscribeEvent
    fun onReceivePacket(event: PacketReceivedEvent) {
        packetFunctions
            .filter { it.type.isInstance(event.packet) && it.shouldRun.invoke() }
            .forEach { it.function(event.packet) }
    }

    @SubscribeEvent
    fun onSendPacket(event: PacketSentEvent) {
        packetFunctions
            .filter { it.type.isInstance(event.packet) && it.shouldRun.invoke() }
            .forEach { it.function(event.packet) }
    }

    @SubscribeEvent
    fun onChatPacket(event: ChatPacketEvent) {
        messageFunctions
            .filter { event.message matches it.filter && it.shouldRun() }
            .forEach { it.function(event.message) }

        cancellableMessageFunctions
            .filter { event.message matches it.filter && it.shouldRun() }
            .forEach { it.function(event) }
    }

    @SubscribeEvent
    fun onWorldLoad(event: WorldEvent.Load) {
        worldLoadFunctions
            .forEach { it.invoke() }
    }

    @SubscribeEvent
    fun activateModuleKeyBinds(event: PreKeyInputEvent) {
        for (module in modules) {
            for (setting in module.settings) {
                if (setting is KeybindSetting && setting.value.key == event.keycode) {
                    setting.value.onPress?.invoke()
                }
            }
        }
    }

    @SubscribeEvent
    fun activateModuleMouseBinds(event: PreMouseInputEvent) {
        for (module in modules) {
            for (setting in module.settings) {
                if (setting is KeybindSetting && setting.value.key + 100 == event.button) {
                    setting.value.onPress?.invoke()
                }
            }
        }
    }

    @SubscribeEvent
    fun onRenderWorld(event: RenderWorldLastEvent) {
        profile("Executors") {
            executors.removeAll {
                if (!it.first.enabled && !it.first.alwaysActive) return@removeAll false // pls test i cba
                it.second.run()
            }
        }
    }

    fun getModuleByName(name: String?): Module? = modules.firstOrNull { it.name.equals(name, true) }

    fun generateFeatureList(): String {
        /* val moduleList = modules.sortedByDescending { getTextWidth(it.name, 18f) }
         val categories = moduleList.groupBy { it.category }

         val categoryOrder = Category.entries.associateWith { it.ordinal }
         val sortedCategories = categories.entries.sortedBy { categoryOrder[it.key] }

         val featureList = StringBuilder()

         for ((category, modulesInCategory) in sortedCategories) {
             val displayName = category.name.capitalizeFirst()
             featureList.appendLine("Category: ${if (displayName == "Floor7") "Floor 7" else displayName}")
             for (module in modulesInCategory) {
                 featureList.appendLine("- ${module.name}: ${module.description}")
             }
             featureList.appendLine()
         }
         return featureList.toString()*/
        return ""
    }
}