package com.example

import com.example.util.CalculatorEvaluator
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
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

  @Test
  fun malformedExpressions_dontCrash() {
    // Tests various incomplete or incorrect strings to verify exceptions/handling
    try {
      CalculatorEvaluator.evaluate("5 +", false)
    } catch (e: Exception) {
      // Incomplete formulas are expected to throw during evaluate() and be caught in ViewModel
    }

    try {
      CalculatorEvaluator.evaluate("sin(", false)
    } catch (e: Exception) {
      // expected
    }

    try {
      CalculatorEvaluator.evaluate("(", false)
    } catch (e: Exception) {
      // expected
    }

    try {
      CalculatorEvaluator.evaluate(".", false)
    } catch (e: Exception) {
      // expected
    }
  }

  @Test
  fun postfixOperators() {
    assertEquals(0.05, CalculatorEvaluator.evaluate("5%", false), 0.0001)
    assertEquals(120.0, CalculatorEvaluator.evaluate("5!", false), 0.0001)
  }

  @Test
  fun divisionByZero() {
    try {
      CalculatorEvaluator.evaluate("5 / 0", false)
    } catch (e: Exception) {
      assertTrue(e is ArithmeticException)
    }
  }

  @Test
  fun testImplicit() {
    System.err.println("Running implicit test")
    assertEquals(6.0, CalculatorEvaluator.evaluate("2(3)", false), 0.0001)
    System.err.println("2(3) passed")
    assertEquals(6.28318, CalculatorEvaluator.evaluate("2pi", false), 0.001)
    System.err.println("2pi passed")
  }
}
