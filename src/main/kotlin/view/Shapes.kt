package view

import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.LayoutDirection
import kotlin.math.sqrt

class HalfCircleCutShape : Shape {
    override fun createOutline(
        size: Size,
        layoutDirection: LayoutDirection,
        density: Density,
    ): Outline {
        val path = Path().apply {
            val sqrt = sqrt(2f)
            arcTo(
                rect = Rect(
                    left = 0f,
                    top = -size.height,
                    right = 2 * sqrt * size.height,
                    bottom = size.height
                ),
                startAngleDegrees = 180f,
                sweepAngleDegrees = -90f,
                forceMoveTo = false
            )
            arcTo(
                rect = Rect(
                    left = size.width - 2 * sqrt * size.height,
                    top = -size.height,
                    right = size.width,
                    bottom = size.height
                ),
                startAngleDegrees = 90f,
                sweepAngleDegrees = -90f,
                forceMoveTo = false
            )
            close()
        }

        return Outline.Generic(path)
    }
}

class ArcCutShape : Shape {
    override fun createOutline(
        size: Size,
        layoutDirection: LayoutDirection,
        density: Density,
    ): Outline {
        val path = Path().apply {
            val arcHeight = size.height / 2 // 调整这个值来改变弧线的高度

            // 开始绘制路径
            moveTo(0f, arcHeight) // 起点

            // 绘制左侧的弧线
            quadraticBezierTo(0f, 0f, arcHeight, 0f)

            // 绘制顶部的直线
            lineTo(size.width - arcHeight, 0f)

            // 绘制右侧的弧线
            quadraticBezierTo(size.width, 0f, size.width, arcHeight)

            // 绘制底部的直线，完成闭合路径
            lineTo(0f, arcHeight)
        }

        return Outline.Generic(path)
    }
}