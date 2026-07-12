package com.wakerep.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.wakerep.app.model.ACHIEVEMENTS
import com.wakerep.app.model.Achievement
import com.wakerep.app.model.AchievementId
import com.wakerep.app.model.CompletionStats
import com.wakerep.app.model.isAchievementUnlocked
import com.wakerep.app.ui.StatsViewModel
import com.wakerep.app.ui.icons.WakerepIcons
import com.wakerep.app.ui.theme.WakerepColors
import com.wakerep.app.ui.theme.WakerepRadius
import com.wakerep.app.ui.theme.WakerepSpacing
import com.wakerep.app.ui.theme.WakerepType

/**
 * Achievements (design-system reference doc, section 07 "Medallions that
 * light up as you rise"): locked medallions sit cold and unlit, unlocked
 * ones glow amber - the distinction reads by luminance, not just an icon
 * swap, so it survives grayscale. Names are defiant, not cutesy.
 */
@Composable
fun AchievementsScreen(viewModel: StatsViewModel, onBack: () -> Unit) {
    val records by viewModel.records.collectAsStateWithLifecycle()
    val stats by viewModel.stats.collectAsStateWithLifecycle()
    val unlocked = ACHIEVEMENTS.filter { isAchievementUnlocked(it.id, records, stats) }.map { it.id }.toSet()
    val nextUp = ACHIEVEMENTS.filterNot { it.id in unlocked }
        .maxByOrNull { achievementProgress(it.id, stats) }

    Scaffold(
        containerColor = WakerepColors.InkCanvas,
        topBar = {
            TopAppBar(
                title = { Text("Trophies", style = WakerepType.H2, color = WakerepColors.TextHigh) },
                navigationIcon = {
                    androidx.compose.material3.TextButton(onClick = onBack) {
                        Text("Back", style = WakerepType.Body, color = WakerepColors.TextMid)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = WakerepColors.InkCanvas),
            )
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding).fillMaxSize().padding(horizontal = WakerepSpacing.xl)) {
            Text(
                "${unlocked.size} of ${ACHIEVEMENTS.size} earned",
                style = WakerepType.Body,
                color = WakerepColors.TextMid,
                modifier = Modifier.padding(top = WakerepSpacing.md, bottom = WakerepSpacing.lg),
            )

            LazyVerticalGrid(
                columns = GridCells.Fixed(3),
                horizontalArrangement = Arrangement.spacedBy(WakerepSpacing.md),
                verticalArrangement = Arrangement.spacedBy(WakerepSpacing.md),
                modifier = Modifier.weight(1f),
            ) {
                items(ACHIEVEMENTS) { achievement ->
                    MedallionCard(achievement = achievement, isUnlocked = achievement.id in unlocked)
                }
            }

            nextUp?.let { achievement ->
                val progress = achievementProgress(achievement.id, stats)
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = WakerepSpacing.lg)
                        .clip(RoundedCornerShape(WakerepRadius.card))
                        .background(WakerepColors.Surface)
                        .padding(WakerepSpacing.lg),
                ) {
                    Text(
                        "NEXT UP · ${(progress * 100).toInt()}%",
                        style = WakerepType.Mono,
                        color = WakerepColors.Amber,
                    )
                    Text(
                        "${achievement.title} — ${achievement.description}",
                        style = WakerepType.BodyEmphasis,
                        color = WakerepColors.TextHigh,
                        modifier = Modifier.padding(top = WakerepSpacing.xs),
                    )
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = WakerepSpacing.md)
                            .height(6.dp)
                            .clip(RoundedCornerShape(WakerepRadius.pill))
                            .background(WakerepColors.Hairline),
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxHeight()
                                .fillMaxWidth(progress.coerceIn(0.02f, 1f))
                                .clip(RoundedCornerShape(WakerepRadius.pill))
                                .background(WakerepColors.DawnGradient)
                        ) {}
                    }
                }
            }
        }
    }
}

@Composable
private fun MedallionCard(achievement: Achievement, isUnlocked: Boolean) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(0.85f)
            .clip(RoundedCornerShape(WakerepRadius.card))
            .background(if (isUnlocked) WakerepColors.SurfaceHigh else WakerepColors.InkSunk)
            .padding(WakerepSpacing.md),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        WakerepIcons.Achievement(
            size = 32.dp,
            tint = if (isUnlocked) WakerepColors.Amber else WakerepColors.TextLow,
            filled = isUnlocked,
        )
        Text(
            achievement.title,
            style = WakerepType.Label,
            color = if (isUnlocked) WakerepColors.TextHigh else WakerepColors.TextLow,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(top = WakerepSpacing.sm),
        )
    }
}

private fun achievementProgress(id: AchievementId, stats: CompletionStats): Float = when (id) {
    AchievementId.FIRST_LIGHT -> if (stats.totalMorningsWon >= 1) 1f else 0f
    AchievementId.DAWN_PATROL -> (stats.bestStreak / 7f).coerceIn(0f, 1f)
    AchievementId.SUNUP_30 -> (stats.totalMorningsWon / 30f).coerceIn(0f, 1f)
    AchievementId.CENTURY -> (stats.totalMorningsWon / 100f).coerceIn(0f, 1f)
    AchievementId.IRON_DAWN -> (stats.bestStreak / 30f).coerceIn(0f, 1f)
    AchievementId.NO_MERCY -> if (stats.snoozeFreeCurrentStreak) (stats.currentStreak / 7f).coerceIn(0f, 1f) else 0f
}
