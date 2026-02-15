package com.tidewatch.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tidewatch.data.models.TideHeight
import com.tidewatch.ui.theme.Primary
import com.tidewatch.utils.formatHeight
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import kotlin.math.floor

/**
 * Displays a tide curve graph.
 *
 * Shows tide heights over time as a line graph.
 *
 * @param tideData List of tide heights to plot
 * @param modifier Modifier for the graph
 * @param lineColor Color of the tide curve line
 * @param useMetric Whether to display heights in meters (true) or feet (false)
 */
@Composable
fun TideGraph(
    tideData: List<TideHeight>,
    modifier: Modifier = Modifier,
    lineColor: Color = Primary,
    useMetric: Boolean = false
) {
    if (tideData.isEmpty()) return

    Canvas(
        modifier = modifier
            .fillMaxWidth()
            .height(100.dp) // Increased height to accommodate labels below
    ) {
        val width = size.width
        val height = size.height

        val labelHeight = 20.dp.toPx()
        val horizontalMargin = 20.dp.toPx()
        val graphHeight = height - labelHeight
        val graphWidth = width - (horizontalMargin * 2)

        val minHeight = tideData.minOf { it.height }
        val maxHeight = tideData.maxOf { it.height }
        val heightRange = maxHeight - minHeight

        if (heightRange == 0.0) return@Canvas

        val startTime = tideData.first().time.epochSecond
        val endTime = tideData.last().time.epochSecond
        val timeRange = (endTime - startTime).toDouble()

        if (timeRange == 0.0) return@Canvas

        val points = tideData.map { tide ->
            val timeOffset = (tide.time.epochSecond - startTime).toDouble()
            val x = horizontalMargin + ((timeOffset / timeRange) * graphWidth).toFloat()

            val normalizedHeight = ((tide.height - minHeight) / heightRange).toFloat()
            val y = graphHeight - (normalizedHeight * graphHeight)

            Offset(x, y)
        }

        if (points.size >= 2) {
            val path = Path().apply {
                moveTo(points[0].x, points[0].y)
                for (i in 1 until points.size) {
                    lineTo(points[i].x, points[i].y)
                }
            }

            drawPath(
                path = path,
                color = lineColor,
                style = Stroke(width = 2.dp.toPx())
            )
        }

        // MLLW (Mean Lower Low Water) baseline
        val zeroY = graphHeight - ((0.0 - minHeight) / heightRange).toFloat() * graphHeight
        if (zeroY in 0f..graphHeight) {
            drawLine(
                color = Color.Gray.copy(alpha = 0.5f),
                start = Offset(horizontalMargin, zeroY),
                end = Offset(horizontalMargin + graphWidth, zeroY),
                strokeWidth = 1.dp.toPx()
            )
        }

        val startInstant = tideData.first().time
        val endInstant = tideData.last().time
        val zoneId = ZoneId.systemDefault()
        val timeFormatter = DateTimeFormatter.ofPattern("ha").withZone(zoneId)

        val startHour = startInstant.atZone(zoneId).hour
        val firstTickHour = ((startHour / 6) * 6)
        var tickTime = startInstant.atZone(zoneId)
            .withHour(firstTickHour)
            .withMinute(0)
            .withSecond(0)
            .withNano(0)
            .toInstant()

        if (tickTime.isBefore(startInstant)) {
            tickTime = tickTime.plusSeconds(6 * 3600)
        }

        while (tickTime.isBefore(endInstant) || tickTime == endInstant) {
            val timeOffset = (tickTime.epochSecond - startTime).toDouble()
            val x = horizontalMargin + ((timeOffset / timeRange) * graphWidth).toFloat()

            drawLine(
                color = Color.Gray.copy(alpha = 0.7f),
                start = Offset(x, graphHeight),
                end = Offset(x, graphHeight + 6.dp.toPx()),
                strokeWidth = 1.dp.toPx()
            )

            val label = timeFormatter.format(tickTime).lowercase()
            drawContext.canvas.nativeCanvas.apply {
                val paint = android.graphics.Paint().apply {
                    color = Color.Gray.copy(alpha = 0.8f).toArgb()
                    textSize = 10.dp.toPx()
                    textAlign = android.graphics.Paint.Align.CENTER
                }
                drawText(label, x, graphHeight + 16.dp.toPx(), paint)
            }

            tickTime = tickTime.plusSeconds(6 * 3600)
        }

        val tickInterval = calculateHeightTickInterval(heightRange)
        val firstTick = (floor(minHeight / tickInterval) * tickInterval)

        val tickValues = mutableListOf<Double>()
        var currentTick = firstTick
        while (currentTick <= maxHeight) {
            tickValues.add(currentTick)
            currentTick += tickInterval
        }

        tickValues.forEachIndexed { index, heightValue ->
            val normalizedHeight = ((heightValue - minHeight) / heightRange).toFloat()
            val y = graphHeight - (normalizedHeight * graphHeight)

            val isLeftSide = index % 2 == 0

            if (isLeftSide) {
                drawLine(
                    color = Color.Gray.copy(alpha = 0.7f),
                    start = Offset(horizontalMargin - 6.dp.toPx(), y),
                    end = Offset(horizontalMargin, y),
                    strokeWidth = 1.dp.toPx()
                )

                val heightText = formatHeight(heightValue, useMetric)
                drawContext.canvas.nativeCanvas.apply {
                    val leftPaint = android.graphics.Paint().apply {
                        color = android.graphics.Color.GRAY
                        textSize = 10.sp.toPx()
                        textAlign = android.graphics.Paint.Align.RIGHT
                        isAntiAlias = true
                    }
                    drawText(
                        heightText,
                        horizontalMargin - 8.dp.toPx(),
                        y + (leftPaint.textSize / 3),
                        leftPaint
                    )
                }
            } else {
                val rightEdge = horizontalMargin + graphWidth
                drawLine(
                    color = Color.Gray.copy(alpha = 0.7f),
                    start = Offset(rightEdge, y),
                    end = Offset(rightEdge + 6.dp.toPx(), y),
                    strokeWidth = 1.dp.toPx()
                )

                val heightText = formatHeight(heightValue, useMetric)
                drawContext.canvas.nativeCanvas.apply {
                    val rightPaint = android.graphics.Paint().apply {
                        color = android.graphics.Color.GRAY
                        textSize = 10.sp.toPx()
                        textAlign = android.graphics.Paint.Align.LEFT
                        isAntiAlias = true
                    }
                    drawText(
                        heightText,
                        rightEdge + 8.dp.toPx(),
                        y + (rightPaint.textSize / 3),
                        rightPaint
                    )
                }
            }
        }
    }
}

/**
 * Calculate appropriate height tick interval based on tide range.
 *
 * @param heightRange Total height range (maxHeight - minHeight) in feet
 * @return Tick interval in feet
 */
private fun calculateHeightTickInterval(heightRange: Double): Double {
    return when {
        heightRange < 2.0 -> 0.5  // Small range: ticks every 0.5 ft
        heightRange < 5.0 -> 1.0  // Medium range: ticks every 1 ft
        heightRange < 10.0 -> 2.0 // Large range: ticks every 2 ft
        else -> 5.0               // Very large: ticks every 5 ft
    }
}
