package me.odinmain.features.impl.skyblock

import com.github.stivais.ui.constraints.percent
import me.odinmain.features.Module
import me.odinmain.features.settings.Setting.Companion.withDependency
import me.odinmain.features.settings.impl.BooleanSetting
import me.odinmain.features.settings.impl.DropdownSetting
import me.odinmain.utils.skyblock.SkyblockPlayer
import me.odinmain.utils.ui.TextHUD
import net.minecraftforge.client.event.RenderGameOverlayEvent
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent

object PlayerDisplay : Module(
    name = "Player Display",
    description = "Displays info about the skyblock player."
) {
    private val hideElements by DropdownSetting("Hide Elements")
    private val hideArmor by BooleanSetting("Hide Armor").withDependency { hideElements }
    private val hideFood by BooleanSetting("Hide Food").withDependency { hideElements }
    private val hideHearts by BooleanSetting("Hide Hearts").withDependency { hideElements }
    private val hideXP by BooleanSetting("Hide XP Level").withDependency { hideElements }
    private val hideActionBar by DropdownSetting("Hide Action Bar Elements")
    private val hideHealth by BooleanSetting("Hide Health", true).withDependency { hideActionBar }
    private val hideMana by BooleanSetting("Hide Mana", true).withDependency { hideActionBar }
    private val hideDefense by BooleanSetting("Hide Defense", true).withDependency { hideActionBar }

    private val healthHUD by TextHUD(
        2.5.percent,
        2.5.percent,
    ) { color, font ->
        if (preview) {
            text("5000/5000❤", font = font, color = color)
        } else {
            needs { SkyblockPlayer.currentHealth != 0 && SkyblockPlayer.maxHealth != 0 }
            text({ "${SkyblockPlayer.currentHealth}/${SkyblockPlayer.maxHealth}❤" }, font = font, color = color)
        }
    }.setting("Health")

    /*private val healthHud: HudElement by HudSetting("Health Hud", 10f, 10f, 1f, true) {
        val text =
            if (it)
                "§c5000/5000❤"
            else if (SkyblockPlayer.currentHealth != 0 && SkyblockPlayer.maxHealth != 0)
                "§c${SkyblockPlayer.currentHealth}/${SkyblockPlayer.maxHealth}❤"
            else return@HudSetting 0f to 0f


        return@HudSetting mcTextAndWidth(text, 2, 2, 2, Color.RED, center = false) * 2f + 2f to 20f
    }
    private val manaHud: HudElement by HudSetting("Mana Hud", 10f, 10f, 1f, true) {
        val text = if (it)
            "§b2000/2000✎"
        else if (SkyblockPlayer.currentMana != 0 && SkyblockPlayer.maxMana != 0)
            "§b${SkyblockPlayer.currentMana}/${SkyblockPlayer.maxMana}✎"
        else return@HudSetting 0f to 0f

        return@HudSetting mcTextAndWidth(text, 2, 2, 2, Color.BLUE, center = false) * 2f + 2f to 20f
    }

    private val defenseHud: HudElement by HudSetting("Defense Hud", 10f, 10f, 1f, true) {
        val text = if (it)
            "§a1000❈"
        else if (SkyblockPlayer.currentDefense != 0)
            "§a${SkyblockPlayer.currentDefense}❈"
        else return@HudSetting 0f to 0f

        return@HudSetting mcTextAndWidth(text, 2, 2, 2, Color.GREEN, center = false) * 2f + 2f to 20f
    }*/

    @SubscribeEvent
    fun onRenderOverlay(event: RenderGameOverlayEvent.Pre) {
        if (event.isCanceled) return // don't override other mods cancelling the event.
        event.isCanceled = when (event.type) {
            RenderGameOverlayEvent.ElementType.ARMOR -> hideArmor
            RenderGameOverlayEvent.ElementType.HEALTH -> hideHearts
            RenderGameOverlayEvent.ElementType.FOOD -> hideFood
            RenderGameOverlayEvent.ElementType.EXPERIENCE -> hideXP
            else -> return
        }
    }


    @JvmStatic
    fun modifyText(text: String): String {
        if (!enabled) return text
        var toReturn = text
        toReturn = if (hideHealth) toReturn.replace("[\\d|,]+/[\\d|,]+❤".toRegex(), "") else toReturn
        toReturn = if (hideMana) toReturn.replace("[\\d|,]+/[\\d|,]+✎ Mana".toRegex(), "") else toReturn
        toReturn = if (hideDefense) toReturn.replace("[\\d|,]+§a❈ Defense".toRegex(), "") else toReturn
        return toReturn
    }
}