package me.odinmain.features.settings.impl

import com.github.stivais.aurora.utils.loop
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import me.odinmain.features.huds.HUD
import me.odinmain.features.settings.Saving
import me.odinmain.features.settings.Setting

class HUDSetting(
    name: String,
    hud: HUD,
    description: String,
) : Setting<HUD>(name, false, description), Saving {

    override val default: HUD = hud
    override var value: HUD = hud

    override fun write(): JsonElement {
        return JsonObject().apply {
            for (setting in value.settings) {
                if (setting !is Saving) continue
                add(setting.name, setting.write())
            }
        }
    }

    override fun read(element: JsonElement?) {
        element?.asJsonObject?.apply {
            for (entry in entrySet()) {
                val setting = value.getSettingByName(entry.key) as? Saving ?: continue
                setting.read(entry.value)
            }
        }
    }

    override fun reset() {
        value.settings.loop {
            it.reset()
        }
    }
}