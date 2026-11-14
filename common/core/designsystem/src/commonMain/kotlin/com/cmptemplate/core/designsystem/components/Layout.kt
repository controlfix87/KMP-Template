package com.cmptemplate.core.designsystem.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ButtonColors
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp

/**
 * Outlined buttons in the brand accent. Material 3's default outlined content colour is
 * onSurfaceVariant, which on the navy cards reads as a disabled button (screenshot review).
 */
@Composable
fun brandOutlinedButtonColors(): ButtonColors =
    ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.primary)

/** Readable width for text and cards on tablets, unfolded foldables and desktop windows. */
val ContentMaxWidth = 720.dp

/** Screen title with a large (48dp) back button; [onBack] null = top-level screen, no button. */
@Composable
fun ScreenHeader(title: String, onBack: (() -> Unit)?, backLabel: String, modifier: Modifier = Modifier) {
    Row(
        modifier.fillMaxWidth().widthIn(max = ContentMaxWidth),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        if (onBack != null) {
            FilledTonalIconButton(onClick = onBack, modifier = Modifier.size(48.dp).testTag("screen_back")) {
                Icon(AppIcons.Back, contentDescription = backLabel)
            }
        }
        Text(title, style = MaterialTheme.typography.headlineSmall, color = MaterialTheme.colorScheme.onBackground)
    }
}

/** A card section with consistent padding and the readable width. */
@Composable
fun SectionCard(modifier: Modifier = Modifier, content: @Composable ColumnScope.() -> Unit) {
    Card(
        modifier.fillMaxWidth().widthIn(max = ContentMaxWidth),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHigh),
    ) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp), content = content)
    }
}

/** Section title with an optional leading icon. */
@Composable
fun SectionLabel(text: String, icon: ImageVector? = null, modifier: Modifier = Modifier) {
    Row(modifier, verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        if (icon != null) Icon(icon, contentDescription = null, Modifier.size(20.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(text, style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onBackground)
    }
}

/**
 * A tappable choice card (radio semantics): big icon or flag on top, label below, optional detail
 * line. Selected gets the accent frame. Used for language and theme in onboarding and About.
 */
@Composable
fun OptionCard(
    selected: Boolean,
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    detail: String? = null,
    icon: @Composable () -> Unit,
) {
    val colors = MaterialTheme.colorScheme
    val shape = RoundedCornerShape(16.dp)
    Column(
        modifier
            .clip(shape)
            .background(if (selected) colors.primaryContainer else colors.surfaceContainer)
            .border(if (selected) 2.dp else 1.dp, if (selected) colors.primary else colors.outlineVariant, shape)
            .selectable(selected = selected, role = Role.RadioButton, onClick = onClick)
            .padding(vertical = 14.dp, horizontal = 6.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        Box(Modifier.height(34.dp), contentAlignment = Alignment.Center) { icon() }
        Text(
            label,
            style = MaterialTheme.typography.labelLarge,
            textAlign = TextAlign.Center,
            maxLines = 2,
            color = if (selected) colors.onPrimaryContainer else colors.onSurface,
        )
        if (detail != null) {
            Text(detail, style = MaterialTheme.typography.labelSmall, color = colors.onSurfaceVariant, maxLines = 1)
        }
    }
}

/**
 * Progress bar split per step, each segment with its title (LockdownControl). Upcoming steps are
 * visible; finished steps can be tapped to go back to them.
 */
@Composable
fun StepProgress(titles: List<String>, current: Int, onStep: (Int) -> Unit, modifier: Modifier = Modifier) {
    val colors = MaterialTheme.colorScheme
    Row(modifier.fillMaxWidth().testTag("step_progress"), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
        titles.forEachIndexed { i, title ->
            val reached = i <= current
            Column(
                Modifier.weight(1f)
                    .clip(RoundedCornerShape(6.dp))
                    .then(if (i < current) Modifier.clickable(role = Role.Button) { onStep(i) } else Modifier)
                    .padding(vertical = 4.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                Box(
                    Modifier.fillMaxWidth().height(4.dp)
                        .background(if (reached) colors.primary else colors.outlineVariant, RoundedCornerShape(2.dp)),
                )
                Text(
                    title,
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = if (i == current) FontWeight.Bold else FontWeight.Normal,
                    color = when {
                        i == current -> colors.primary
                        reached -> colors.onSurface
                        else -> colors.onSurfaceVariant
                    },
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
    }
}
