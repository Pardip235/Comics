package com.bpn.comics

import kotlin.test.Test
import kotlin.test.assertTrue

class GreetingTest {
    @Test
    fun testGreeting() {
        val greeting = Greeting()
        assertTrue(greeting.greet().isNotEmpty(), "Greeting should not be empty")
    }
}

