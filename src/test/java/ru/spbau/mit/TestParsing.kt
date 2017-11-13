package ru.spbau.mit

import org.antlr.v4.runtime.CharStream
import org.antlr.v4.runtime.CharStreams
import org.antlr.v4.runtime.CommonTokenStream
import org.antlr.v4.runtime.misc.ParseCancellationException
import org.junit.Test
import ru.spbau.mit.parser.FunLexer
import ru.spbau.mit.parser.FunParser
import kotlin.test.assertEquals

class TestParsing {
    @Test
    fun testParsingSimpleProgram() {
        val charStream: CharStream = CharStreams.fromString("var a = 10\n" +
                "var b = 0\n" +
                "if (a > b) {\n" +
                "    println(1)\n" +
                "} else {\n" +
                "    println(0)\n" +
                "}")

        val lexer = FunLexer(charStream)
        lexer.removeErrorListeners()
        lexer.addErrorListener(ThrowingErrorListener.INSTANCE)

        val tokens = CommonTokenStream(lexer)

        val parser = FunParser(tokens)
        parser.removeErrorListeners()
        parser.addErrorListener(ThrowingErrorListener.INSTANCE)

        val fileContext = parser.file()!!
        assertEquals(3, fileContext.block().statement().size)
        assertEquals("a", fileContext.block().statement(0).variable().IDENTIFIER().text)
        assertEquals("10", fileContext.block().statement(0).variable().expression().text)
        assertEquals("b", fileContext.block().statement(1).variable().IDENTIFIER().text)
        assertEquals("0", fileContext.block().statement(1).variable().expression().text)
        assertEquals(2, fileContext.block().statement(2).ifStatement().blockWithBraces().size)
    }

    @Test(expected = ParseCancellationException::class)
    fun testParsingProgramWithSyntaxError() {
        val charStream: CharStream = CharStreams.fromString("var a = 10.\n" +
                "var b = 0\n")

        val lexer = FunLexer(charStream)
        lexer.removeErrorListeners()
        lexer.addErrorListener(ThrowingErrorListener.INSTANCE)

        val tokens = CommonTokenStream(lexer)

        val parser = FunParser(tokens)
        parser.removeErrorListeners()
        parser.addErrorListener(ThrowingErrorListener.INSTANCE)
        parser.file()!!
    }
}