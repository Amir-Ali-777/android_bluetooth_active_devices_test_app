package com.example.bluetoothtestapp.ui.components

import android.graphics.BlurMaskFilter
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.graphics.PaintingStyle
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.unit.Dp

/**
 * Soft colored light around a rounded shape. Uses BlurMaskFilter because Modifier.blur is
 * ignored below Android 12 and this app must look the same on Android 11.
 * With [edgeWidth], only an outline of that width is lit (inside and outside the edge);
 * without it, the whole shape glows. A smaller [edgeWidth] and [blurRadius] give a thinner glow.
 */
fun Modifier.glow(
    brush: Brush,
    cornerRadius: Dp,
    blurRadius: Dp,
    edgeWidth: Dp? = null
): Modifier = drawWithCache {
    val radius = cornerRadius.toPx()
    val paint = Paint().apply {
        brush.applyTo(size, this, 1f)
        if (edgeWidth != null) {
            style = PaintingStyle.Stroke
            strokeWidth = edgeWidth.toPx()
        }
        asFrameworkPaint().maskFilter = BlurMaskFilter(blurRadius.toPx(), BlurMaskFilter.Blur.NORMAL)
    }
    onDrawBehind {
        drawIntoCanvas { canvas ->
            canvas.drawRoundRect(0f, 0f, size.width, size.height, radius, radius, paint)
        }
    }
}
