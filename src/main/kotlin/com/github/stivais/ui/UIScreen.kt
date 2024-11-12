package com.github.stivais.ui

import me.odinmain.OdinMain.display
import me.odinmain.OdinMain.mc
import net.minecraft.client.gui.GuiScreen
import net.minecraftforge.client.event.RenderWorldLastEvent
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import org.lwjgl.input.Keyboard
import org.lwjgl.input.Mouse
import org.lwjgl.opengl.Display

open class UIScreen(val ui: UI) : GuiScreen(), Window {

    private var previousWidth: Int = 0
    private var previousHeight: Int = 0
    private val pressedKeys = mutableSetOf<Int>()

    fun close() {
        if (mc.currentScreen == null) {
            // assume current is the ui rendering
            closeAnimHandler = null
        } else if (mc.currentScreen == this) {
            mc.displayGuiScreen(null)
        }
    }

    override fun initGui() {
        val start = System.nanoTime()
        ui.initialize(Display.getWidth(), Display.getHeight(), this)
        println("UI initialization took: ${System.nanoTime() - start}")
    }

    override fun onGuiClosed() {
        ui.cleanup()
    }

    override fun drawScreen(mouseX: Int, mouseY: Int, partialTicks: Float) {
        ui.measureFrametime {
            val w = mc.framebuffer.framebufferWidth
            val h = mc.framebuffer.framebufferHeight
            if (w != previousWidth || h != previousHeight) {
                ui.resize(w, h)
                previousWidth = w
                previousHeight = h
            }

            ui.eventManager.apply {
                val mx = Mouse.getX().toFloat()
                val my = previousHeight - Mouse.getY() - 1f

                if (this.mouseX != mx || this.mouseY != my || check()) {
                    onMouseMove(mx, my)
                }
            }
            ui.render()
        }

        for (key in pressedKeys.toList()) {
            if (!Keyboard.isKeyDown(key)) {
                ui.eventManager.onKeyReleased(key)
                pressedKeys.remove(key)
            }
        }
    }

    override fun handleMouseInput() {
        super.handleMouseInput()
        val scroll = Mouse.getEventDWheel()
        if (scroll != 0) {
            ui.eventManager.onMouseScroll(scroll.toFloat())
        }
    }

    override fun mouseClicked(mouseX: Int, mouseY: Int, button: Int) {
        ui.eventManager.onMouseClick(button)
    }

    override fun mouseReleased(mouseX: Int, mouseY: Int, button: Int) {
        ui.eventManager.onMouseRelease(button)
    }

    override fun keyTyped(typedChar: Char, keyCode: Int) {
        if (ui.eventManager.onKeyType(typedChar)) return
        if (ui.eventManager.onKeycodePressed(keyCode)) {
            pressedKeys.add(keyCode)
            return
        }
        super.keyTyped(typedChar, keyCode)
    }

    override fun doesGuiPauseGame(): Boolean = false

    companion object {
        fun open(ui: UI) {
            display = UIScreen(ui)
        }

        @JvmName("openUI")
        fun UI.open() {
            open(this)
        }

        fun UI.init(): UI {
            initialize(Display.getWidth(), Display.getHeight())
            return this
        }

        var closeAnimHandler: UIScreen? = null

        @SubscribeEvent
        fun onRender(event: RenderWorldLastEvent) {
            if (mc.currentScreen == null) {
                closeAnimHandler?.drawScreen(0, 0, event.partialTicks)
            }
        }
    }
}