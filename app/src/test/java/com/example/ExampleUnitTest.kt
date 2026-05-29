package com.example

import com.example.util.CalculatorEvaluator
import org.junit.Assert.assertEquals
import org.junit.Test

class ExampleUnitTest {
  @Test
  fun addition_isCorrect() {
    assertEquals(4.0, CalculatorEvaluator.evaluate("2 + 2", false), 0.0001)
  }

  @Test
  fun complexExpression_isCorrect() {
    assertEquals(14.0, CalculatorEvaluator.evaluate("2 + 3 * 4", false), 0.0001)
    assertEquals(11.0, CalculatorEvaluator.evaluate("2 * (3 + 5.5) - 6", false), 0.0001)
  }

  @Test
  fun scientificFunctions_isCorrect() {
    assertEquals(1.0, CalculatorEvaluator.evaluate("ln(e)", false), 0.0001)
    assertEquals(2.0, CalculatorEvaluator.evaluate("log(100)", false), 0.0001)
    assertEquals(1.0, CalculatorEvaluator.evaluate("sin(pi / 2)", false), 0.0001)
  }
}
