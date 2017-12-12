package ru.spbau.mit

import org.antlr.v4.runtime.CharStream
import org.antlr.v4.runtime.CharStreams
import org.antlr.v4.runtime.CommonTokenStream
import org.junit.Test
import ru.spbau.mit.parser.FunLexer
import ru.spbau.mit.parser.FunParser
import java.io.PipedInputStream
import java.io.PipedOutputStream
import java.io.PrintStream
import java.util.*
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class TestInterpreter {
    @Test
    fun testSimpleProgramWithIf() {
        val charStream: CharStream = CharStreams.fromString("""
            |var a = 10
            |var b = 20
            |if (a > b) {
            |    println(1)
            |} else {
            |    println(0)
            |}
            """.trimMargin())

        val lexer = FunLexer(charStream)
        val tokens = CommonTokenStream(lexer)
        val parser = FunParser(tokens)
        val pipedOutputStream = PipedOutputStream()
        val out: java.io.PrintStream = PrintStream(pipedOutputStream)
        val pipedInputStream = PipedInputStream(pipedOutputStream)
        val inputScanner = Scanner(pipedInputStream)

        val funInterpreter = FunInterpreter(out = out)
        funInterpreter.visitFile(parser.file())

        out.close()
        assertTrue(inputScanner.hasNext())
        assertEquals(0, inputScanner.nextInt())
        assertFalse(inputScanner.hasNext())
    }

    @Test
    fun testProgramWithFunctionAndWhile() {
        val charStream: CharStream = CharStreams.fromString("""
            |fun fib(n) {
            |        if (n <= 1) {
            |            return 1
            |        }
            |        return fib(n - 1) + fib(n - 2)
            |    }
            |
            |    var i = 1
            |    while (i <= 5) {
            |        println(i, fib(i))
            |        i = i + 1
            |    }
            """.trimMargin())

        val lexer = FunLexer(charStream)
        val tokens = CommonTokenStream(lexer)
        val parser = FunParser(tokens)

        val pipedOutputStream = PipedOutputStream()
        val out: java.io.PrintStream = PrintStream(pipedOutputStream)
        val pipedInputStream = PipedInputStream(pipedOutputStream)
        val inputScanner = Scanner(pipedInputStream)

        val funInterpreter = FunInterpreter(out = out)
        funInterpreter.visitFile(parser.file())

        out.close()
        assertTrue(inputScanner.hasNext())
        assertEquals(1, inputScanner.nextInt())
        assertEquals(1, inputScanner.nextInt())

        assertEquals(2, inputScanner.nextInt())
        assertEquals(2, inputScanner.nextInt())

        assertEquals(3, inputScanner.nextInt())
        assertEquals(3, inputScanner.nextInt())

        assertEquals(4, inputScanner.nextInt())
        assertEquals(5, inputScanner.nextInt())

        assertEquals(5, inputScanner.nextInt())
        assertEquals(8, inputScanner.nextInt())
        assertFalse(inputScanner.hasNext())
    }

    @Test(expected = FunException::class)
    fun testProgramWithIncorrectNumber() {
        val charStream: CharStream = CharStreams.fromString("""
            |var a = 10
            |var b = 20000000000 //Overflow
            |if (a > b) {
            |    println(1)
            |} else {
            |    println(0)
            |}
            """.trimMargin())

        val lexer = FunLexer(charStream)
        lexer.removeErrorListeners()
        lexer.addErrorListener(ThrowingErrorListener)
        val tokens = CommonTokenStream(lexer)
        val parser = FunParser(tokens)
        parser.removeErrorListeners()
        parser.addErrorListener(ThrowingErrorListener)

        val funInterpreter = FunInterpreter()
        funInterpreter.visitFile(parser.file())
    }

    @Test(expected = FunException::class)
    fun testProgramWithRedefinedVariable() {
        val charStream: CharStream = CharStreams.fromString("""
            |var a = 10
            |var a = 200 //Redefined
            |println(a)
            """.trimMargin())

        val lexer = FunLexer(charStream)
        lexer.removeErrorListeners()
        lexer.addErrorListener(ThrowingErrorListener)
        val tokens = CommonTokenStream(lexer)
        val parser = FunParser(tokens)
        parser.removeErrorListeners()
        parser.addErrorListener(ThrowingErrorListener)

        val funInterpreter = FunInterpreter()
        funInterpreter.visitFile(parser.file())
    }
}