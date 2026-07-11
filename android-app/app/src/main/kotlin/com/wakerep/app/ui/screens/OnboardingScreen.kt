package com.wakerep.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.wakerep.app.ui.theme.WakerepColors
import kotlinx.coroutines.launch

private data class OnboardingPage(
    val icon: androidx.compose.ui.graphics.vector.ImageVector,
    val title: String,
    val subtitle: String,
)

private val pages = listOf(
    OnboardingPage(
        Icons.Filled.NotificationsActive,
        "The alarm you can't sleep through",
        "No snooze spiral. No shutting it off half-asleep. It rings until you move.",
    ),
    OnboardingPage(
        Icons.Filled.FitnessCenter,
        "Prove it with push-ups",
        "Point your front camera at yourself and knock out your set. We count every rep automatically.",
    ),
    OnboardingPage(
        Icons.Filled.WbSunny,
        "Start your day already winning",
        "By the time the alarm stops, you've already done your first workout of the day.",
    ),
)

@Composable
fun OnboardingScreen(onFinished: () -> Unit) {
    val pagerState = rememberPagerState(pageCount = { pages.size })
    val scope = rememberCoroutineScope()

    Column(modifier = Modifier.fillMaxSize().background(WakerepColors.InkCanvas)) {
        HorizontalPager(state = pagerState, modifier = Modifier.weight(1f)) { index ->
            val page = pages[index]
            Column(
                modifier = Modifier.fillMaxSize().padding(32.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
            ) {
                Icon(page.icon, contentDescription = null, tint = WakerepColors.Coral, modifier = Modifier.padding(bottom = 24.dp))
                Text(
                    page.title,
                    color = WakerepColors.TextHigh,
                    fontWeight = FontWeight.Bold,
                    fontSize = 26.sp,
                    textAlign = TextAlign.Center,
                )
                Text(
                    page.subtitle,
                    color = WakerepColors.TextMid,
                    fontSize = 15.sp,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(top = 16.dp),
                )
            }
        }

        Button(
            onClick = {
                if (pagerState.currentPage < pages.size - 1) {
                    scope.launch { pagerState.animateScrollToPage(pagerState.currentPage + 1) }
                } else {
                    onFinished()
                }
            },
            modifier = Modifier.fillMaxWidth().padding(horizontal = 32.dp).padding(bottom = 40.dp),
            colors = ButtonDefaults.buttonColors(containerColor = WakerepColors.Coral),
        ) {
            Text(
                if (pagerState.currentPage < pages.size - 1) "Continue" else "Get Started",
                color = Color.Black,
                fontWeight = FontWeight.Bold,
            )
        }
    }
}
