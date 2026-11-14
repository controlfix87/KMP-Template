package com.cmptemplate.feature.about.ui

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelStore
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import app.cash.turbine.test
import com.cmptemplate.core.common.DispatcherProvider
import com.cmptemplate.core.model.AppInfo
import com.cmptemplate.feature.about.report.CrashRecord
import com.cmptemplate.feature.about.report.DeviceInfo
import com.cmptemplate.feature.about.report.Diagnostics
import com.cmptemplate.feature.about.report.ReportDraft
import com.cmptemplate.feature.about.report.ReportKind
import com.cmptemplate.feature.about.report.ReportResult
import com.cmptemplate.feature.about.report.ReportSender
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

private class FakeSender(var result: ReportResult = ReportResult.Sent("T-1")) : ReportSender {
    val sent = mutableListOf<ReportDraft>()
    override suspend fun send(draft: ReportDraft): ReportResult { sent += draft; return result }
}

private class CrashDiagnostics(private val at: Long?) : Diagnostics {
    override fun device() = DeviceInfo()
    override suspend fun recentLog(): String? = null
    override fun lastCrash() = at?.let { CrashRecord(it, "trace") }
    override fun clearCrash() = Unit
}

@OptIn(ExperimentalCoroutinesApi::class)
class ReportViewModelTest {
    private val dispatcher = UnconfinedTestDispatcher()
    private val dispatchers = object : DispatcherProvider {
        override val main: CoroutineDispatcher = dispatcher
        override val io: CoroutineDispatcher = dispatcher
        override val default: CoroutineDispatcher = dispatcher
    }
    private val stores = mutableListOf<ViewModelStore>()
    private val app = AppInfo("com.example.app", "Example", "1", "1", "test", "android")

    @BeforeTest fun setUp() { Dispatchers.setMain(dispatcher) }
    @AfterTest fun tearDown() { stores.forEach { it.clear() }; stores.clear(); Dispatchers.resetMain() }

    private fun model(sender: ReportSender, crashAt: Long? = null, kind: ReportKind = ReportKind.BUG, saved: SavedStateHandle = SavedStateHandle()): ReportViewModel {
        val store = ViewModelStore().also { stores.add(it) }
        return ViewModelProvider.create(store, viewModelFactory {
            initializer { ReportViewModel(kind, sender, CrashDiagnostics(crashAt), app, saved, dispatchers) }
        })[ReportViewModel::class]
    }

    @Test fun `send stays disabled until title and message are filled`() = runTest {
        val vm = model(FakeSender())
        vm.state.test {
            assertFalse(expectMostRecentItem().canSend)
            vm.onAction(ReportAction.Title("Broken"))
            assertFalse(expectMostRecentItem().canSend)
            vm.onAction(ReportAction.Message("Steps"))
            assertTrue(expectMostRecentItem().canSend)
        }
    }

    @Test fun `a recorded crash is shown, and a crash report needs no title`() = runTest {
        val vm = model(FakeSender(), crashAt = 1234L, kind = ReportKind.CRASH)
        vm.state.test {
            val s = expectMostRecentItem()
            assertEquals(1234L, s.crashAt)
            assertTrue(s.canSend)
        }
    }

    @Test fun `successful send reports the ticket and sends the draft once`() = runTest {
        val sender = FakeSender()
        val vm = model(sender)
        vm.state.test {
            vm.onAction(ReportAction.Title("T")); vm.onAction(ReportAction.Message("M")); vm.onAction(ReportAction.IncludeLogs(false))
            vm.onAction(ReportAction.Send)
            assertEquals(ReportStatus.Sent("T-1"), expectMostRecentItem().status)
            vm.onAction(ReportAction.Send)
        }
        assertEquals(listOf(ReportDraft(ReportKind.BUG, "T", "M", "", includeLogs = false)), sender.sent)
    }

    @Test fun `failure offers the mail fallback and clears on edit`() = runTest {
        val vm = model(FakeSender(ReportResult.Offline))
        vm.state.test {
            vm.onAction(ReportAction.Title("T")); vm.onAction(ReportAction.Message("M")); vm.onAction(ReportAction.Send)
            assertEquals(ReportStatus.Failed(SendFailure.OFFLINE), expectMostRecentItem().status)
            assertTrue(vm.mailUri().startsWith("mailto:support@controlfix.net"))
            vm.onAction(ReportAction.Message("M2"))
            assertEquals(ReportStatus.Editing, expectMostRecentItem().status)
        }
    }

    @Test fun `draft restores from saved state`() = runTest {
        val saved = SavedStateHandle(mapOf("report_title" to "Saved", "report_message" to "Body", "report_kind" to "suggestion"))
        val vm = model(FakeSender(), saved = saved)
        vm.state.test {
            val s = expectMostRecentItem()
            assertEquals("Saved", s.title)
            assertEquals(ReportKind.SUGGESTION, s.kind)
            assertNull(s.crashAt)
        }
    }
}
