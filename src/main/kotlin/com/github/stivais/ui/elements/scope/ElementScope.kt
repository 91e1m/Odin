package com.github.stivais.ui.elements.scope

import com.github.stivais.ui.UI
import com.github.stivais.ui.color.Color
import com.github.stivais.ui.constraints.*
import com.github.stivais.ui.constraints.measurements.Undefined
import com.github.stivais.ui.constraints.sizes.Bounding
import com.github.stivais.ui.constraints.sizes.Copying
import com.github.stivais.ui.elements.Element
import com.github.stivais.ui.elements.impl.*
import com.github.stivais.ui.events.*
import com.github.stivais.ui.operation.UIOperation
import com.github.stivais.ui.renderer.Font
import com.github.stivais.ui.renderer.Gradient
import com.github.stivais.ui.renderer.Image
import com.github.stivais.ui.transforms.Transforms
import com.github.stivais.ui.utils.radius

open class ElementScope<E: Element>(val element: E) {

    val ui: UI
        get() = element.ui

    val parent: Element?
        get() = element.parent

    val x: Position
        get() = element.constraints.x

    val y: Position
        get() = element.constraints.y

    val width: Size
        get() = element.constraints.width

    val height: Size
        get() = element.constraints.height

    var color: Color?
        get() = element.color
        set(value) {
            element.color = value
        }

    var enabled: Boolean
        get() = element.enabled
        set(value) {
            element.enabled = value
        }

    fun parent(): ElementScope<*>? = parent?.createScope()

    fun child(index: Int): ElementScope<*>? = element.elements?.get(index)?.createScope()

    fun sibling(distance: Int = 1): ElementScope<*>? {
        if (element.parent != null) {
            val currIndex = element.parent!!.elements!!.indexOf(element)
            val sibling = element.parent!!.elements!!.getOrNull(currIndex + distance) ?: return null
            return sibling.createScope()
        }
        return null
    }

    @ScopeDSL
    fun group(
        constraints: Constraints? = null,
        block: ElementScope<Group>.() -> Unit = {}
    ) = create(ElementScope(Group(constraints)), block)

    @ScopeDSL
    fun column(
        constraints: Constraints? = null,
        padding: Size? = null,
        block: LayoutScope.() -> Unit = {}
    ) = create(LayoutScope(Layout.Column(constraints, padding)), block)

    @ScopeDSL
    fun row(
        constraints: Constraints? = null,
        padding: Size? = null,
        block: LayoutScope.() -> Unit = {}
    ) = create(LayoutScope(Layout.Row(constraints, padding)), block)

    @ScopeDSL
    fun block(
        constraints: Constraints? = null,
        color: Color,
        outlineColor: Color? = null,
        outlineThickness: Measurement? = null,
        radius: FloatArray? = null,
        block: BlockScope.() -> Unit = {}
    ) = create(BlockScope(Block(constraints, color, outlineColor, outlineThickness, radius)), block)

    @ScopeDSL
    fun block(
        constraints: Constraints? = null,
        colors: Pair<Color, Color>,
        radius: FloatArray? = null,
        gradient: Gradient,
        block: BlockScope.() -> Unit = {}
    ) = create(BlockScope(Block.Gradient(constraints, colors.first, colors.second, radius, gradient)), block)

    @ScopeDSL
    fun text(
        text: String,
        font: Font = UI.defaultFont,
        pos: Positions = at(Undefined, Undefined),
        size: Size = 50.percent,
        color: Color = Color.WHITE,
        block: TextScope.() -> Unit = {}
    ) = create(TextScope(TextElement(text, font, color, pos, size)), block)

    @ScopeDSL
    fun text(
        text: () -> Any?,
        font: Font = UI.defaultFont,
        color: Color = Color.WHITE,
        pos: Positions = at(Undefined, Undefined),
        size: Size = 50.percent,
        block: TextScope.() -> Unit = {}
    ) = create(TextScope(TextElement.Supplied(text, font, color, pos, size)), block)

    @ScopeDSL
    fun image(
        image: Image,
        constraints: Constraints? = null,
        radius: FloatArray = 0.radius(),
        dsl: ElementScope<ImageElement>.() -> Unit = {}
    ) = create(ElementScope(ImageElement(image, constraints, radius)), dsl)

    @ScopeDSL
    fun image(
        image: String,
        constraints: Constraints? = null,
        radius: FloatArray = 0.radius(),
        dsl: ElementScope<ImageElement>.() -> Unit = {}
    ) = create(ElementScope(ImageElement(Image(image), constraints, radius)), dsl)

