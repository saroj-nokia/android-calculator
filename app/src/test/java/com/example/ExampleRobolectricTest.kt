package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.viewmodel.CalculatorViewModel
import com.example.viewmodel.HistoryItem
import com.example.util.CalculatorEvaluator
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.shadows.ShadowLooper
import java.util.concurrent.TimeUnit

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35])
class ExampleRobolectricTest {

    private lateinit var context: Context
    private lateinit var viewModel: CalculatorViewModel

    @Before
    fun setUp() {
        context = ApplicationProvider.getApplicationContext()
        viewModel = CalculatorViewModel(ApplicationProvider.getApplicationContext())
        viewModel.onKeyPress("AC")
        viewModel.clearHistory()
    }

    @Test
    fun `read string from context`() {
        val appName = context.getString(R.string.app_name)
        assertEquals("Calculator", appName)
    }

    @Test
    fun `test basic operations`() {
        // 5 + 3 * 2
        viewModel.onKeyPress("5")
        viewModel.onKeyPress("+")
        viewModel.onKeyPress("3")
        viewModel.onKeyPress("*")
        viewModel.onKeyPress("2")
        
        ShadowLooper.idleMainLooper(150, TimeUnit.MILLISECONDS)
        assertEquals("5+3×2", viewModel.formula.value)
        assertEquals("= 11", viewModel.calculationResult.value)

        // Evaluate (=)
        viewModel.onKeyPress("=")
        assertEquals("11", viewModel.formula.value)
        assertEquals("", viewModel.calculationResult.value)
    }

    @Test
    fun `test division by zero`() {
        viewModel.onKeyPress("6")
        viewModel.onKeyPress("/")
        viewModel.onKeyPress("0")
        assertEquals("6÷0", viewModel.formula.value)
        assertEquals("", viewModel.calculationResult.value) // Live result shouldn't be valid, it's divided by zero

        viewModel.onKeyPress("=")
        assertEquals("Error", viewModel.calculationResult.value)
    }

    @Test
    fun `test smart delete`() {
        // sin(
        viewModel.onKeyPress("sin")
        assertEquals("sin(", viewModel.formula.value)
        
        // delete
        viewModel.onKeyPress("DEL")
        assertEquals("", viewModel.formula.value)

        // 123
        viewModel.onKeyPress("1")
        viewModel.onKeyPress("2")
        viewModel.onKeyPress("3")
        assertEquals("123", viewModel.formula.value)

        viewModel.onKeyPress("DEL")
        assertEquals("12", viewModel.formula.value)
    }

    @Test
    fun `test scientific mode functions`() {
        viewModel.toggleDegrees() // RAD mode
        // sin(pi)
        viewModel.onKeyPress("sin")
        viewModel.onKeyPress("pi")
        viewModel.onKeyPress(")")
        assertEquals("sin(π)", viewModel.formula.value)

        // sin(pi) = 0
        viewModel.onKeyPress("=")
        assertEquals("0", viewModel.formula.value)
    }

    @Test
    fun `test power and square root`() {
        // 2 ^ 3
        viewModel.onKeyPress("2")
        viewModel.onKeyPress("^")
        viewModel.onKeyPress("3")
        ShadowLooper.idleMainLooper(150, TimeUnit.MILLISECONDS)
        assertEquals("2^3", viewModel.formula.value)
        assertEquals("= 8", viewModel.calculationResult.value)

        viewModel.onKeyPress("=")
        assertEquals("8", viewModel.formula.value)
    }

    @Test
    fun `test factorial and percent`() {
        // 5!
        viewModel.onKeyPress("5")
        viewModel.onKeyPress("x!")
        ShadowLooper.idleMainLooper(150, TimeUnit.MILLISECONDS)
        assertEquals("5!", viewModel.formula.value)
        assertEquals("= 120", viewModel.calculationResult.value)

        // 5! % -> should be 1.2
        viewModel.onKeyPress("%")
        ShadowLooper.idleMainLooper(150, TimeUnit.MILLISECONDS)
        assertEquals("5!%", viewModel.formula.value)
        assertEquals("= 1.2", viewModel.calculationResult.value)
    }

