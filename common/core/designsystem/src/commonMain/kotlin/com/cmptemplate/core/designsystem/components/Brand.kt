package com.cmptemplate.core.designsystem.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.cmptemplate.core.designsystem.BrandColors
import com.cmptemplate.core.designsystem.exo2FontFamily

/**
 * Placeholder app mark in the ControlFix style: navy tile, lime arc, teal dot. Drawn in code so it
 * renders the same on every target. Replace it with the product's approved artwork (keep the mark
 * inside the launcher safe zone: about 66% of the tile).
 */
@Composable
fun AppMark(modifier: Modifier = Modifier, size: Dp = 72.dp, contentDescription: String? = null) {
    Canvas(
        modifier.size(size).then(
            if (contentDescription != null) Modifier.semantics { this.contentDescription = contentDescription } else Modifier,
        ),
    ) {
        val s = this.size.minDimension
        drawRoundRect(
            brush = Brush.linearGradient(listOf(BrandColors.Navy, BrandColors.Ink)),
            cornerRadius = CornerRadius(s * 0.24f),
        )
        val stroke = s * 0.11f
        val inset = s * 0.25f
        drawArc(
            color = BrandColors.Lime,
            startAngle = 40f,
            sweepAngle = 280f,
            useCenter = false,
            topLeft = Offset(inset, inset),
            size = androidx.compose.ui.geometry.Size(s - 2 * inset, s - 2 * inset),
            style = Stroke(width = stroke, cap = StrokeCap.Round),
        )
        drawCircle(BrandColors.Mint, radius = stroke * 0.62f, center = Offset(s * 0.70f, s * 0.50f))
    }
}

/**
 * The product name in Exo 2 ExtraBold, with [highlight] (one part of the name, e.g. "Control" in
 * GameController) in the brand accent: lime on dark, the readable deep lime on light.
 */
@Composable
fun Wordmark(
    name: String,
    modifier: Modifier = Modifier,
    highlight: String? = null,
    style: TextStyle = MaterialTheme.typography.headlineSmall,
) {
    val accent = MaterialTheme.colorScheme.primary
    val text = buildAnnotatedString {
        val at = highlight?.takeIf { it.isNotEmpty() }?.let { name.indexOf(it) } ?: -1
        if (at < 0) {
            append(name)
        } else {
            append(name.substring(0, at))
            withStyle(SpanStyle(color = accent)) { append(highlight!!) }
            append(name.substring(at + highlight!!.length))
        }
    }
    Text(
        text,
        modifier,
        style = style.copy(fontFamily = exo2FontFamily(), fontWeight = FontWeight.ExtraBold),
        color = MaterialTheme.colorScheme.onBackground,
    )
}
