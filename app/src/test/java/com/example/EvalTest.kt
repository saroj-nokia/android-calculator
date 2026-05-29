package com.example

import com.example.util.CalculatorEvaluator

fun main() {
    println(CalculatorEvaluator.evaluate("2pi", false))
    println(CalculatorEvaluator.evaluate("2", false))
    println(CalculatorEvaluator.evaluate("2+2", false))
}