    @Test
    fun `test invalid syntax live evaluation`() {
        // 5 + +
        viewModel.onKeyPress("5")
        viewModel.onKeyPress("+")
        viewModel.onKeyPress("+")
        assertEquals("5++", viewModel.formula.value)
        assertEquals("", viewModel.calculationResult.value) // Evaluator should handle gracefully

        viewModel.onKeyPress("=")
        assertEquals("Error", viewModel.calculationResult.value)
    }

    @Test
    fun `test unmatched parenthesis robust evaluation`() {
        viewModel.onKeyPress("2")
        viewModel.onKeyPress("*")
        viewModel.onKeyPress("(")
        viewModel.onKeyPress("3")
        viewModel.onKeyPress("+")
        viewModel.onKeyPress("4")
        // formula: 2×(3+4 - wait wait, closing bracket is omitted
        assertEquals("2×(3+4", viewModel.formula.value)
        assertEquals("", viewModel.calculationResult.value) // Should show empty during live typing due to strict parenthesis parsing
        
        // Evaluate (=)
        viewModel.onKeyPress("=")
        assertEquals("Error", viewModel.calculationResult.value) // Should show Error on evaluation due to strict parsing
    }

    @Test
    fun `test history save and reload`() {
        // Perform calculation: 4 * 25
        viewModel.onKeyPress("4")
        viewModel.onKeyPress("*")
        viewModel.onKeyPress("2")
        viewModel.onKeyPress("5")
        viewModel.onKeyPress("=")

        assertEquals("100", viewModel.formula.value)
        assertEquals(1, viewModel.history.value.size)

        val historyItem = viewModel.history.value.first()
        assertEquals("4×25", historyItem.formula)
        assertEquals("100", historyItem.result)

        // Perform second calculation: 3 + 2
        viewModel.onKeyPress("AC")
        viewModel.onKeyPress("3")
        viewModel.onKeyPress("+")
        viewModel.onKeyPress("2")
        viewModel.onKeyPress("=")

        assertEquals(2, viewModel.history.value.size)

        // Restore first item
        viewModel.loadHistoryItem(historyItem)
        assertEquals("4×25", viewModel.formula.value)
        assertEquals("= 100", viewModel.calculationResult.value)
    }

    @Test
    fun `test math undefined boundaries`() {
        // Logarithmic functions of zero or negative numbers should evaluate to error
        val logZero = CalculatorEvaluator.evaluate("log(0)", false)
        assertTrue(logZero.isInfinite()) // -Infinity

        val lnZero = CalculatorEvaluator.evaluate("ln(0)", false)
        assertTrue(lnZero.isInfinite()) // -Infinity

        // Square root of negative number
        val sqrtNeg = CalculatorEvaluator.evaluate("sqrt(-4)", false)
        assertTrue(sqrtNeg.isNaN())

        // Arcsine out of range -1 to 1
        val asinOutOfRange = CalculatorEvaluator.evaluate("asin(1.5)", false)
        assertTrue(asinOutOfRange.isNaN())
    }

    @Test
    fun `test function tokenization edge cases`() {
        // xsin(30) with x=2 in degrees -> 2 * sin(30) = 2 * 0.5 = 1.0
        val eval1 = CalculatorEvaluator.evaluate("xsin(30)", true, xValue = 2.0)
        assertEquals(1.0, eval1, 0.001)

        // xe with x=5 -> 5 * e
        val eval2 = CalculatorEvaluator.evaluate("xe", false, xValue = 5.0)
        assertEquals(5.0 * Math.E, eval2, 0.001)

        // xpi with x=4 -> 4 * pi
        val eval3 = CalculatorEvaluator.evaluate("xpi", false, xValue = 4.0)
        assertEquals(4.0 * Math.PI, eval3, 0.001)

        // asin(0.5) -> should still work, not tokenize as "a" and "sin"
        val eval4 = CalculatorEvaluator.evaluate("asin(0.5)", true)
        assertEquals(30.0, eval4, 0.001)

        // xsinx with x=30 in degrees -> 30 * sin(30) = 30 * 0.5 = 15.0
        val eval5 = CalculatorEvaluator.evaluate("xsinx", true, xValue = 30.0)
        assertEquals(15.0, eval5, 0.001)
    }
}
