package me.odinmain.features.impl.render

import com.github.stivais.ui.constraints.px
import me.odinmain.events.impl.PacketSentEvent
import me.odinmain.features.Module
import me.odinmain.features.settings.impl.BooleanSetting
import me.odinmain.utils.round
import me.odinmain.utils.ui.TextHUD
import me.odinmain.utils.ui.and
import net.minecraft.network.play.client.C07PacketPlayerDigging.Action.START_DESTROY_BLOCK
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import net.minecraftforge.fml.common.gameevent.TickEvent.ClientTickEvent
import kotlin.math.roundToInt
import net.minecraft.network.play.client.C07PacketPlayerDigging as PacketPlayerDigging

// todo: Fix number going to infinity somehow
object BPSDisplay : Module(
    name = "BPS Display",
    description = "Displays how many blocks you're breaking per second."
) {
    private val roundNumber by BooleanSetting("Round number", true, description = "If the number should be rounded.")

    private val hud by TextHUD("HUD") { color, font ->
        text(
            text = "BPS ",
            font = font,
            color = color,
            size = 30.px
        ) and text({ if (roundNumber) bps.roundToInt() else bps.round(1) }, font = font)
    }.registerSettings(
        ::roundNumber
    ).setting("Displays the BPS on screen.")

    private var bps = 0.0
        get() = field.coerceIn(0.0, 20.0)

    private var startTime = 0L
    private var isBreaking = false
    private var blocksBroken = 0
    private var lastBrokenBlock = 0L

    @SubscribeEvent
    fun onPacket(event: PacketSentEvent) {
        val packet = event.packet as? PacketPlayerDigging ?: return
        if (packet.status != START_DESTROY_BLOCK) return
        if (startTime == 0L) startTime = System.currentTimeMillis()
        isBreaking = true
        blocksBroken++
        lastBrokenBlock = System.currentTimeMillis()
    }

    @SubscribeEvent
    fun onTick(event: ClientTickEvent) {
        if (!isBreaking) return
        val secondsElapsed = (System.currentTimeMillis() - startTime) / 1000.0
        bps = (blocksBroken / secondsElapsed).round(2).toDouble()
        if (System.currentTimeMillis() - lastBrokenBlock > 1000) {
            bps = 0.0
            isBreaking = false
            blocksBroken = 0
            startTime = 0
            lastBrokenBlock = 0
        }
    }
}