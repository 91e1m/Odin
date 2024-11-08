package me.odinmain.features.impl.render

import com.github.stivais.ui.color.Color
import me.odinmain.events.impl.PacketSentEvent
import me.odinmain.features.Module
import me.odinmain.features.settings.Setting.Companion.withDependency
import me.odinmain.features.settings.impl.BooleanSetting
import me.odinmain.features.settings.impl.ColorSetting
import me.odinmain.features.settings.impl.DropdownSetting
import me.odinmain.features.settings.impl.SelectorSetting
import net.minecraft.network.play.client.C08PacketPlayerBlockPlacement
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import net.minecraftforge.fml.common.gameevent.TickEvent.ClientTickEvent

object CPSDisplay : Module(
    name = "CPS Display",
    description = "Customizable CPS Display"
) {
    // make it depend on right clicks to be shown
    private val countPackets by BooleanSetting("Count Packets", false, description = "Counts packets sent outside of the rightclickmouse method, this will be better at detecting other mods' auto clickers, but might show inaccurate values.")
    private val advanced by DropdownSetting("Show Settings", false)
    private val button by SelectorSetting("Button", "Both", arrayListOf("Left", "Right", "Both")).withDependency { advanced }
    private val mouseText by BooleanSetting("Show Button", true).withDependency { advanced }
    private val textColor by ColorSetting("Text Color", Color.RGB(239, 239, 239, 1f), allowAlpha = true).withDependency { advanced }
    private val outline by BooleanSetting("Outline", true).withDependency { advanced }

    private val leftClicks = mutableListOf<Long>()
    private val rightClicks = mutableListOf<Long>()


    @SubscribeEvent
    fun onTick(event: ClientTickEvent) {
        if (leftClicks.size != 0 && System.currentTimeMillis() - leftClicks.first() > 1000) {
            leftClicks.removeFirst()
        }
        if (rightClicks.size != 0 && System.currentTimeMillis() - rightClicks.first() > 1000) {
            rightClicks.removeFirst()
        }
    }

    @SubscribeEvent
    fun onPacket(event: PacketSentEvent) {
        if (event.packet !is C08PacketPlayerBlockPlacement) return
        if (countPackets) {
            if (rightClicks.size == 0 || System.currentTimeMillis() - rightClicks.last() > 5) {
                onRightClick()
            }
        }
    }

    @JvmStatic
    fun onLeftClick() {
        leftClicks.add(System.currentTimeMillis())
    }

    @JvmStatic
    fun onRightClick() {
        rightClicks.add(System.currentTimeMillis())
    }
}