package com.beader.core.testing

import android.app.Application
import android.content.Context
import androidx.test.runner.AndroidJUnitRunner
import dagger.hilt.android.testing.HiltTestApplication

/**
 * Instrumentation test runner that swaps in [HiltTestApplication] so
 * `@HiltAndroidTest` classes can inject a test component graph. Registered
 * as `testInstrumentationRunner` in every module with androidTest sources.
 */
class HiltTestRunner : AndroidJUnitRunner() {
    override fun newApplication(cl: ClassLoader?, className: String?, context: Context?): Application =
        super.newApplication(cl, HiltTestApplication::class.java.name, context)
}
