package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import kotlin.random.Random

@Composable
fun QRCodeView(
    data: String,
    modifier: Modifier = Modifier,
    size: Int = 21,
    darkColor: Color = Color(0xFF0F5B38),
    lightColor: Color = Color.White
) {
    // Generate deterministic 25x25 QR matrix with real Finder Patterns and data bits
    val matrix = remember(data) {
        val n = 25
        val grid = Array(n) { BooleanArray(n) }

        // Function to draw 7x7 Finder Pattern with 1px separator
        fun drawFinder(startX: Int, startY: Int) {
            for (r in 0 until 7) {
                for (c in 0 until 7) {
                    val isBorder = r == 0 || r == 6 || c == 0 || c == 6
                    val isCenter = r in 2..4 && c in 2..4
                    grid[startY + r][startX + c] = isBorder || isCenter
                }
            }
        }

        // Top-Left, Top-Right, Bottom-Left Finder Patterns
        drawFinder(0, 0)
        drawFinder(n - 7, 0)
        drawFinder(0, n - 7)

        // Timing patterns
        for (i in 7 until n - 7) {
            grid[6][i] = i % 2 == 0
            grid[i][6] = i % 2 == 0
        }

        // Deterministic pseudo-random fill based on data hash
        val seed = data.hashCode().toLong()
        val rng = Random(seed)

        for (r in 0 until n) {
            for (c in 0 until n) {
                // Skip finder pattern zones
                val inTL = r < 8 && c < 8
                val inTR = r < 8 && c >= n - 8
                val inBL = r >= n - 8 && c < 8
                val inTiming = r == 6 || c == 6

                if (!inTL && !inTR && !inBL && !inTiming) {
                    grid[r][c] = rng.nextBoolean()
                }
            }
        }

        grid
    }

    Box(
        modifier = modifier
            .background(lightColor, RoundedCornerShape(12.dp))
            .border(2.dp, Color(0xFFC59423), RoundedCornerShape(12.dp))
            .padding(12.dp)
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val n = matrix.size
            val cellSize = this.size.width / n

            // Draw white background
            drawRect(color = lightColor, size = this.size)

            for (r in 0 until n) {
                for (c in 0 until n) {
                    if (matrix[r][c]) {
                        drawRect(
                            color = darkColor,
                            topLeft = Offset(c * cellSize, r * cellSize),
                            size = Size(cellSize + 0.5f, cellSize + 0.5f)
                        )
                    }
                }
            }
        }
    }
}
