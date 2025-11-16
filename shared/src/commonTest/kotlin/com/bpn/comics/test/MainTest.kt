package com.bpn.comics.test

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.junit.rules.TestWatcher
import org.junit.runner.Description

/**
 * A base test class with useful infrastructure for tests that need Main dispatcher.
 */
@OptIn(ExperimentalCoroutinesApi::class)
abstract class MainTest {
    
    /**
     * A test rule that sets up a [TestDispatcher] to override the Main dispatcher.
     */
    @get:org.junit.Rule
    val mainDispatcherRule = MainDispatcherRule()
}

/**
 * Rules & helpers
 * Test rule that sets up and tears down the Main dispatcher for testing.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class MainDispatcherRule(
    private val testDispatcher: TestDispatcher = StandardTestDispatcher()
) : TestWatcher() {
    
    override fun starting(description: Description?) {
        Dispatchers.setMain(testDispatcher)
    }

    override fun finished(description: Description?) {
        Dispatchers.resetMain()
    }
}
