package me.odinmain.features.impl.floor7.p3

import io.github.moulberry.notenoughupdates.NEUApi
import me.odinmain.events.impl.*
import me.odinmain.features.Category
import me.odinmain.features.Module
import me.odinmain.features.impl.floor7.p3.termGUI.CustomTermGui
import me.odinmain.features.settings.AlwaysActive
import me.odinmain.features.settings.Setting.Companion.withDependency
import me.odinmain.features.settings.impl.*
import me.odinmain.ui.clickgui.util.ColorUtil
import me.odinmain.ui.clickgui.util.ColorUtil.withAlpha
import me.odinmain.ui.util.MouseUtils
import me.odinmain.utils.equalsOneOf
import me.odinmain.utils.postAndCatch
import me.odinmain.utils.render.*
import me.odinmain.utils.skyblock.modMessage
import me.odinmain.utils.skyblock.unformattedName
import net.minecraft.client.gui.Gui
import net.minecraft.client.renderer.GlStateManager
import net.minecraft.entity.player.InventoryPlayer
import net.minecraft.inventory.ContainerChest
import net.minecraft.inventory.ContainerPlayer
import net.minecraft.item.*
import net.minecraftforge.event.entity.player.ItemTooltipEvent
import net.minecraftforge.fml.common.Loader
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import net.minecraftforge.fml.common.gameevent.TickEvent
import net.minecraftforge.fml.common.gameevent.TickEvent.ClientTickEvent

