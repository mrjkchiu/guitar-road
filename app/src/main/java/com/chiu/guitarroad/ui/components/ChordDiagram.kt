package com.chiu.guitarroad.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.chiu.guitarroad.ui.theme.BrickRed
import com.chiu.guitarroad.ui.theme.InkSecondary
import com.chiu.guitarroad.ui.theme.Wood

/**
 * 和弦指板图：6 弦 × 5 品。
 * strings / fingers 数组为 6→1 弦顺序；UI 从左到右画 6→1 弦。
 * -1 = 不弹（画 ×），0 = 空弦（画 ○），n > 0 = 按第 n 品。
 */
@Composable
fun ChordDiagram(strings: List<Int>, fingers: List<Int>, modifier: Modifier = Modifier) {
    val textMeasurer = rememberTextMeasurer()
    Canvas(modifier = modifier.then(Modifier.size(216.dp, 204.dp))) {
        val topPad = 20.dp.toPx()
        val bottomPad = 4.dp.toPx()
        val baseLeftPad = 12.dp.toPx()
        val rightPad = 12.dp.toPx()

        val pressed = strings.filter { it > 0 }
        val minFret = pressed.minOrNull() ?: 1
        val showFretLabel = minFret > 1
        val leftPad = baseLeftPad + if (showFretLabel) 24.dp.toPx() else 0f

        val gridWidth = size.width - leftPad - rightPad
        val gridHeight = size.height - topPad - bottomPad
        val cellW = gridWidth / 5f
        val cellH = gridHeight / 5f

        fun stringX(index: Int): Float = leftPad + index * cellW
        fun fretY(row: Int): Float = topPad + row * cellH
        // 品格号换算为显示行（1 起）：起始品 > 1 时顶行即为起始品
        fun displayRow(fret: Int): Int = if (showFretLabel) fret - minFret + 1 else fret
        fun fretCenterY(fret: Int): Float = fretY(displayRow(fret) - 1) + cellH / 2f

        // 弦竖线
        for (i in 0 until 6) {
            val x = stringX(i)
            drawLine(
                color = InkSecondary,
                start = Offset(x, topPad),
                end = Offset(x, topPad + 5 * cellH),
                strokeWidth = 1.5f
            )
        }
        // 品丝横线：第 0 根（琴枕）加粗
        for (j in 0..5) {
            drawLine(
                color = Wood,
                start = Offset(leftPad, fretY(j)),
                end = Offset(leftPad + 5 * cellW, fretY(j)),
                strokeWidth = if (j == 0) 4f else 2f
            )
        }

        // 起始品标注，如 5fr
        if (showFretLabel) {
            val label = "${minFret}fr"
            val style = TextStyle(fontSize = 12.sp, color = InkSecondary)
            val layout = textMeasurer.measure(label, style)
            drawText(
                textMeasurer = textMeasurer,
                text = label,
                topLeft = Offset(
                    leftPad - layout.size.width - 4.dp.toPx(),
                    fretCenterY(minFret) - layout.size.height / 2f
                ),
                style = style
            )
        }

        // 横按：同一品 ≥3 个按弦点时画圆角矩形条
        val barreFrets = pressed.groupingBy { it }.eachCount()
            .filter { it.value >= 3 }.keys
        for (fret in barreFrets) {
            val cy = fretCenterY(fret)
            val barColor = Wood.copy(alpha = 0.85f)
            drawRoundRect(
                color = barColor,
                topLeft = Offset(stringX(0) - cellW * 0.42f, cy - cellH * 0.30f),
                size = androidx.compose.ui.geometry.Size(
                    width = 5 * cellW + cellW * 0.84f,
                    height = cellH * 0.60f
                ),
                cornerRadius = CornerRadius(cellH * 0.28f, cellH * 0.28f)
            )
        }

        val markerY = topPad / 2f
        for (i in 0 until 6) {
            val fret = strings.getOrNull(i) ?: -1
            val finger = fingers.getOrNull(i) ?: 0
            val x = stringX(i)
            when {
                fret == -1 -> {
                    // ×：砖红，两条 3f 斜线
                    val r = cellW * 0.24f
                    drawLine(
                        color = BrickRed,
                        start = Offset(x - r, markerY - r),
                        end = Offset(x + r, markerY + r),
                        strokeWidth = 3f
                    )
                    drawLine(
                        color = BrickRed,
                        start = Offset(x + r, markerY - r),
                        end = Offset(x - r, markerY + r),
                        strokeWidth = 3f
                    )
                }
                fret == 0 -> {
                    // ○：木色描边
                    drawCircle(
                        color = Wood,
                        radius = cellW * 0.22f,
                        center = Offset(x, markerY),
                        style = Stroke(width = 2f)
                    )
                }
                else -> {
                    val cy = fretCenterY(fret)
                    val isBarre = fret in barreFrets
                    if (!isBarre) {
                        drawCircle(
                            color = Wood,
                            radius = cellW * 0.32f,
                            center = Offset(x, cy)
                        )
                    }
                    if (finger > 0) {
                        val text = finger.toString()
                        val style = TextStyle(
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        val layout = textMeasurer.measure(text, style)
                        drawText(
                            textMeasurer = textMeasurer,
                            text = text,
                            topLeft = Offset(
                                x - layout.size.width / 2f,
                                cy - layout.size.height / 2f
                            ),
                            style = style
                        )
                    }
                }
            }
        }
    }
}
