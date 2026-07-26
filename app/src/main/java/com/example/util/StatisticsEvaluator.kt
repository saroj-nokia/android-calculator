package com.example.util

import kotlin.math.pow
import kotlin.math.sqrt

data class StatsResult(
    val count: Int,
    val sum: Double,
    val mean: Double,
    val median: Double,
    val mode: Double,
    val min: Double,
    val max: Double,
    val sampleVariance: Double,
    val populationVariance: Double,
    val sampleStdDev: Double,
    val populationStdDev: Double
)

data class RegressionResult(
    val slope: Double,
    val intercept: Double,
    val r: Double
)

object StatisticsEvaluator {

    fun descriptiveStats(data: List<Double>): StatsResult {
        require(data.isNotEmpty()) { "Dataset is empty" }
        
        val count = data.size
        val sum = data.sum()
        val mean = sum / count
        
        val sorted = data.sorted()
        val median = if (count % 2 == 0) {
            (sorted[count / 2 - 1] + sorted[count / 2]) / 2.0
        } else {
            sorted[count / 2]
        }
        
        val frequencies = data.groupingBy { it }.eachCount()
        val maxFreq = frequencies.values.maxOrNull() ?: 0
        // Smallest value if tie
        val mode = frequencies.filter { it.value == maxFreq }.keys.minOrNull() ?: 0.0
        
        val min = sorted.first()
        val max = sorted.last()
        
        val sumSquaredDiffs = data.sumOf { (it - mean).pow(2) }
        val populationVariance = sumSquaredDiffs / count
        val populationStdDev = sqrt(populationVariance)
        
        val sampleVariance = if (count > 1) sumSquaredDiffs / (count - 1) else {
            throw IllegalArgumentException("Sample variance requires at least 2 values")
        }
        val sampleStdDev = sqrt(sampleVariance)
        
        return StatsResult(
            count = count,
            sum = sum,
            mean = mean,
            median = median,
            mode = mode,
            min = min,
            max = max,
            sampleVariance = sampleVariance,
            populationVariance = populationVariance,
            sampleStdDev = sampleStdDev,
            populationStdDev = populationStdDev
        )
    }

    fun linearRegression(xs: List<Double>, ys: List<Double>): RegressionResult {
        require(xs.size == ys.size) { "Datasets must have the same length" }
        require(xs.size >= 2) { "Regression requires at least 2 points" }
        
        val n = xs.size
        val sumX = xs.sum()
        val sumY = ys.sum()
        val sumXY = xs.zip(ys) { x, y -> x * y }.sum()
        val sumX2 = xs.sumOf { it.pow(2) }
        val sumY2 = ys.sumOf { it.pow(2) }
        
        val denominator = n * sumX2 - sumX.pow(2)
        if (denominator == 0.0) {
            throw ArithmeticException("All x values are identical, cannot compute slope")
        }
        
        val slope = (n * sumXY - sumX * sumY) / denominator
        val intercept = (sumY - slope * sumX) / n
        
        val rNumerator = n * sumXY - sumX * sumY
        val rDenominator = sqrt((n * sumX2 - sumX.pow(2)) * (n * sumY2 - sumY.pow(2)))
        
        val r = if (rDenominator == 0.0) 0.0 else rNumerator / rDenominator
        
        return RegressionResult(slope, intercept, r)
    }
}
