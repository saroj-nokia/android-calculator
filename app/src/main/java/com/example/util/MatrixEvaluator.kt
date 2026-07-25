package com.example.util

import kotlin.math.abs

data class Matrix(val rows: Int, val cols: Int, val data: List<List<Double>>) {
    init {
        require(rows in 2..4 && cols in 2..4 && rows == cols) {
            "Only 2x2, 3x3, and 4x4 square matrices are supported."
        }
        require(data.size == rows && data.all { it.size == cols }) {
            "Data dimensions do not match specified rows and cols."
        }
    }

    operator fun plus(other: Matrix): Matrix {
        require(this.rows == other.rows && this.cols == other.cols) {
            "Matrix dimensions must match for addition."
        }
        return Matrix(rows, cols, data.zip(other.data) { row1, row2 ->
            row1.zip(row2) { a, b -> a + b }
        })
    }

    operator fun minus(other: Matrix): Matrix {
        require(this.rows == other.rows && this.cols == other.cols) {
            "Matrix dimensions must match for subtraction."
        }
        return Matrix(rows, cols, data.zip(other.data) { row1, row2 ->
            row1.zip(row2) { a, b -> a - b }
        })
    }

    operator fun times(other: Matrix): Matrix {
        require(this.cols == other.rows) {
            "Matrix dimensions must be compatible for multiplication."
        }
        val resultData = List(this.rows) { i ->
            List(other.cols) { j ->
                var sum = 0.0
                for (k in 0 until this.cols) {
                    sum += this.data[i][k] * other.data[k][j]
                }
                sum
            }
        }
        return Matrix(this.rows, other.cols, resultData)
    }

    fun transpose(): Matrix {
        val resultData = List(cols) { i ->
            List(rows) { j ->
                data[j][i]
            }
        }
        return Matrix(cols, rows, resultData)
    }

    fun determinant(): Double {
        val n = rows
        val a = Array(n) { i -> DoubleArray(n) { j -> data[i][j] } }
        var det = 1.0

        for (i in 0 until n) {
            // Partial pivoting
            var maxRow = i
            var maxVal = abs(a[i][i])
            for (k in i + 1 until n) {
                if (abs(a[k][i]) > maxVal) {
                    maxVal = abs(a[k][i])
                    maxRow = k
                }
            }

            if (maxVal < 1e-10) {
                return 0.0 // Singular matrix
            }

            if (maxRow != i) {
                // Swap rows
                val temp = a[i]
                a[i] = a[maxRow]
                a[maxRow] = temp
                det = -det // Row swap changes sign of determinant
            }

            val pivot = a[i][i]
            det *= pivot

            // Eliminate column below pivot
            for (k in i + 1 until n) {
                val factor = a[k][i] / pivot
                for (j in i until n) {
                    a[k][j] -= factor * a[i][j]
                }
            }
        }

        return det
    }

    fun inverse(): Matrix {
        val n = rows
        val a = Array(n) { i -> DoubleArray(n) { j -> data[i][j] } }
        val inv = Array(n) { i -> DoubleArray(n) { j -> if (i == j) 1.0 else 0.0 } }

        for (i in 0 until n) {
            // Partial pivoting
            var maxRow = i
            var maxVal = abs(a[i][i])
            for (k in i + 1 until n) {
                if (abs(a[k][i]) > maxVal) {
                    maxVal = abs(a[k][i])
                    maxRow = k
                }
            }

            if (maxVal < 1e-10) {
                throw ArithmeticException("Matrix is singular")
            }

            if (maxRow != i) {
                // Swap rows in a
                val temp = a[i]
                a[i] = a[maxRow]
                a[maxRow] = temp
                // Swap rows in inv
                val tempInv = inv[i]
                inv[i] = inv[maxRow]
                inv[maxRow] = tempInv
            }

            val pivot = a[i][i]
            // Normalize pivot row
            for (j in 0 until n) {
                a[i][j] /= pivot
                inv[i][j] /= pivot
            }

            // Eliminate other rows
            for (k in 0 until n) {
                if (k != i) {
                    val factor = a[k][i]
                    for (j in 0 until n) {
                        a[k][j] -= factor * a[i][j]
                        inv[k][j] -= factor * inv[i][j]
                    }
                }
            }
        }

        return Matrix(n, n, inv.map { it.toList() })
    }

    companion object {
        fun formatMatrix(m: Matrix): String {
            return "[" + m.data.joinToString(", ") { row ->
                "[" + row.joinToString(", ") { value ->
                    val formatted = "%.4f".format(value).trimEnd('0').trimEnd('.')
                    if (formatted == "-0") "0" else formatted
                } + "]"
            } + "]"
        }
    }
}