    @ScopeDSL
    fun scrollable(
        constraints: Constraints = size(Bounding, Bounding),
        dsl: ScrollableScope.() -> Unit
    ) = create(ScrollableScope(Scrollable(constraints)), dsl)

    fun onCreation(block: () -> Unit) {
        element.registerEvent(Lifetime.Initialized) {
            block()
            false
        }
    }

    fun afterCreation(block: () -> Unit) {
        element.registerEvent(Lifetime.AfterInitialized) {
            block()
            false
        }
    }

    fun onRemove(block: () -> Unit) {
        element.registerEvent(Lifetime.Uninitialized) {
            block()
            false
        }
    }

    fun onClick(button: Int = 0, block: (Mouse.Clicked) -> Boolean) {
        element.registerEvent(Mouse.Clicked(button), block)
    }

    fun onClick(vararg buttons: Int, block: (Mouse.Clicked) -> Boolean) {
        for (i in buttons) {
            element.registerEvent(Mouse.Clicked(i), block)
        }
    }

    fun onFocusedClick(button: Int = 0, block: (Focused.Clicked) -> Boolean) {
        element.registerEvent(Focused.Clicked(button), block)
    }

    fun onRelease(button: Int = 0, block: (Mouse.Released) -> Unit) {
        element.registerEvent(Mouse.Released(button)) {
            block(this)
            false
        }
    }

    fun onScroll(block: (Mouse.Scrolled) -> Boolean) {
        element.registerEvent(Mouse.Scrolled(0f), block)
    }

    fun onKeyPressed(block: (Key.CodePressed) -> Boolean) {
        element.registerEvent(Key.CodePressed(-1, true), block)
    }

    fun onKeyRelease(block: (Key.CodePressed) -> Boolean) {
        element.registerEvent(Key.CodePressed(-1, false), block)
    }

    fun onMouseEnter(block: (Event) -> Unit) {
        element.registerEvent(Mouse.Entered) {
            block(this)
            false
        }
    }

    fun onMouseExit(block: (Event) -> Unit) {
        element.registerEvent(Mouse.Exited) {
            block(this)
            false
        }
    }

    fun onMouseEnterExit(block: (Event) -> Unit) {
        val func: Event.() -> Boolean = { block(this); false }
        element.registerEvent(Mouse.Entered, func)
        element.registerEvent(Mouse.Exited, func)
    }

    fun onMouseMove(block: (Mouse.Moved) -> Boolean) {
        element.registerEvent(Mouse.Moved, block)
    }

    fun onFocusGain(block: (Event) -> Unit) {
        element.registerEvent(Focused.Gained) {
            block(this)
            false
        }
    }

    fun onFocusLost(block: (Event) -> Unit) {
        element.registerEvent(Focused.Lost) {
            block(this)
            false
        }
    }

    fun redraw() {
        element.redraw = true
    }

    fun scissors() {
        element.scissors = true
    }

    fun focusThis() {
        ui.eventManager.focus(element)
    }

    fun <E : Element, S : ElementScope<E>> create(scope: S, dsl: S.() -> Unit = {}) : S {
        this.element.addElement(scope.element)
        scope.dsl()
        return scope
    }

    fun <E : Element> E.add(): E {
        this@ElementScope.element.addElement(this)
        return this
    }

    fun addElement(element: Element) {
        this@ElementScope.element.addElement(element)
    }

    fun operation(operation: UIOperation) {
        element.addOperation(operation)
    }

    fun UIOperation.add() {
        element.addOperation(this)
    }

    fun transform(transform: Transforms) {
        element.addTransform(transform)
    }

    fun Element.scope(block: ElementScope<*>.() -> Unit) {
        add()
        block(createScope())
    }
}

open class BlockScope(block: Block) : ElementScope<Block>(block) {

    val outlineColor: Color?
        get() = element.outlineColor

    val outline: Measurement?
        get() = element.outlineThickness

    @ScopeDSL
    fun outline(color: Color, thickness: Measurement = 1.px) {
        element.outlineColor = color
        element.outlineThickness = thickness
    }
}

open class LayoutScope(layout: Layout) : ElementScope<Layout>(layout) {
    @ScopeDSL
    fun background(color: Color) {
        element.color = color
    }

    // temporary
    @ScopeDSL
    fun divider(amount: Size) {
        element.createDivider(amount)
    }

    @ScopeDSL
    fun section(size: Size, block: ElementScope<Group>.() -> Unit) = group(size(Copying, size), block)
}

@DslMarker
annotation class ScopeDSL

typealias ElementDSL = ElementScope<*>
