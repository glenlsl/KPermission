package com.solin.kpermission

import org.junit.Test

import org.junit.Assert.*
import kotlin.random.Random

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * @see [Testing documentation](http://d.android.com/tools/testing)
 */
class ExampleUnitTest {
    @Test
    fun addition_isCorrect() {
        assertEquals(4, (2 + 2).toLong())
    }

    @Test
    fun testRandom() {
        val temp = Random.nextBits(16)
        println(temp)
    }
}