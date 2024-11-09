package me.odinmain.utils.ui

import com.github.stivais.ui.UI
import com.github.stivais.ui.Window
import me.odinmain.OdinMain.mc
import me.odinmain.events.impl.GuiEvent
import net.minecraftforge.client.event.RenderWorldLastEvent
import net.minecraftforge.common.MinecraftForge
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import org.lwjgl.input.Mouse
import org.lwjgl.opengl.Display

/**
 * Class that draws handles a [UI][com.github.stivais.ui.UI] instance via MC events.
 *
 * It is required to call [open] and [close] otherwise it will mess up with the eventbus.
 */
class UIHandler(private val ui: UI, private val onlyRender: Boolean = false) : Window {

    private var previousWidth: Int = 0
    private var previousHeight: Int = 0

    fun open() {
        ui.initialize(Display.getWidth(), Display.getHeight(), this)
        MinecraftForge.EVENT_BUS.register(this)
    }

    fun close() {
        ui.cleanup()
        MinecraftForge.EVENT_BUS.unregister(this)
    }

    @SubscribeEvent
    fun onRender(event: RenderWorldLastEvent) {
        val w = mc.framebuffer.framebufferWidth
        val h = mc.framebuffer.framebufferHeight
        if (w != previousWidth || h != previousHeight) {
            ui.resize(w, h)
            previousWidth = w
            previousHeight = h
        }
        if (!onlyRender) {
            ui.eventManager.apply {
                val mx = Mouse.getX().toFloat()
                val my = previousHeight - Mouse.getY() - 1f

                if (this.mouseX != mx || this.mouseY != my || check()) {
                    onMouseMove(mx, my)
                }
            }
        }
        ui.render()
    }

    @SubscribeEvent
    fun onMouseClick(event: GuiEvent.GuiMouseClickEvent) {
        ui.eventManager.onMouseClick(event.button)
    }

    @SubscribeEvent
    fun onMouseReleased(event: GuiEvent.GuiMouseReleaseEvent) {
        ui.eventManager.onMouseRelease(event.button)
    }

    @SubscribeEvent
    fun onKeyboardClick(event: GuiEvent.GuiKeyPressEvent) {
        ui.eventManager.onKeyType(event.char)
    }
}