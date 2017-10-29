package ru.spbau.mit
import kotlin.test.assertEquals
import org.junit.Test

class TestSource {
    @Test
    fun testSample1() {
        val solution = Solution()
        val inputVectors = ArrayList<Vector>()
        inputVectors.add(Vector(-1, 0, 1))
        inputVectors.add(Vector(0, -1, 2))
        inputVectors.add(Vector(1, 0, 3))
        inputVectors.add(Vector(1, 1, 4))
        val res = solution.solve(inputVectors)
        assertEquals(res, Pair(3, 4))
    }

    @Test
    fun testSample2() {
        val solution = Solution()
        val inputVectors = ArrayList<Vector>()
        inputVectors.add(Vector(-1, 0, 1))
        inputVectors.add(Vector(0, -1, 2))
        inputVectors.add(Vector(1, 0, 3))
        inputVectors.add(Vector(1, 1, 4))
        inputVectors.add(Vector(-4, -5, 5))
        inputVectors.add(Vector(-4, -6, 6))
        val res = solution.solve(inputVectors)
        assertEquals(res, Pair(5, 6))
    }
}
