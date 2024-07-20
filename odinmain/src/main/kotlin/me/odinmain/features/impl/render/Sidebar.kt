package me.odinmain.features.impl.render

import com.github.stivais.ui.color.Color
import com.github.stivais.ui.color.multiplyAlpha
import me.odinmain.features.Module
import me.odinmain.features.settings.impl.*
import net.minecraft.client.gui.ScaledResolution
import net.minecraft.scoreboard.ScoreObjective

object Sidebar : Module(
    name = "Sidebar",
    description = "Various settings to change the look of the minecraft sidebar."
) {
    private var variableScoreObjective: ScoreObjective? = null
    /*private val hud: HudElement by HudSetting("Hud", 500f, 500f, 2f, false) {
        val scoreObjective = variableScoreObjective ?: return@HudSetting 0f to 0f
        val scoreboard: Scoreboard = scoreObjective.scoreboard
        val scoreList: MutableList<Score> = ArrayList()
        var width: Int = getStringWidth(scoreObjective.displayName)
        for (score in scoreboard.getSortedScores(scoreObjective)) {
            val name = score.playerName
            if (scoreList.size < 15 && name != null && !name.startsWith("#")) {
                val scorePlayerTeam = scoreboard.getPlayersTeam(name)
                val str = ScorePlayerTeam.formatPlayerName(scorePlayerTeam, name) + (if (this.redNumbers) (": " + EnumChatFormatting.RED + score.scorePoints) else "")
                width = max(width, getStringWidth(str))
                scoreList.add(score)
            }
        }
        var index = 0
        val color: Int = backgroundColor.rgba
        for (score in scoreList.reversed()) {
            index++
            val team = scoreboard.getPlayersTeam(score.playerName)
            val s1 = ScorePlayerTeam.formatPlayerName(team, score.playerName)
            val s2 = "§c" + score.scorePoints
            val scoreY: Int = index * mc.fontRendererObj.FONT_HEIGHT
            drawRect(-2, scoreY, width, scoreY + mc.fontRendererObj.FONT_HEIGHT, color)
            drawString(s1, 0, scoreY)
            if (this.redNumbers) {
                drawString(s2, width - getStringWidth(s2), scoreY)
            }
            if (index == scoreList.size) {
                val s3 = scoreObjective.displayName
                drawRect(-2, 0, width, mc.fontRendererObj.FONT_HEIGHT, color)
                drawString(s3, width - getStringWidth(s3), 1)
            }
        }
        GlStateManager.resetColor()
        Color.WHITE.bind()
        width.toFloat() to (scoreList.size + 1) * mc.fontRendererObj.FONT_HEIGHT.toFloat()
    }*/
    private val customFont by SelectorSetting("Font", "Minecraft", arrayListOf("Minecraft", "Custom"))
    private val textShadow: Boolean by BooleanSetting("Text Shadow")
    private val redNumbers: Boolean by BooleanSetting("Show Red Numbers")
    private val backgroundColor: Color by ColorSetting("Background Color", Color.MINECRAFT_GRAY.multiplyAlpha(.5f), allowAlpha = true)

    fun renderSidebar(scoreObjective: ScoreObjective, scaledResolution: ScaledResolution): Boolean {
        variableScoreObjective = scoreObjective
        return this.enabled
    }

    private fun drawString(str: String, x: Int, y: Int) {
      /*  if (customFont)
            //text(str, x - 1, y + 3, Color.WHITE, 7, shadow = textShadow)
        else
           // mcText(str, x, y, 1, Color.WHITE, shadow = textShadow, center = false)*/
    }

    private fun getStringWidth(str: String): Int {
        return mc.fontRendererObj.getStringWidth(str)
    }
}