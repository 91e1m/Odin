package me.odinmain.features.impl.skyblock

import me.odinmain.features.Module
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import net.minecraftforge.fml.common.gameevent.TickEvent
import net.minecraftforge.fml.common.gameevent.TickEvent.RenderTickEvent
import org.lwjgl.input.Mouse

object SkillsSucks : Module(
    name = "Skills Sucks",
    description = "Skills sucks."
) {

    private var currentPronoun: String? = null
    private var lastMessage: String? = null

    @SubscribeEvent
    fun renderTickEvent(event: RenderTickEvent) {
        if (event.phase != TickEvent.Phase.END) return
        val chatComponent = mc.ingameGUI?.chatGUI?.getChatComponent(Mouse.getX(), Mouse.getY())?.formattedText ?: return
        if (!chatComponent.contains("♲")) return

        if (chatComponent != lastMessage) {
            lastMessage = chatComponent
            currentPronoun = pronouns.random()
        }

        //drawDynamicTexture(skillsSucks, Mouse.getX().toFloat(), Mouse.getY().toFloat(), 150f, 150f)
        //mcText(currentPronoun ?: pronouns.random(), Mouse.getX().toFloat() + 75, Mouse.getY().toFloat() + 150f, 2, Color.WHITE)
    }

    private val pronouns = listOf(
        "they/them",
        "she/her/hers",
        "he/him/his",
        "ze/zir/zir",
        "fae/faer/faer",
        "sie/hir/sie",
        "ey/em/eir",
        "co/cos/cos",
        "xe/xer/xem",
        "thon/thon/thon"
    )
}