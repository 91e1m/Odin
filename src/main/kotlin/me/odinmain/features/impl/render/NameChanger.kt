package me.odinmain.features.impl.render

import me.odinmain.features.Module
import me.odinmain.features.settings.impl.StringSetting

/**
 * @see me.odinclient.mixin.mixins.MixinFontRenderer
 */
object NameChanger : Module(
    name = "Name Changer",
    description = "Replaces your name with the given nick, color codes work (&)."
) {
    private val nick by StringSetting("Nick", "Odin", length = 32, placeholder = "Name", description = "The nick to replace your name with.")

    @JvmStatic
    fun modifyString(string: String?): String? {
        if (!enabled || string == null) return string
        return string.replace(mc.session.username, nick.replace("&", "§").replace("$", ""))
    }
}
