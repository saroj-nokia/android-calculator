package com.example

import com.example.util.Complex
import com.example.util.ComplexEvaluator
import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Test

class ComplexEvaluatorTest {

    @Test
    fun testParseLiteral() {
        assertEquals(Complex(3.0, 4.0), ComplexEvaluator.parseLiteral("3+4i"))
        assertEquals(Complex(2.0, -3.0), ComplexEvaluator.parseLiteral("2-3i"))
        assertEquals(Complex(5.0, 0.0), ComplexEvaluator.parseLiteral("5"))
        assertEquals(Complex(0.0, -2.0), ComplexEvaluator.parseLiteral("-2i"))
        assertEquals(Complex(0.0, 1.0), ComplexEvaluator.parseLiteral("i"))
        assertEquals(Complex(0.0, -1.0), ComplexEvaluator.parseLiteral("-i"))
    }

    @Test
    fun testEvaluateOperations() {
        // Addition
        assertEquals(Complex(4.0, 6.0), ComplexEvaluator.evaluate("1+2i + 3+4i"))
        
        // Subtraction
        assertEquals(Complex(-2.0, -2.0), ComplexEvaluator.evaluate("1+2i - 3+4i"))
        
        // Multiplication
        // (1+1i)*(2-3i) = 2 - 3i + 2i - 3i^2 = 2 - i + 3 = 5 - i
        assertEquals(Complex(5.0, -1.0), ComplexEvaluator.evaluate("1+1i * 2-3i"))
        
        // Division
        // (1+1i)/(1-1i) = (1+1i)(1+1i)/2 = (1+2i-1)/2 = i
        assertEquals(Complex(0.0, 1.0), ComplexEvaluator.evaluate("1+1i / 1-1i"))
    }

    @Test
    fun testModulusAndConjugate() {
        val c = Complex(3.0, 4.0)
        assertEquals(5.0, c.modulus(), 0.0001)
        assertEquals(Complex(3.0, -4.0), c.conjugate())
    }

    @Test
    fun testDivisionByZero() {
        assertThrows(ArithmeticException::class.java) {
            ComplexEvaluator.evaluate("1+1i / 0")
        }
    }

    @Test
    fun testFormatComplex() {
        assertEquals("3 + 4i", ComplexEvaluator.formatComplex(Complex(3.0, 4.0)))
        assertEquals("-2 - 3i", ComplexEvaluator.formatComplex(Complex(-2.0, -3.0)))
        assertEquals("5", ComplexEvaluator.formatComplex(Complex(5.0, 0.0)))
        assertEquals("-2i", ComplexEvaluator.formatComplex(Complex(0.0, -2.0)))
        assertEquals("i", ComplexEvaluator.formatComplex(Complex(0.0, 1.0)))
        assertEquals("-i", ComplexEvaluator.formatComplex(Complex(0.0, -1.0)))
        assertEquals("0", ComplexEvaluator.formatComplex(Complex(0.0, 0.0)))
        assertEquals("3 - i", ComplexEvaluator.formatComplex(Complex(3.0, -1.0)))
    }
}
