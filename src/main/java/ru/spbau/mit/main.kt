package ru.spbau.mit

import java.io.PrintWriter
import java.util.*
import kotlin.collections.ArrayList

data class Vector(private val x: Long, private val y: Long, val id: Int? = null) : Comparable<Vector> {
    override fun compareTo(other: Vector): Int {
        if (this.x == other.x && this.y == other.y) {
            return 0
        }
        if (y == 0L && other.y == 0L) {
            return -x.compareTo(0)
        }
        if (y == 0L && x > 0) {
            return -1
        }
        if (other.y == 0L && other.x > 0) {
            return 1
        }

        val thisHalfPlane = -y.compareTo(0)
        val otherHalfPlane = -other.y.compareTo(0)
        return if (thisHalfPlane == otherHalfPlane) {
            -this.cross(other).compareTo(0)
        } else {
            thisHalfPlane.compareTo(otherHalfPlane)
        }
    }

    fun dot(other: Vector) = x * other.x + y * other.y
    fun cross(other: Vector) = x * other.y - y * other.x
}

class InputReader {
    fun readData(): ArrayList<Vector> {
        val data = ArrayList<Vector>()
        val scanner = Scanner(System.`in`)
        val n = scanner.nextInt()
        for (i in 1..n) {
            val x = scanner.nextLong()
            val y = scanner.nextLong()
            data.add(Vector(x, y, i))
        }
        scanner.close()
        return data
    }
}

class OutputWriter {
    fun writeAnswer(ids: Pair<Int, Int>) {
        val printWriter = PrintWriter(System.out)
        printWriter.println(ids.first.toString() + " " + ids.second + "\n")
        printWriter.close()
    }
}

class Solution {
    fun solve(data: ArrayList<Vector>): Pair<Int, Int> {
        val sortedPoints = data.sorted()
        var resId = 0
        (0 until sortedPoints.size).asSequence().filter {
                    angleLess(sortedPoints[it], sortedPoints[(it + 1) % sortedPoints.size],
                            sortedPoints[resId], sortedPoints[(resId + 1) % sortedPoints.size])
                }
                .forEach { resId = it }
        return Pair(sortedPoints[resId].id!!, sortedPoints[(resId + 1) % sortedPoints.size].id!!)
    }

    private fun angleLess(firstAngle1: Vector, firstAngle2: Vector,
                          secondAngle1: Vector, secondAngle2: Vector): Boolean {
        val p1 = Vector(firstAngle1.dot(firstAngle2), Math.abs(firstAngle1.cross(firstAngle2)))
        val p2 = Vector(secondAngle1.dot(secondAngle2), Math.abs(secondAngle1.cross(secondAngle2)))
        return p1.cross(p2) > 0
    }
}

fun main(args: Array<String>) {
    val reader = InputReader()
    val data = reader.readData()

    val solution = Solution()
    val answer = solution.solve(data)

    val writer = OutputWriter()
    writer.writeAnswer(answer)
}
