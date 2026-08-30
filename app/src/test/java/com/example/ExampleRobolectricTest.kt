package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.local.EventEntity
import com.example.data.local.UserEntity
import com.example.notification.NotificationHelper
import com.example.util.DateUtils
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

    @Test
    fun `read string from context`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val appName = context.getString(R.string.app_name)
        assertEquals("Calendar", appName)
    }

    @Test
    fun `test birthday countdown calculations`() {
        val countdown = DateUtils.getBirthdayCountdownText("2000-08-30")
        assertNotNull(countdown)
        assertTrue(countdown!!.contains("Birthday"))
    }

    @Test
    fun `test notification helper acknowledgment flow`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val eventId = "test_event_100"

        NotificationHelper.clearAcknowledged(context, eventId)
        assertEquals(false, NotificationHelper.isAcknowledged(context, eventId))

        NotificationHelper.markAcknowledged(context, eventId)
        assertEquals(true, NotificationHelper.isAcknowledged(context, eventId))

        NotificationHelper.clearAcknowledged(context, eventId)
        assertEquals(false, NotificationHelper.isAcknowledged(context, eventId))
    }

    @Test
    fun `test date utils iso8601 parser and formatter`() {
        val now = System.currentTimeMillis()
        val iso = DateUtils.formatIso8601(now)
        val parsed = DateUtils.parseIso8601(iso)
        assertNotNull(parsed)
        assertTrue(Math.abs(now - parsed!!) < 1000)
    }
}

