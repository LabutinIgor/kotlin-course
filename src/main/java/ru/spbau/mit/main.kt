package ru.spbau.mit

import java.io.PrintWriter
import java.util.*
import kotlin.collections.ArrayList

data class Vector(private val x: Long, private val y: Long) : Comparable<Vector> {
    override fun compareTo(other: Vector): Int = when {
        x == other.x && y == other.y -> 0
        y == 0L && other.y == 0L -> -x.compareTo(0)
        y == 0L && x > 0 -> -1
        other.y == 0L && other.x > 0 -> 1
        else -> {
            val thisHalfPlane = -y.compareTo(0)
            val otherHalfPlane = -other.y.compareTo(0)
            if (thisHalfPlane == otherHalfPlane) {
                -cross(other).compareTo(0)
            } else {
                thisHalfPlane.compareTo(otherHalfPlane)
            }
        }
    }

    fun dot(other: Vector) = x * other.x + y * other.y
    fun cross(other: Vector) = x * other.y - y * other.x
}

data class VectorWithId(val vector: Vector, val id: Int) : Comparable<VectorWithId> {
    override fun compareTo(other: VectorWithId): Int = vector.compareTo(other.vector)
}

fun readData(): List<VectorWithId> {
    val data = ArrayList<VectorWithId>()
    val scanner = Scanner(System.`in`)
    val n = scanner.nextInt()
    for (i in 1..n) {
        val x = scanner.nextLong()
        val y = scanner.nextLong()
        data.add(VectorWithId(Vector(x, y), i))
    }
    scanner.close()
    return data
}

fun writeAnswer(ids: Pair<Int, Int>) {
    val printWriter = PrintWriter(System.out)
    printWriter.println(ids.first.toString() + " " + ids.second + "\n")
    printWriter.close()
}

fun solve(data: List<VectorWithId>): Pair<Int, Int> {
    val sortedPoints = data.sorted()
    val vectors: MutableList<Vector> = mutableListOf()
    for (i in 0 until sortedPoints.size) {
        val p1 = sortedPoints[i].vector
        val p2 = sortedPoints[(i + 1) % sortedPoints.size].vector
        vectors.add(Vector(p1.dot(p2), Math.abs(p1.cross(p2))))
    }

    var resId = 0
    for (i in 0 until sortedPoints.size) {
        if (vectors[i].cross(vectors[resId]) > 0) {
            resId = i
        }
    }
    return Pair(sortedPoints[resId].id, sortedPoints[(resId + 1) % sortedPoints.size].id)
}

fun main(args: Array<String>) {
    val data = readData()
    val answer = solve(data)
    writeAnswer(answer)
}