@AlwaysActive // So it can be used in other modules
object TerminalSolver : Module(
    name = "Terminal Solver",
    description = "Renders solution for terminals in floor 7.",
    category = Category.FLOOR7
) {
    private val lockRubixSolution: Boolean by BooleanSetting("Lock Rubix Solution", false, description = "Locks the 'correct' color of the rubix terminal to the one that was scanned first, should make the solver less 'jumpy'.")
    private val cancelToolTip: Boolean by BooleanSetting("Stop Tooltips", default = true, description = "Stops rendering tooltips in terminals")
    val renderType: Int by SelectorSetting("Mode", "Odin", arrayListOf("Odin", "Skytils", "SBE", "Custom GUI"))
    val customGuiText: Int by SelectorSetting("Custom Gui Title", "Top Left", arrayListOf("Top Left", "Middle", "Disabled")).withDependency { renderType == 3 }
    val customScale: Float by NumberSetting("Custom Scale", 1f, .8f, 2.5f, .1f, description = "Size of the Custom Terminal Gui").withDependency { renderType == 3 }
    val textShadow: Boolean by BooleanSetting("Shadow", true, description = "Adds a shadow to the text")
    val renderOrderNumbers: Boolean by BooleanSetting("Render Order Numbers", true)

    private val showRemoveWrongSettings: Boolean by DropdownSetting("Render Wrong Settings").withDependency { renderType.equalsOneOf(1,2) }
    private val removeWrong: Boolean by BooleanSetting("Stop Rendering Wrong").withDependency { renderType.equalsOneOf(1,2) && showRemoveWrongSettings }
    private val removeWrongPanes: Boolean by BooleanSetting("Stop Panes", true).withDependency { renderType.equalsOneOf(1,2) && showRemoveWrongSettings && removeWrong }
    private val removeWrongRubix: Boolean by BooleanSetting("Stop Rubix", true).withDependency { renderType.equalsOneOf(1,2) && showRemoveWrongSettings && removeWrong }
    private val removeWrongStartsWith: Boolean by BooleanSetting("Stop Starts With", true).withDependency { renderType.equalsOneOf(1,2) && showRemoveWrongSettings && removeWrong }
    private val removeWrongSelect: Boolean by BooleanSetting("Stop Select", true).withDependency { renderType.equalsOneOf(1,2) && showRemoveWrongSettings && removeWrong }

    private val showColors: Boolean by DropdownSetting("Color Settings")
    private val wrongColor: Color by OldColorSetting("Wrong Color", Color(45, 45, 45), true).withDependency { renderType == 0 && showColors }
    val textColor: Color by OldColorSetting("Text Color", Color(220, 220, 220), true).withDependency { showColors }
    val panesColor: Color by OldColorSetting("Panes Color", Color(0, 170, 170), true).withDependency { showColors }
    val rubixColor1: Color by OldColorSetting("Rubix Color 1", Color(0, 170, 170), true).withDependency { showColors }
    val rubixColor2: Color by OldColorSetting("Rubix Color 2", Color(0, 100, 100), true).withDependency { showColors }
    val oppositeRubixColor1: Color by OldColorSetting("Negative Rubix Color 1", Color(170, 85, 0), true).withDependency { showColors }
    val oppositeRubixColor2: Color by OldColorSetting("Negative Rubix Color 2", Color(210, 85, 0), true).withDependency { showColors }
    val orderColor: Color by OldColorSetting("Order Color 1", Color(0, 170, 170, 1f), true).withDependency { showColors }
    val orderColor2: Color by OldColorSetting("Order Color 2", Color(0, 100, 100, 1f), true).withDependency { showColors }
    val orderColor3: Color by OldColorSetting("Order Color 3", Color(0, 65, 65, 1f), true).withDependency { showColors }
    val startsWithColor: Color by OldColorSetting("Starts With Color", Color(0, 170, 170), true).withDependency { showColors }
    val selectColor: Color by OldColorSetting("Select Color", Color(0, 170, 170), true).withDependency { showColors }
    val customGuiColor: Color by OldColorSetting("Custom Gui Color", ColorUtil.moduleButtonColor.withAlpha(.8f), true).withDependency { showColors }
    val gap: Int by NumberSetting("Gap", 10, 0, 20, 1, false, "gap between items").withDependency { renderType == 3 }
    val textScale: Int by NumberSetting("Text Scale", 1, 1, 3, increment = 1, description = "Text scale").withDependency { renderType == 3 }

    private var lastRubixSolution: Int? = null
    private val zLevel get() = if (renderType == 1 && currentTerm.equalsOneOf(TerminalTypes.STARTS_WITH, TerminalTypes.SELECT)) 100f else 400f
    var openedTerminalTime = 0L
    var clicksNeeded = -1


    var currentTerm = TerminalTypes.NONE
    var solution = listOf<Int>()

    @SubscribeEvent
    fun onGuiLoad(event: GuiEvent.GuiLoadedEvent) {
        val newTerm = TerminalTypes.entries.find { event.name.startsWith(it.guiName) } ?: TerminalTypes.NONE
        if (newTerm != currentTerm) {
            currentTerm = newTerm
            openedTerminalTime = System.currentTimeMillis()
            lastRubixSolution = null
        }
        if (currentTerm == TerminalTypes.NONE) return leftTerm()
        val items = event.gui.inventory.subList(0, event.gui.inventory.size - 37)
        when (currentTerm) {
            TerminalTypes.PANES -> solvePanes(items)
            TerminalTypes.RUBIX -> solveColor(items)
            TerminalTypes.ORDER -> solveNumbers(items)
            TerminalTypes.STARTS_WITH -> {
                val letter = Regex("What starts with: '(\\w+)'?").find(event.name)?.groupValues?.get(1) ?: return modMessage("Failed to find letter, please report this!")
                solveStartsWith(items, letter)
            }
            TerminalTypes.SELECT -> {
                val colorNeeded = EnumDyeColor.entries.find { event.name.contains(it.getName().replace("_", " ").uppercase()) }?.unlocalizedName ?: return modMessage("Failed to find color, please report this!")
                solveSelect(items, colorNeeded.lowercase())
            }
            else -> return
        }
        clicksNeeded = solution.size
        if (renderType == 3 && Loader.instance().activeModList.any { it.modId == "notenoughupdates" }) NEUApi.setInventoryButtonsToDisabled()
        TerminalOpenedEvent(currentTerm, solution).postAndCatch()
    }

    @SubscribeEvent
    fun onGuiRender(event: GuiEvent.DrawGuiContainerScreenEvent) {
        if (currentTerm == TerminalTypes.NONE || currentTerm == TerminalTypes.MELODY || !enabled || !renderType.equalsOneOf(0,3) || event.container !is ContainerChest) return
        if (renderType == 3) {
            CustomTermGui.render()
            event.isCanceled = true
            return
        }
        translate(event.guiLeft.toFloat(), event.guiTop.toFloat(), 399f)
        Gui.drawRect(7, 16, event.xSize - 7, event.ySize - 96, wrongColor.rgba)
        translate(-event.guiLeft.toFloat(), -event.guiTop.toFloat(), -399f)
    }

    private fun getShouldBlockWrong(): Boolean {
        if (!removeWrong) return false
        return when (currentTerm) {
            TerminalTypes.PANES -> removeWrongPanes
            TerminalTypes.RUBIX -> removeWrongRubix
            TerminalTypes.ORDER -> true
            TerminalTypes.STARTS_WITH -> removeWrongStartsWith
            TerminalTypes.SELECT -> removeWrongSelect
            else -> false
        }
    }

    @SubscribeEvent
    fun drawSlot(event: GuiEvent.DrawSlotEvent) {
        if ((removeWrong || renderType == 0) && enabled && getShouldBlockWrong() && event.slot.slotIndex <= event.container.inventorySlots.size - 37 && event.slot.slotIndex !in solution && event.slot.inventory !is InventoryPlayer) event.isCanceled = true
        if (event.slot.slotIndex !in solution || event.slot.slotIndex > event.container.inventorySlots.size - 37 || !enabled || renderType == 3 || event.slot.inventory is InventoryPlayer) return

        translate(0f, 0f, zLevel)
        GlStateManager.disableLighting()
        GlStateManager.enableDepth()
        when (currentTerm) {
            TerminalTypes.PANES -> Gui.drawRect(event.x, event.y, event.x + 16, event.y + 16, panesColor.rgba)

            TerminalTypes.RUBIX -> {
                val needed = solution.count { it == event.slot.slotIndex }
                val text = if (needed < 3) needed else (needed - 5)
                val color = when {
                    needed < 3 && text == 2 -> rubixColor2
                    needed < 3 && text == 1 -> rubixColor1
                    text == -2 -> oppositeRubixColor2
                    else -> oppositeRubixColor1
                }

                Gui.drawRect(event.x, event.y, event.x + 16, event.y + 16, color.rgba)
                mcText(text.toString(), event.x + 8f - getMCTextWidth(text.toString()) / 2, event.y + 4.5, 1, textColor, shadow = textShadow, false)
            }
            TerminalTypes.ORDER -> {
                val index = solution.indexOf(event.slot.slotIndex)
                if (index < 3) {
                    val color = when (index) {
                        0 -> orderColor
                        1 -> orderColor2
                        else -> orderColor3
                    }.rgba
                    Gui.drawRect(event.x, event.y, event.x + 16, event.y + 16, color)
                    event.isCanceled = true
                }
                if (renderOrderNumbers) {
                    val amount = event.slot.stack?.stackSize ?: 0
                    mcText(amount.toString(), event.x + 8.5f - getMCTextWidth(amount.toString()) / 2, event.y + 4.5f, 1, textColor, shadow = textShadow, false)
                }
            }
            TerminalTypes.STARTS_WITH ->
                if (renderType != 1 || (renderType == 1 && !removeWrong)) Gui.drawRect(event.x, event.y, event.x + 16, event.y + 16, startsWithColor.rgba)

            TerminalTypes.SELECT ->
                if (renderType != 1 || (renderType == 1 && !removeWrong)) Gui.drawRect(event.x, event.y, event.x + 16, event.y + 16, startsWithColor.rgba)

            else -> {}
        }
        GlStateManager.enableLighting()
        translate(0f, 0f, -zLevel)
    }

    @SubscribeEvent
    fun onTooltip(event: ItemTooltipEvent) {
        if (!cancelToolTip || currentTerm == TerminalTypes.NONE || !enabled) return
        event.toolTip.clear()
    }

    @SubscribeEvent
    fun guiClick(event: GuiEvent.GuiMouseClickEvent) {
        if (renderType != 3 || currentTerm == TerminalTypes.NONE || currentTerm == TerminalTypes.MELODY || !enabled) return
        CustomTermGui.mouseClicked(MouseUtils.mouseX.toInt(), MouseUtils.mouseY.toInt(), event.button)
        event.isCanceled = true
    }

    @SubscribeEvent
    fun itemStack(event: GuiEvent.DrawSlotOverlayEvent) {
        val stack = event.stack?.item?.registryName ?: return
        if (currentTerm != TerminalTypes.ORDER || !enabled || stack != "minecraft:stained_glass_pane") return
        event.isCanceled = true
    }

    @SubscribeEvent
    fun onTick(event: ClientTickEvent) {
        if (event.phase != TickEvent.Phase.END) return
        if (mc.thePlayer?.openContainer is ContainerPlayer) leftTerm()
    }

    @SubscribeEvent
    fun onChat(event: ChatPacketEvent) {
        val match = Regex("(.+) (?:activated|completed) a (?:terminal|lever)! \\((\\d)/(\\d)\\)").find(event.message) ?: return
        val playerName = match.groups[1]?.value
        val completionStatus = match.groups[2]?.value
        if (playerName != mc.thePlayer.name) return
        if (completionStatus == "(7/7)" || completionStatus == "(8/8)") {
            TerminalSolvedEvent(currentTerm).postAndCatch()
            leftTerm()
        }
    }

    private fun leftTerm() {
        if (currentTerm == TerminalTypes.NONE && solution.isEmpty()) return
        TerminalClosedEvent(currentTerm).postAndCatch()
        currentTerm = TerminalTypes.NONE
        solution = emptyList()
    }

    private fun solvePanes(items: List<ItemStack?>) {
        solution = items.filter { it?.metadata == 14 }.map { items.indexOf(it) }
    }

    private val colorOrder = listOf(1, 4, 13, 11, 14)
    private fun solveColor(items: List<ItemStack?>) {
        val panes = items.filter { it?.metadata != 15 && Item.getIdFromItem(it?.item) == 160 }.filterNotNull()
        var temp = List(100) { i -> i }
        if (lastRubixSolution != null && lockRubixSolution) {
            temp = panes.flatMap { pane ->
                if (pane.metadata != lastRubixSolution) {
                    Array(dist(colorOrder.indexOf(pane.metadata), colorOrder.indexOf(lastRubixSolution))) { pane }.toList()
                } else emptyList()
            }.map { items.indexOf(it) }
        } else {
            for (color in colorOrder) {
                val temp2 = panes.flatMap { pane ->
                    if (pane.metadata != color) {
                        Array(dist(colorOrder.indexOf(pane.metadata), colorOrder.indexOf(color))) { pane }.toList()
                    } else emptyList()
                }.map { items.indexOf(it) }
                if (getRealSize(temp2) < getRealSize(temp)) {
                    temp = temp2
                    lastRubixSolution = color
                }
            }
        }
        solution = temp
    }

    private fun getRealSize(list: List<Int>): Int {
        var size = 0
        list.distinct().forEach { pane ->
            val count = list.count { it == pane }
            size += if (count >= 3) 5 - count else count
        }
        return size
    }

    private fun dist(pane: Int, most: Int): Int =
            if (pane > most) (most + colorOrder.size) - pane else most - pane

    private fun solveNumbers(items: List<ItemStack?>) {
        solution = items.filter { it?.metadata == 14 && Item.getIdFromItem(it.item) == 160 }.filterNotNull().sortedBy { it.stackSize }.map { items.indexOf(it) }
    }

    private fun solveStartsWith(items: List<ItemStack?>, letter: String) {
        solution = items.filter { it?.unformattedName?.startsWith(letter, true) == true && !it.isItemEnchanted }.map { items.indexOf(it) }
    }

    private fun solveSelect(items: List<ItemStack?>, color: String) {
        solution = items.filter {
            it?.isItemEnchanted == false &&
            it.unlocalizedName?.contains(color, true) == true &&
            (color == "lightblue" || it.unlocalizedName?.contains("lightBlue", true) == false) && // color BLUE should not accept light blue items.
            Item.getIdFromItem(it.item) != 160
        }.map { items.indexOf(it) }
    }
}

