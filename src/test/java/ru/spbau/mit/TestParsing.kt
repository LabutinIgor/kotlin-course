package ru.spbau.mit

import org.antlr.v4.runtime.CharStream
import org.antlr.v4.runtime.CharStreams
import org.antlr.v4.runtime.CommonTokenStream
import org.junit.Test
import ru.spbau.mit.parser.FunLexer
import ru.spbau.mit.parser.FunParser
import kotlin.test.assertFalse

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
        val tokens = CommonTokenStream(lexer)
        val parser = FunParser(tokens)

        assert(parser.file() != null)
        assertFalse( parser.file().block().isEmpty)
    }
}