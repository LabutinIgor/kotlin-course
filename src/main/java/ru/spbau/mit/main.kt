package ru.spbau.mit

import org.antlr.v4.runtime.CharStream
import org.antlr.v4.runtime.CharStreams
import org.antlr.v4.runtime.CommonTokenStream
import ru.spbau.mit.parser.FunLexer
import ru.spbau.mit.parser.FunParser
import org.antlr.v4.runtime.misc.ParseCancellationException


fun main(args: Array<String>) {
    if (args.size != 1) {
        println("Usage:" +
                "./main filename")
        System.exit(1)
    }

    try {
        val charStream: CharStream = CharStreams.fromFileName(args[0])
        val lexer = FunLexer(charStream)
        lexer.removeErrorListeners()
        lexer.addErrorListener(ThrowingErrorListener.INSTANCE)

        val tokens = CommonTokenStream(lexer)

        val parser = FunParser(tokens)
        parser.removeErrorListeners()
        parser.addErrorListener(ThrowingErrorListener.INSTANCE)

        val funInterpreter = FunInterpreter()
        funInterpreter.visitFile(parser.file())
    } catch (e: java.nio.file.NoSuchFileException) {
        println("File not found")
        System.exit(1)
    } catch (e: ParseCancellationException) {
        println("Syntax error: " + e.message)
        System.exit(1)
    } catch (e: FunException) {
        if (e.line != null) {
            println("Error: " + e.message + " in line " + e.line)
        } else {
            println("Error: " + e.message)
        }
        System.exit(1)
    } catch (e: Exception) {
        println("Something went wrong: " + e.message)
        System.exit(1)
    }
}
