package view

import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.DrawStyle
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.painter.Painter
import kotlin.math.min
import kotlin.math.sqrt

class CustomIconPainter(private val color: Color) : Painter() {
    override val intrinsicSize = Size(Float.MAX_VALUE, Float.MAX_VALUE)

    override fun DrawScope.onDraw() {

        // 绘制图标的样式
        val style: DrawStyle = Stroke(width = 2f, cap = StrokeCap.Round)

        // 设置绘制的颜色
        val color: Color = color

        // 在这里使用 drawXXX() 方法绘制自定义的图标形状
        val path = Path().apply {
            // 绘制圆形
            val centerX = size.minDimension / 2f
            val centerY = size.minDimension / 2f
            val radius = min(centerX, centerY) * 0.9f// 圆形半径为宽高中较小值的一半
            val ovalRect = Rect(centerX - radius, centerY - radius, centerX + radius, centerY + radius)
            val startAngle = 0f
            val sweepAngle = 360f
            addArc(ovalRect, startAngle, sweepAngle)

            // 绘制正三角形
            val triangleSize = min(centerX, centerY) * 2f// 正三角形边长为圆形半径的一半
            val triangleHeight = triangleSize * sqrt(3f) / 2f // 正三角形的高度
            val triangleTop = centerY - triangleHeight / 2f // 正三角形的顶部位置
            val triangleBottom = centerY + triangleHeight / 2f // 正三角形的底部位置
            val triangleLeft = centerX - triangleSize / 2f // 正三角形的左边位置
            val triangleRight = centerX + triangleSize / 2f // 正三角形的右边位置
            moveTo(centerX, triangleTop) // 移动到正三角形的顶部
            lineTo(triangleRight, triangleBottom) // 绘制正三角形的右边线
            lineTo(triangleLeft, triangleBottom) // 绘制正三角形的底边线
            close() // 封闭路径
        }


        // 使用 drawPath() 方法绘制图标形状
        drawPath(path = path, color = color, style = style)
    }
}
