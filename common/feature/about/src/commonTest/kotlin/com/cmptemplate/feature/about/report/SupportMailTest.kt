package com.cmptemplate.feature.about.report

import com.cmptemplate.core.model.AppInfo
import kotlin.test.Test
import kotlin.test.assertContains
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class SupportMailTest {
    private val app = AppInfo("com.example.app", "Example", "1.2.3", "42", "play/release", "android")

    @Test fun `addresses support with an encoded subject, the message and device details`() {
        val uri = SupportMail.uri(ReportDraft(ReportKind.BUG, "It broke & stopped", "Steps: 1 2"), app, DeviceInfo(model = "P1", os = "Android 17", api = "37"))
        assertTrue(uri.startsWith("mailto:support@controlfix.net?subject="))
        assertContains(uri, "%5BExample%5D%20Bug%3A%20It%20broke%20%26%20stopped")
        assertContains(uri, "Steps%3A%201%202")
        assertContains(uri, "API%2037")
        assertFalse(" " in uri)
    }

    @Test fun `keeps the link short and redacts the message`() {
        val uri = SupportMail.uri(ReportDraft(ReportKind.SUGGESTION, "x", "mail me at a@b.co " + "y".repeat(10_000)), app, DeviceInfo())
        assertFalse("a%40b.co" in uri)
        assertTrue(uri.length < 6000, "uri length ${uri.length}")
    }
}
