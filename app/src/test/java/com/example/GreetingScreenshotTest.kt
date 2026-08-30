package com.example

import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onRoot
import com.example.data.local.EventEntity
import com.example.ui.calendar.TodayEventSwipeCard
import com.example.ui.theme.MyApplicationTheme
import com.github.takahirom.roborazzi.RobolectricDeviceQualifiers
import com.github.takahirom.roborazzi.captureRoboImage
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(qualifiers = RobolectricDeviceQualifiers.Pixel8, sdk = [36])
class GreetingScreenshotTest {

    @get:Rule val composeTestRule = createComposeRule()

    @Test
    fun today_card_screenshot() {
        val testEvent = EventEntity(
            id = "test_event_card",
            userId = "user_1",
            title = "Team Sprint Planning",
            description = "Review roadmap and assign milestones.",
            location = "Room 302",
            startTime = System.currentTimeMillis(),
            category = "Work",
            color = "#3B82F6",
            repeatNotificationCount = 3,
            repeatGapMinutes = 5
        )

        composeTestRule.setContent {
            MyApplicationTheme {
                TodayEventSwipeCard(
                    event = testEvent,
                    onAcknowledge = {},
                    onToggleComplete = {},
                    onEdit = {},
                    onReschedule = {},
                    pageNumber = 1,
                    totalPages = 3
                )
            }
        }

        composeTestRule.onRoot().captureRoboImage(filePath = "src/test/screenshots/today_card.png")
    }
}
