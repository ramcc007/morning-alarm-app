package com.wakerep.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.wakerep.app.ui.StatsViewModel
import com.wakerep.app.ui.components.HorizonArc
import com.wakerep.app.ui.theme.WakerepColors
import com.wakerep.app.ui.theme.WakerepRadius
import com.wakerep.app.ui.theme.WakerepSpacing
import com.wakerep.app.ui.theme.WakerepType
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.TextStyle
import java.util.Locale

/**
 * Streaks & Stats (design-system reference doc, section 07 "A calendar of
 * sunrises you earned"): not a green GitHub grid - every day is a sun, fully
 * lit gold for a morning won, a dim ember for one missed, empty for days
 * with nothing logged yet. Status reads by brightness, colorblind-safe.
 */
@Composable
fun StreaksScreen(
    viewModel: StatsViewModel,
    onOpenAchievements: () -> Unit,
) {
    val stats by viewModel.stats.collectAsStateWithLifecycle()
    val today = LocalDate.now()
    val yearMonth = YearMonth.from(today)

    Scaffold(
        containerColor = WakerepColors.InkCanvas,
        topBar = {
            TopAppBar(
                title = { Text("Your mornings", style = WakerepType.H2, color = WakerepColors.TextHigh) },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = WakerepColors.InkCanvas),
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .fillMaxSize()
                .padding(WakerepSpacing.xl),
        ) {
            if (stats.totalMorningsWon == 0) {
                EmptyStreaksState()
            } else {
                Row(verticalAlignment = Alignment.Bottom) {
                    Text(
                        "${stats.currentStreak}",
                        style = WakerepType.DisplayXl,
                        color = WakerepColors.TextHigh,
                    )
                    Text(
                        "DAY STREAK",
                        style = WakerepType.Mono,
                        color = WakerepColors.Amber,
                        modifier = Modifier.padding(start = WakerepSpacing.sm, bottom = WakerepSpacing.md),
                    )
                }

                Text(
                    yearMonth.month.getDisplayName(TextStyle.FULL, Locale.getDefault()).uppercase(),
                    style = WakerepType.Label,
                    color = WakerepColors.TextMid,
                    modifier = Modifier.padding(top = WakerepSpacing.lg, bottom = WakerepSpacing.md),
                )

                SunCalendar(yearMonth = yearMonth, wonDates = stats.wonDates, today = today)

                Row(
                    modifier = Modifier.fillMaxWidth().padding(top = WakerepSpacing.xl),
                    horizontalArrangement = Arrangement.spacedBy(WakerepSpacing.md),
                ) {
                    StatTile("${stats.totalMorningsWon}", "mornings won", Modifier.weight(1f))
                    StatTile("${stats.totalReps}", "total reps", Modifier.weight(1f))
                    StatTile("${stats.bestStreak}", "best streak", Modifier.weight(1f))
                }
            }

            Text(
                "Achievements",
                style = WakerepType.BodyEmphasis,
                color = WakerepColors.TextHigh,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = WakerepSpacing.xxl)
                    .clip(RoundedCornerShape(WakerepRadius.card))
                    .background(WakerepColors.Surface)
                    .clickable(onClick = onOpenAchievements)
                    .padding(WakerepSpacing.lg),
            )
        }
    }
}

@Composable
private fun EmptyStreaksState() {
    Column(
        modifier = Modifier.fillMaxWidth().padding(vertical = WakerepSpacing.xxxl),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        HorizonArc(progress = 0f, diameter = 56.dp, strokeWidth = 3.dp, lit = false)
        Text(
            "Win your first morning to light this up.",
            style = WakerepType.Body,
            color = WakerepColors.TextMid,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(top = WakerepSpacing.lg),
        )
    }
}

@Composable
private fun SunCalendar(yearMonth: YearMonth, wonDates: Set<LocalDate>, today: LocalDate) {
    val firstOfMonth = yearMonth.atDay(1)
    val leadingBlanks = firstOfMonth.dayOfWeek.value % 7 // Sunday-first offset
    val daysInMonth = yearMonth.lengthOfMonth()
    val cells: List<LocalDate?> = List(leadingBlanks) { null } + (1..daysInMonth).map { yearMonth.atDay(it) }
    val paddedCells = cells + List((7 - cells.size % 7) % 7) { null }

    Column {
        Row(modifier = Modifier.fillMaxWidth()) {
            listOf("S", "M", "T", "W", "T", "F", "S").forEach { label ->
                Text(
                    label,
                    style = WakerepType.Mono,
                    color = WakerepColors.TextLow,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.weight(1f),
                )
            }
        }
        paddedCells.chunked(7).forEach { week ->
            Row(modifier = Modifier.fillMaxWidth().padding(top = WakerepSpacing.sm)) {
                week.forEach { date ->
                    Box(modifier = Modifier.weight(1f).aspectRatio(1f), contentAlignment = Alignment.Center) {
                        if (date != null) DaySun(date = date, isWon = wonDates.contains(date), isToday = date == today, isPast = date.isBefore(today))
                    }
                }
            }
        }
    }
}

@Composable
private fun DaySun(date: LocalDate, isWon: Boolean, isToday: Boolean, isPast: Boolean) {
    val fillColor = when {
        isWon -> WakerepColors.Amber
        isPast -> WakerepColors.TextHigh.copy(alpha = 0.10f)
        else -> Color.Transparent
    }
    Box(
        modifier = Modifier
            .size(26.dp)
            .clip(CircleShape)
            .background(fillColor)
            .then(
                if (isToday) Modifier.border(1.5.dp, WakerepColors.Amber, CircleShape)
                else if (!isWon) Modifier.border(1.dp, WakerepColors.Hairline, CircleShape)
                else Modifier
            ),
    )
}

@Composable
private fun StatTile(value: String, label: String, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(WakerepRadius.card))
            .background(WakerepColors.Surface)
            .padding(vertical = WakerepSpacing.lg, horizontal = WakerepSpacing.sm),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(value, style = WakerepType.H1, color = WakerepColors.TextHigh)
        Text(label, style = WakerepType.Mono, color = WakerepColors.TextMid, textAlign = TextAlign.Center)
    }
}
