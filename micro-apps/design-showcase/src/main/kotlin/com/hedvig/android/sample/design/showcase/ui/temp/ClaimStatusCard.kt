package com.hedvig.android.sample.design.showcase.ui.temp

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.contentColorFor
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.hedvig.android.core.designsystem.component.card.HedvigCard
import com.hedvig.android.core.designsystem.material3.DisabledAlpha
import com.hedvig.android.core.designsystem.material3.infoContainer
import com.hedvig.android.core.designsystem.material3.infoElement
import com.hedvig.android.core.designsystem.material3.onInfoContainer
import com.hedvig.android.core.designsystem.material3.onWarningContainer
import com.hedvig.android.core.designsystem.material3.squircleExtraSmall
import com.hedvig.android.core.designsystem.material3.warningContainer
import com.hedvig.android.core.designsystem.material3.warningElement

@Composable
fun ClaimStatusCard(
  uiState: com.hedvig.android.sample.design.showcase.ui.temp.ClaimStatusCardUiState,
  goToDetailScreen: ((claimId: String) -> Unit)?,
  modifier: Modifier = Modifier,
) {
  val onClick: (() -> Unit)? = if (goToDetailScreen != null) {
    { goToDetailScreen.invoke(uiState.id) }
  } else {
    null
  }
  HedvigCard(
    onClick = onClick,
    modifier = modifier,
  ) {
    Column {
      TopInfo(
        pillsUiState = uiState.pillsUiState,
        title = uiState.title,
        subtitle = uiState.subtitle,
        modifier = Modifier.padding(16.dp),
      )
      HorizontalDivider()
      ClaimProgressRow(
        claimProgressItemsUiState = uiState.claimProgressItemsUiState,
        modifier = Modifier.padding(16.dp),
      )
    }
  }
}

@Composable
internal fun TopInfo(pillsUiState: List<PillUiState>, title: String, subtitle: String, modifier: Modifier = Modifier) {
  Column(modifier = modifier) {
    ClaimPillsAndForwardArrow(pillsUiState)
    Spacer(modifier = Modifier.height(16.dp))
    Text(text = title, style = MaterialTheme.typography.bodyLarge)
    Text(
      text = subtitle,
      style = MaterialTheme.typography.bodyMedium.copy(color = MaterialTheme.colorScheme.onSurfaceVariant),
    )
    Spacer(modifier = Modifier.height(4.dp))
  }
}

@Composable
internal fun ClaimProgressRow(claimProgressItemsUiState: List<ClaimProgressUiState>, modifier: Modifier = Modifier) {
  Row(
    modifier = modifier,
    horizontalArrangement = Arrangement.spacedBy(6.dp),
  ) {
    claimProgressItemsUiState.forEach { claimProgressUiState: ClaimProgressUiState ->
      ClaimProgress(
        text = claimProgressUiState.text,
        type = claimProgressUiState.type,
        modifier = Modifier.weight(1f),
      )
    }
  }
}

@Composable
private fun ClaimProgress(text: String, type: ClaimProgressUiState.ClaimProgressType, modifier: Modifier = Modifier) {
  val color = when (type) {
    ClaimProgressUiState.ClaimProgressType.PAID -> MaterialTheme.colorScheme.infoElement
    ClaimProgressUiState.ClaimProgressType.REOPENED -> MaterialTheme.colorScheme.warningElement
    ClaimProgressUiState.ClaimProgressType.UNKNOWN,
    ClaimProgressUiState.ClaimProgressType.PAST_INACTIVE,
    ClaimProgressUiState.ClaimProgressType.CURRENTLY_ACTIVE,
    ClaimProgressUiState.ClaimProgressType.FUTURE_INACTIVE,
    -> MaterialTheme.colorScheme.primary
  }
  val contentAlpha = when (type) {
    ClaimProgressUiState.ClaimProgressType.PAST_INACTIVE -> ContentAlpha.MEDIUM
    ClaimProgressUiState.ClaimProgressType.CURRENTLY_ACTIVE -> ContentAlpha.HIGH
    ClaimProgressUiState.ClaimProgressType.FUTURE_INACTIVE -> ContentAlpha.DISABLED
    ClaimProgressUiState.ClaimProgressType.PAID -> ContentAlpha.HIGH
    ClaimProgressUiState.ClaimProgressType.REOPENED -> ContentAlpha.HIGH
    ClaimProgressUiState.ClaimProgressType.UNKNOWN -> ContentAlpha.HIGH
  }
  ClaimProgress(
    text = text,
    color = color,
    contentAlpha = contentAlpha,
    modifier = modifier,
  )
}

