package me.odinmain.utils.ui.elements

import com.github.stivais.ui.UI
import com.github.stivais.ui.color.Color
import com.github.stivais.ui.constraints.*
import com.github.stivais.ui.elements.impl.TextElement
import com.github.stivais.ui.events.Event
import com.github.stivais.ui.events.Focused
import com.github.stivais.ui.events.Key
import com.github.stivais.ui.events.Mouse
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.GuiScreen
import net.minecraft.util.ChatAllowedCharacters
import org.lwjgl.input.Keyboard
import kotlin.math.min
import kotlin.math.max

class TextInput(
    text: String,
    private val placeholder: String,
    position: Positions? = null,
    size: Size,
    private val widthLimit: Size? = null,
    censor: Boolean = false,
    private val onlyNumbers: Boolean = false,
    onTextChange: (event: TextChanged) -> Unit
) : TextElement(text, UI.defaultFont, Color.WHITE, position ?: at(), size) {

    private val cursorBlinkRate = 500L
    private var lastBlinkTime = System.currentTimeMillis()
    private var cursorVisible = true

    override var text: String = text
        set(value) {
            if (field == value) return
            val event = TextChanged(value)
            accept(event)
            if (!event.cancelled) {
                field = value
                redraw = true
                previousHeight = 0f
                if (history.last() != value) history.add(value)
                if (censorInput) censorCache = "*".repeat(value.length)
                updateCaret()
                updateSelectionX()
            }
        }

    private var caretPosition = text.length
        set(value) {
            field = value.coerceIn(0, text.length)
            updateCaret()
            lastBlinkTime = System.currentTimeMillis()
            cursorVisible = true
        }

    private var selectionStart = caretPosition
        set(value) {
            field = value.coerceIn(0, text.length)
            updateSelectionX()
        }

    private var caretX = 0f
    private var selectionX = 0f
    private var dragging = false
    var censorInput = censor
    private var censorCache: String? = if (censor) "*".repeat(text.length) else null
    private val history by lazy { mutableListOf(text) }
    private var lastClickTime = 0L
    private var clickCount = 0

    init {
        TextChanged().register {
            onTextChange(it)
            false
        }

        Focused.Gained register {
            Keyboard.enableRepeatEvents(true)
            updateCaretFromMouse()
            selectionStart = caretPosition
            false
        }

        Focused.Lost register {
            selectionStart = caretPosition
            Keyboard.enableRepeatEvents(false)
            false
        }

        Key.CodePressed(-1, true) register {
            handleKeyInput(it.code)
            true
        }

        Mouse.Clicked(0) register {
            dragging = true
            ui.focus(this)
            handleMouseClick()
            false
        }

        Mouse.Moved register {
            if (dragging) {
                updateCaretFromMouse()
                return@register true
            }
            lastClickTime = 0L
            false
        }

        Mouse.Released(0) register {
            dragging = false
            false
        }
    }

    override fun draw() {
        val currentTime = System.currentTimeMillis()
        if (currentTime - lastBlinkTime > cursorBlinkRate) {
            cursorVisible = !cursorVisible
            lastBlinkTime = currentTime
            redraw = true
        }

        // Draw selection background
        if (selectionStart != caretPosition) {
            val startX = x + min(selectionX, caretX)
            val endX = x + max(selectionX, caretX)
            renderer.rect(startX, y + 2, endX - startX, height - 2, Color.RGB(0, 120, 215, 0.4f).rgba)
        }

        // Draw text or placeholder
        when {
            text.isEmpty() && !ui.isFocused(this) -> {
                renderer.text(placeholder, x, y, height, Color.RGB(169, 169, 169).rgba)
            }
            censorInput -> {
                renderer.text(censorCache!!, x, y, height, color!!.get(this))
            }
            else -> {
                renderer.text(text, x, y, height, color!!.get(this))
            }
        }

        // Draw cursor
        if (ui.isFocused(this) && cursorVisible) {
            renderer.rect(x + caretX, y + 2, 1f, height - 4, Color.WHITE.rgba)
        }
    }

    override fun getTextWidth(): Float {
        return when {
            text.isEmpty() -> renderer.textWidth(placeholder, height)
            censorInput -> renderer.textWidth(censorCache!!, height)
            else -> super.getTextWidth()
        }
    }

//    override fun preSize() {
//        super.preSize()
//        if (widthLimit != null) {
//            val maxW = widthLimit.get(this, Type.W)
//            if (width >= maxW) {
//                offs = width - maxW
//                width = maxW
//            } else {
//                offs = 0f
//            }
//        }
//    }

    private fun updateCaret() {
        val visibleText = if (censorInput) censorCache!! else text
        val safeCaretPosition = caretPosition.coerceIn(0, visibleText.length)
        caretX = renderer.textWidth(visibleText.substring(0, safeCaretPosition), height)
    }

    private fun updateSelectionX() {
        val visibleText = if (censorInput) censorCache!! else text
        val safeSelectionStart = selectionStart.coerceIn(0, visibleText.length)
        selectionX = renderer.textWidth(visibleText.substring(0, safeSelectionStart), height)
    }

    private fun updateCaretFromMouse() {
        val mouseX = ui.mx - x
        var newPos = 0
        var currentWidth = 0f
        val visibleText = if (censorInput) censorCache!! else text

        for (i in visibleText.indices) {
            val charWidth = renderer.textWidth(visibleText[i].toString(), height)
            if (currentWidth + (charWidth / 2) > mouseX) break
            currentWidth += charWidth
            newPos = i + 1
        }

        caretPosition = newPos
        if (!isShiftKeyDown() && !dragging) {
            selectionStart = caretPosition
        }
    }

    private fun handleKeyInput(keyCode: Int) {
        val char = Keyboard.getEventCharacter()

        when {
            isKeyComboCtrlA(keyCode) -> selectAll()
            isKeyComboCtrlC(keyCode) -> copySelection()
            isKeyComboCtrlV(keyCode) -> paste()
            isKeyComboCtrlX(keyCode) -> cut()
            isKeyComboCtrlZ(keyCode) -> undo()

            keyCode == Keyboard.KEY_LEFT -> handleLeftArrow()
            keyCode == Keyboard.KEY_RIGHT -> handleRightArrow()
            keyCode == Keyboard.KEY_HOME -> handleHome()
            keyCode == Keyboard.KEY_END -> handleEnd()
            keyCode == Keyboard.KEY_BACK -> handleBackspace()
            keyCode == Keyboard.KEY_DELETE -> handleDelete()

            keyCode == Keyboard.KEY_RETURN || keyCode == Keyboard.KEY_NUMPADENTER -> ui.unfocus()
            keyCode == Keyboard.KEY_ESCAPE -> {
                text = history.first()
                ui.unfocus()
            }

            else -> {
                if (ChatAllowedCharacters.isAllowedCharacter(char)) {
                    if (onlyNumbers) {
                        if (char.isDigit() || (char == '-' && text.isEmpty()) || (char == '.' && !text.contains('.'))) {
                            insertText(char.toString())
                        }
                    } else {
                        insertText(char.toString())
                    }
                }
            }
        }
    }

    private fun handleMouseClick() {
        val currentTime = System.currentTimeMillis()
        if (currentTime - lastClickTime < 500) {
            clickCount++
        } else {
            clickCount = 1
        }
        lastClickTime = currentTime

        when (clickCount) {
            1 -> {
                updateCaretFromMouse()
                if (!isShiftKeyDown()) selectionStart = caretPosition
            }
            2 -> selectWord()
            3 -> selectAll()
        }
    }

    private fun insertText(str: String) {
        val start = min(caretPosition, selectionStart).coerceIn(0, text.length)
        val end = max(caretPosition, selectionStart).coerceIn(0, text.length)

        text = text.substring(0, start) + str + text.substring(end)
        caretPosition = start + str.length
        selectionStart = caretPosition
    }

    private fun deleteSelection() {
        if (selectionStart == caretPosition) return
        val start = min(caretPosition, selectionStart)
        val end = max(caretPosition, selectionStart)
        text = text.substring(0, start) + text.substring(end)
        caretPosition = start
        selectionStart = caretPosition
    }

    // Helper functions for keyboard shortcuts
    private fun selectAll() {
        selectionStart = 0
        caretPosition = text.length
    }

    private fun copySelection() {
        if (selectionStart != caretPosition) {
            val start = min(selectionStart, caretPosition)
            val end = max(selectionStart, caretPosition)
            GuiScreen.setClipboardString(text.substring(start, end))
        }
    }

    private fun paste() {
        val clipboard = GuiScreen.getClipboardString()
        if (clipboard.isNotEmpty()) {
            deleteSelection()
            insertText(clipboard)
        }
    }

    private fun cut() {
        copySelection()
        deleteSelection()
    }

    private fun undo() {
        if (history.size > 1) {
            history.removeAt(history.lastIndex)
            text = history.last()
            caretPosition = text.length
            selectionStart = caretPosition
        }
    }

    private fun selectWord() {
        var start = caretPosition
        var end = caretPosition

        // Move start left until a space or the beginning
        while (start > 0 && !text[start - 1].isWhitespace()) start--
        // Move end right until a space or the end
        while (end < text.length && !text[end].isWhitespace()) end++

        selectionStart = start
        caretPosition = end
    }

    // Standard keyboard input handlers
    private fun handleLeftArrow() {
        if (isCtrlKeyDown()) {
            var newPos = caretPosition
            // Skip spaces
            while (newPos > 0 && text[newPos - 1].isWhitespace()) newPos--
            // Skip word
            while (newPos > 0 && !text[newPos - 1].isWhitespace()) newPos--
            if (isShiftKeyDown()) caretPosition = newPos else {
                caretPosition = newPos
                selectionStart = caretPosition
            }
        } else {
            if (isShiftKeyDown()) caretPosition-- else {
                caretPosition--
                selectionStart = caretPosition
            }
        }
    }

    private fun handleRightArrow() {
        if (isCtrlKeyDown()) {
            var newPos = caretPosition
            // Skip spaces
            while (newPos < text.length && text[newPos].isWhitespace()) newPos++
            // Skip word
            while (newPos < text.length && !text[newPos].isWhitespace()) newPos++
            if (isShiftKeyDown()) caretPosition = newPos else {
                caretPosition = newPos
                selectionStart = caretPosition
            }
        } else {
            if (isShiftKeyDown()) caretPosition++ else {
                caretPosition++
                selectionStart = caretPosition
            }
        }
    }

    private fun handleHome() {
        if (isShiftKeyDown()) caretPosition = 0 else {
            caretPosition = 0
            selectionStart = 0
        }
    }

    private fun handleEnd() {
        if (isShiftKeyDown()) caretPosition = text.length else {
            caretPosition = text.length
            selectionStart = text.length
        }
    }

    private fun handleBackspace() {
        if (selectionStart != caretPosition) {
            deleteSelection()
        } else if (caretPosition > 0) {
            if (isCtrlKeyDown()) {
                var newPos = caretPosition
                while (newPos > 0 && text[newPos - 1].isWhitespace()) newPos--
                while (newPos > 0 && !text[newPos - 1].isWhitespace()) newPos--
                text = text.substring(0, newPos) + text.substring(caretPosition)
                caretPosition = newPos
            } else {
                text = text.substring(0, caretPosition - 1) + text.substring(caretPosition)
                caretPosition--
            }
            selectionStart = caretPosition
        }
    }

    private fun handleDelete() {
        if (selectionStart != caretPosition) {
            deleteSelection()
        } else if (caretPosition < text.length) {
            if (isCtrlKeyDown()) {
                var newPos = caretPosition
                while (newPos < text.length && text[newPos].isWhitespace()) newPos++
                while (newPos < text.length && !text[newPos].isWhitespace()) newPos++
                text = text.substring(0, caretPosition) + text.substring(newPos)
            } else {
                text = text.substring(0, caretPosition) + text.substring(caretPosition + 1)
            }
        }
    }

    private fun isCtrlKeyDown(): Boolean {
        return if (Minecraft.isRunningOnMac) Keyboard.isKeyDown(219) || Keyboard.isKeyDown(220) else Keyboard.isKeyDown(
            29
        ) || Keyboard.isKeyDown(157)
    }

    private fun isShiftKeyDown(): Boolean {
        return Keyboard.isKeyDown(42) || Keyboard.isKeyDown(54)
    }

    private fun isAltKeyDown(): Boolean {
        return Keyboard.isKeyDown(56) || Keyboard.isKeyDown(184)
    }

    private fun isKeyComboCtrlX(keyID: Int): Boolean {
        return keyID == 45 && isCtrlKeyDown() && !isShiftKeyDown() && !isAltKeyDown()
    }

    private fun isKeyComboCtrlV(keyID: Int): Boolean {
        return keyID == 47 && isCtrlKeyDown() && !isShiftKeyDown() && !isAltKeyDown()
    }

    private fun isKeyComboCtrlC(keyID: Int): Boolean {
        return keyID == 46 && isCtrlKeyDown() && !isShiftKeyDown() && !isAltKeyDown()
    }

    private fun isKeyComboCtrlA(keyID: Int): Boolean {
        return keyID == 30 && isCtrlKeyDown() && !isShiftKeyDown() && !isAltKeyDown()
    }

    private fun isKeyComboCtrlZ(keyID: Int): Boolean {
        return keyID == Keyboard.KEY_Z && isCtrlKeyDown() && !isShiftKeyDown() && !isAltKeyDown()
    }

    data class TextChanged(val string: String = "") : Event {

        var cancelled: Boolean = false

        fun cancel() {
            cancelled = true
        }

        override fun hashCode(): Int {
            return 9999
        }

        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            return other is TextChanged
        }
    }
}