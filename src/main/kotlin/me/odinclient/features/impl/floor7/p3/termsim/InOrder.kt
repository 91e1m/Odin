package me.odinclient.features.impl.floor7.p3.termsim

import me.odinclient.events.impl.GuiLoadedEvent
import me.odinclient.features.impl.floor7.p3.TerminalSolver
import net.minecraft.inventory.ContainerChest
import net.minecraft.inventory.Slot
import net.minecraft.item.ItemStack
import kotlin.math.floor

object InOrder : TermSimGui(
    "Click in order!",
    36
) {
    override fun create() {
        val used = (1..14).shuffled().toMutableList()
        inventorySlots.inventorySlots.subList(0, size).forEachIndexed { index, it ->
            if (floor(index / 9.0) in 1.0..2.0 && index % 9 in 1..7) {
                it.putStack(ItemStack(pane, used.first(), 14).apply { setStackDisplayName("") })
                used.removeFirst()
            }
            else it.putStack(blackPane)
        }
    }

    override fun slotClick(slot: Slot, button: Int) {
        if (
            inventorySlots.inventorySlots
                .subList(0, size)
                .filter { it.stack.metadata == 14 }
                .minByOrNull { it.stack.stackSize } != slot
        ) return
        slot.putStack(ItemStack(pane, slot.stack.stackSize, 5).apply { setStackDisplayName("") })
        mc.thePlayer.playSound("random.orb", 1f, 1f)
        TerminalSolver.onGuiLoad(GuiLoadedEvent(name, inventorySlots as ContainerChest))
        if (inventorySlots.inventorySlots.subList(0, size).none { it?.stack?.metadata == 14 }) {
            solved()
        }
    }
}