package com.github.stivais.ui.elements.impl

import com.github.stivais.ui.color.Color
import com.github.stivais.ui.constraints.Constraints
import com.github.stivais.ui.constraints.Measurement
import com.github.stivais.ui.constraints.measurements.Percent
import com.github.stivais.ui.constraints.measurements.Pixel
import com.github.stivais.ui.constraints.px
import com.github.stivais.ui.elements.Element
import com.github.stivais.ui.utils.replaceUndefined

// todo: Text width doesn't update when height updates
class Text(
    text: String,
    textColor: Color,
    constraints: Constraints?,
    size: Measurement
) : Element(constraints.replaceUndefined(w = 0.px, h = size)) {

    var text: String = text
        set(value) {
            if (field == value) return
            field = value
            needsUpdate = true
        }

    private var needsUpdate = true

    init {
        this.color = textColor
    }

    override fun draw() {
        // todo: make positioning not happen every frame,
            //  because in ClickGUI almost all text uses percent, which basically halves frametime because of calculating width
        if (needsUpdate || constraints.height is Percent) {
            parent?.position() // suboptimal
            (constraints.width as Pixel).pixels = renderer.textWidth(text, height)
            position()
            needsUpdate = false
        }
        renderer.text(text, x, y, height, color!!.rgba)
//        renderer.hollowRect(x, y, width, height, 1f, Color.WHITE.rgba)
    }
}