@Composable
private fun ClaimProgress(
  text: String,
  color: Color,
  modifier: Modifier = Modifier,
  contentAlpha: ContentAlpha = ContentAlpha.HIGH,
) {
  CompositionLocalProvider(LocalContentColor provides LocalContentColor.current.copy(alpha = contentAlpha.value)) {
    Column(modifier = modifier) {
      val progressColor = color.copy(alpha = contentAlpha.value)
      Canvas(
        modifier = Modifier
          .fillMaxWidth()
          .height(4.dp)
          .clip(CircleShape),
      ) {
        drawRect(progressColor)
      }
      Spacer(modifier = Modifier.height(6.dp))
      Text(
        text = text,
        textAlign = TextAlign.Center,
        style = MaterialTheme.typography.bodySmall,
        modifier = Modifier.fillMaxWidth(),
      )
    }
  }
}

private enum class ContentAlpha {
  HIGH,
  MEDIUM,
  DISABLED,
  ;

  val value: Float
    @Composable
    get() = when (this) {
      HIGH -> 1f
      MEDIUM -> 0.74f
      DISABLED -> DisabledAlpha
    }
}

@Composable
internal fun ClaimPillsAndForwardArrow(pillsUiState: List<PillUiState>, modifier: Modifier = Modifier) {
  Row(
    horizontalArrangement = Arrangement.spacedBy(8.dp),
    modifier = modifier.fillMaxWidth(),
  ) {
    pillsUiState.forEach { pillUiState: PillUiState ->
      ClaimPill(
        text = pillUiState.text,
        pillType = pillUiState.type,
      )
    }
  }
}

@Composable
private fun ClaimPill(text: String, pillType: PillUiState.PillType) {
  when (pillType) {
    PillUiState.PillType.OPEN -> Pill(text, MaterialTheme.colorScheme.outlineVariant)
    PillUiState.PillType.CLOSED -> Pill(text, MaterialTheme.colorScheme.primary)
    PillUiState.PillType.REOPENED -> Pill(
      text,
      MaterialTheme.colorScheme.warningContainer,
      MaterialTheme.colorScheme.onWarningContainer,
    )
    PillUiState.PillType.PAYMENT -> Pill(
      text,
      MaterialTheme.colorScheme.infoContainer,
      MaterialTheme.colorScheme.onInfoContainer,
    )
    PillUiState.PillType.UNKNOWN -> Pill(text, MaterialTheme.colorScheme.surface)
  }
}

@Composable
internal fun Pill(text: String, color: Color, contentColor: Color = contentColorFor(color)) {
  Surface(
    shape = MaterialTheme.shapes.squircleExtraSmall,
    color = color,
    contentColor = contentColor,
    modifier = Modifier.heightIn(min = 24.dp),
  ) {
    Row(
      Modifier.padding(
        horizontal = 10.dp,
        vertical = 4.dp,
      ),
      horizontalArrangement = Arrangement.Center,
      verticalAlignment = Alignment.CenterVertically,
    ) {
      Text(
        text = text,
        style = MaterialTheme.typography.bodyMedium,
        maxLines = 1,
      )
    }
  }
}

data class ClaimStatusCardUiState(
  val id: String,
  val pillsUiState: List<PillUiState>,
  val title: String,
  val subtitle: String,
  val claimProgressItemsUiState: List<ClaimProgressUiState>,
)

data class PillUiState(
  val text: String,
  val type: PillType,
) {
  enum class PillType {
    OPEN,
    CLOSED,
    REOPENED,
    PAYMENT,
    UNKNOWN, // Default type to not break clients on breaking API changes. Should default to how OPEN is rendered
  }
}

data class ClaimProgressUiState(
  val text: String,
  val type: ClaimProgressType,
) {
  enum class ClaimProgressType {
    PAST_INACTIVE,
    CURRENTLY_ACTIVE,
    FUTURE_INACTIVE,
    PAID,
    REOPENED,
    UNKNOWN,
  }
}
