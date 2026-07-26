package com.example

import com.example.util.StatisticsEvaluator
import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Test

class StatisticsEvaluatorTest {

    @Test
    fun testDescriptiveStats() {
        val data = listOf(1.0, 2.0, 2.0, 3.0, 4.0, 4.0, 4.0, 5.0)
        val stats = StatisticsEvaluator.descriptiveStats(data)
        
        assertEquals(8, stats.count)
        assertEquals(25.0, stats.sum, 1e-6)
        assertEquals(3.125, stats.mean, 1e-6)
        // removed bad assert
        // Wait, indices: 0(1), 1(2), 2(2), 3(3), 4(4), 5(4), 6(4), 7(5). 
        // 8/2 - 1 = 3 -> 3.0. 8/2 = 4 -> 4.0. Average is 3.5.
        assertEquals(3.5, stats.median, 1e-6)
        assertEquals(4.0, stats.mode, 1e-6)
        assertEquals(1.0, stats.min, 1e-6)
        assertEquals(5.0, stats.max, 1e-6)
        
        // sum = 25. mean = 3.125
        // diffs: -2.125, -1.125, -1.125, -0.125, 0.875, 0.875, 0.875, 1.875
        // sq: 4.515625, 1.265625, 1.265625, 0.015625, 0.765625, 0.765625, 0.765625, 3.515625
        // sum = 12.875
        assertEquals(12.875 / 8, stats.populationVariance, 1e-6)
        assertEquals(12.875 / 7, stats.sampleVariance, 1e-6)
    }

    @Test
    fun testModeTie() {
        val data = listOf(3.0, 3.0, 2.0, 2.0, 5.0)
        val stats = StatisticsEvaluator.descriptiveStats(data)
        // frequencies: 3.0->2, 2.0->2, 5.0->1. max freq is 2. smallest value is 2.0.
        assertEquals(2.0, stats.mode, 1e-6)
    }

    @Test
    fun testEmptyDataset() {
        assertThrows(IllegalArgumentException::class.java) {
            StatisticsEvaluator.descriptiveStats(emptyList())
        }
    }

    @Test
    fun testSingleValueDataset() {
        val data = listOf(5.0)
        assertThrows(IllegalArgumentException::class.java) {
            StatisticsEvaluator.descriptiveStats(data)
        }
    }

    @Test
    fun testRegression() {
        // y = 2x + 1
        val xs = listOf(1.0, 2.0, 3.0, 4.0)
        val ys = listOf(3.0, 5.0, 7.0, 9.0)
        val result = StatisticsEvaluator.linearRegression(xs, ys)
        
        assertEquals(2.0, result.slope, 1e-6)
        assertEquals(1.0, result.intercept, 1e-6)
        assertEquals(1.0, result.r, 1e-6)
    }

    @Test
    fun testRegressionNoRelationship() {
        val xs = listOf(1.0, 2.0, 3.0, 4.0, 5.0)
        val ys = listOf(2.0, 2.0, 2.0, 2.0, 2.0)
        val result = StatisticsEvaluator.linearRegression(xs, ys)
        
        assertEquals(0.0, result.slope, 1e-6)
        assertEquals(2.0, result.intercept, 1e-6)
        // If all ys are the same, rDenominator is 0, so r should be 0.
        assertEquals(0.0, result.r, 1e-6)
    }

    @Test
    fun testRegressionIdenticalX() {
        val xs = listOf(2.0, 2.0, 2.0)
        val ys = listOf(1.0, 2.0, 3.0)
        assertThrows(ArithmeticException::class.java) {
            StatisticsEvaluator.linearRegression(xs, ys)
        }
    }
}
