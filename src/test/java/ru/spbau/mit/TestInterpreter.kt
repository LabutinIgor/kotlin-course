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
        val charStream: CharStream = CharStreams.fromString("var a = 10\n" +
                "var b = 20\n" +
                "if (a > b) {\n" +
                "    println(1)\n" +
                "} else {\n" +
                "    println(0)\n" +
                "}")

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
        val charStream: CharStream = CharStreams.fromString("fun fib(n) {\n" +
                "    if (n <= 1) {\n" +
                "        return 1\n" +
                "    }\n" +
                "    return fib(n - 1) + fib(n - 2)\n" +
                "}\n" +
                "\n" +
                "var i = 1\n" +
                "while (i <= 5) {\n" +
                "    println(i, fib(i))\n" +
                "    i = i + 1\n" +
                "}")

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
        val charStream: CharStream = CharStreams.fromString("var a = 10\n" +
                "var b = 20000000000 //Overflow\n" +
                "if (a > b) {\n" +
                "    println(1)\n" +
                "} else {\n" +
                "    println(0)\n" +
                "}")

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
        val charStream: CharStream = CharStreams.fromString("var a = 10\n" +
                "var a = 200 //Redefined\n" +
                "println(a)\n")

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