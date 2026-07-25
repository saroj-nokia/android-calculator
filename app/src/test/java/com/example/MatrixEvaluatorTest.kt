package com.example

import com.example.util.Matrix
import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.math.abs

class MatrixEvaluatorTest {

    @Test
    fun testDimensions() {
        assertThrows(IllegalArgumentException::class.java) {
            Matrix(1, 1, listOf(listOf(1.0)))
        }
        assertThrows(IllegalArgumentException::class.java) {
            Matrix(2, 3, listOf(listOf(1.0, 2.0, 3.0), listOf(4.0, 5.0, 6.0)))
        }
        val m = Matrix(2, 2, listOf(listOf(1.0, 2.0), listOf(3.0, 4.0)))
        assertEquals(2, m.rows)
    }

    @Test
    fun testAdditionSubtraction() {
        val m1 = Matrix(2, 2, listOf(listOf(1.0, 2.0), listOf(3.0, 4.0)))
        val m2 = Matrix(2, 2, listOf(listOf(5.0, 6.0), listOf(7.0, 8.0)))
        
        val sum = m1 + m2
        assertEquals(listOf(listOf(6.0, 8.0), listOf(10.0, 12.0)), sum.data)

        val diff = m1 - m2
        assertEquals(listOf(listOf(-4.0, -4.0), listOf(-4.0, -4.0)), diff.data)
    }

    @Test
    fun testMultiplication() {
        val m1 = Matrix(2, 2, listOf(listOf(1.0, 2.0), listOf(3.0, 4.0)))
        val m2 = Matrix(2, 2, listOf(listOf(2.0, 0.0), listOf(1.0, 2.0)))
        
        val product = m1 * m2
        assertEquals(listOf(listOf(4.0, 4.0), listOf(10.0, 8.0)), product.data)
    }

    @Test
    fun testDeterminant() {
        val identity = Matrix(3, 3, listOf(listOf(1.0, 0.0, 0.0), listOf(0.0, 1.0, 0.0), listOf(0.0, 0.0, 1.0)))
        assertEquals(1.0, identity.determinant(), 1e-10)

        val singular = Matrix(2, 2, listOf(listOf(1.0, 2.0), listOf(2.0, 4.0)))
        assertEquals(0.0, singular.determinant(), 1e-10)

        val m = Matrix(3, 3, listOf(
            listOf(6.0, 1.0, 1.0),
            listOf(4.0, -2.0, 5.0),
            listOf(2.0, 8.0, 7.0)
        ))
        assertEquals(-306.0, m.determinant(), 1e-10)
    }

    @Test
    fun testInverse() {
        // Singular matrix inverse should throw
        val singular = Matrix(2, 2, listOf(listOf(1.0, 2.0), listOf(2.0, 4.0)))
        assertThrows(ArithmeticException::class.java) {
            singular.inverse()
        }

        // Invertible matrix
        val m = Matrix(2, 2, listOf(listOf(4.0, 7.0), listOf(2.0, 6.0)))
        val inv = m.inverse()
        
        // Check M * M^-1 = I
        val product = m * inv
        assertEquals(1.0, product.data[0][0], 1e-10)
        assertEquals(0.0, product.data[0][1], 1e-10)
        assertEquals(0.0, product.data[1][0], 1e-10)
        assertEquals(1.0, product.data[1][1], 1e-10)
    }

    @Test
    fun testPartialPivoting() {
        // A matrix that has a 0 at the top-left pivot position but is invertible.
        // It requires a row swap for Gaussian elimination to succeed.
        val m = Matrix(3, 3, listOf(
            listOf(0.0, 2.0, 3.0),
            listOf(1.0, 5.0, 6.0),
            listOf(7.0, 8.0, 9.0)
        ))
        
        // This determinant will be non-zero
        val det = m.determinant()
        assertTrue(abs(det) > 1e-10)

        // Inverse shouldn't throw, thanks to partial pivoting
        val inv = m.inverse()
        val product = m * inv
        
        // Verify identity
        for (i in 0 until 3) {
            for (j in 0 until 3) {
                if (i == j) {
                    assertEquals(1.0, product.data[i][j], 1e-10)
                } else {
                    assertEquals(0.0, product.data[i][j], 1e-10)
                }
            }
        }
    }

    @Test
    fun testTranspose() {
        val m = Matrix(2, 2, listOf(listOf(1.0, 2.0), listOf(3.0, 4.0)))
        val t = m.transpose()
        assertEquals(listOf(listOf(1.0, 3.0), listOf(2.0, 4.0)), t.data)
    }
}
