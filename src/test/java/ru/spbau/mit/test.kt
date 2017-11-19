package ru.spbau.mit

import org.junit.Test
import java.io.PipedInputStream
import java.io.PipedOutputStream
import java.io.PrintStream
import java.util.*
import kotlin.test.assertEquals
import kotlin.test.assertFalse

class TestTexBuilder {
    @Test
    fun testPreamble() {
        val res =
                tex {
                    documentClass("beamer")
                    usePackage("babel", listOf("russian", "english"))
                    usePackage("caption", listOf(), mapOf("labelsep" to "period"))
                }.toString()
        assertEquals("""
                    |\documentclass{beamer}
                    |\usepackage[russian, english]{babel}
                    |\usepackage[labelsep=period]{caption}
                    |""".trimMargin(), res)
    }

    @Test
    fun testPrintToStream() {
        val pipedOutputStream = PipedOutputStream()
        val out: java.io.PrintStream = PrintStream(pipedOutputStream)
        val pipedInputStream = PipedInputStream(pipedOutputStream)
        val inputScanner = Scanner(pipedInputStream)
        tex {
            documentClass("beamer")
            usePackage("babel", listOf("russian", "english"))
            usePackage("caption", listOf(), mapOf("labelsep" to "period"))
        }.toOutputStream(out)

        assertEquals("\\documentclass{beamer}", inputScanner.nextLine())
        assertEquals("\\usepackage[russian, english]{babel}", inputScanner.nextLine())
        assertEquals("\\usepackage[labelsep=period]{caption}", inputScanner.nextLine())
        assertFalse(inputScanner.hasNext())
    }

    @Test
    fun testSimpleDocumentWithFrame() {
        val res =
                tex {
                    documentClass("beamer")
                    usePackage("babel", listOf("russian", "english"))
                    document {
                        frame(frameTitle = "Frame01") {
                            +"a"
                        }
                        frame(frameTitle = "Frame02") {
                            +"b"
                            +"c"
                        }
                    }
                }.toString()
        assertEquals("""
                    |\documentclass{beamer}
                    |\usepackage[russian, english]{babel}
                    |\begin{document}
                    |  \begin{frame}
                    |    \frametitle{Frame01}
                    |    a
                    |  \end{frame}
                    |  \begin{frame}
                    |    \frametitle{Frame02}
                    |    b
                    |    c
                    |  \end{frame}
                    |\end{document}
                    |""".trimMargin(), res)
    }

    @Test
    fun testCommandsWithItems() {
        val rows = listOf("first", "second")
        val res =
                tex {
                    documentClass("beamer")
                    usePackage("babel", listOf("russian", "english"))
                    document {
                        frame(frameTitle = "Frame01") {
                            enumerate {
                                item { +"1" }
                                item { +"2" }
                            }
                        }
                        frame(frameTitle = "Frame02") {
                            itemize {
                                for (row in rows) {
                                    item {
                                        +"$row text"
                                    }
                                }
                            }
                        }
                    }
                }.toString()
        assertEquals("""
                    |\documentclass{beamer}
                    |\usepackage[russian, english]{babel}
                    |\begin{document}
                    |  \begin{frame}
                    |    \frametitle{Frame01}
                    |    \begin{enumerate}
                    |      \item
                    |        1
                    |      \item
                    |        2
                    |    \end{enumerate}
                    |  \end{frame}
                    |  \begin{frame}
                    |    \frametitle{Frame02}
                    |    \begin{itemize}
                    |      \item
                    |        first text
                    |      \item
                    |        second text
                    |    \end{itemize}
                    |  \end{frame}
                    |\end{document}
                    |""".trimMargin(), res)
    }

    @Test
    fun testMathAndAlignment() {
        val res =
                tex {
                    documentClass("beamer")
                    usePackage("babel", listOf("russian", "english"))
                    document {
                        frame(frameTitle = "Frame01") {
                            flushLeft {
                                +"left"
                            }
                            center {
                                +"center"
                            }
                            flushRight {
                                math {
                                    +"1 + \\frac 2 3"
                                }
                            }
                        }
                    }
                }.toString()
        assertEquals("""
                    |\documentclass{beamer}
                    |\usepackage[russian, english]{babel}
                    |\begin{document}
                    |  \begin{frame}
                    |    \frametitle{Frame01}
                    |    \begin{flushleft}
                    |      left
                    |    \end{flushleft}
                    |    \begin{center}
                    |      center
                    |    \end{center}
                    |    \begin{flushright}
                    |      \begin{math}
                    |        1 + \frac 2 3
                    |      \end{math}
                    |    \end{flushright}
                    |  \end{frame}
                    |\end{document}
                    |""".trimMargin(), res)
    }

    @Test
    fun testCustomCommand() {
        val res =
                tex {
                    documentClass("beamer")
                    usePackage("babel", listOf("russian", "english"))
                    document {
                        frame(frameTitle = "Title") {
                            customCommand(name = "pyglist", attributesWithValue = mapOf("language" to "kotlin")) {
                                +"""
                                   |fun main(args: Array<String>) {
                                   |    val a = 1
                                   |}
                                """.trimMargin()
                            }
                        }
                    }
                }.toString()
        assertEquals("""
                    |\documentclass{beamer}
                    |\usepackage[russian, english]{babel}
                    |\begin{document}
                    |  \begin{frame}
                    |    \frametitle{Title}
                    |    \begin{pyglist}[language=kotlin]
                    |      fun main(args: Array<String>) {
                    |          val a = 1
                    |      }
                    |    \end{pyglist}
                    |  \end{frame}
                    |\end{document}
                    |""".trimMargin(), res)
    }

    @Test
    fun testComplicatedDocument() {
        val rows = listOf("first", "second")
        val res =
                tex {
                    documentClass("beamer")
                    usePackage("babel", listOf("russian", "english"))
                    document {
                        frame(frameTitle = "Title") {
                            enumerate {
                                item { +"1" }
                                item { +"2" }
                                item {
                                    flushRight {
                                        math {
                                            +"1 + \\frac 2 3"
                                        }
                                    }
                                }
                            }
                            itemize {
                                for (row in rows) {
                                    item {
                                        +"$row text"
                                    }
                                }
                            }
                            //begin { pyglist }[language = kotlin]...\end{ pyglist }
                            customCommand(name = "pyglist", attributesWithValue = mapOf("language" to "kotlin")) {
                                +"""
                       |fun main(args: Array<String>) {
                       |    val a = 1
                       |}
                    """.trimMargin()
                            }
                        }
                    }
                }.toString()
        assertEquals("""
                    |\documentclass{beamer}
                    |\usepackage[russian, english]{babel}
                    |\begin{document}
                    |  \begin{frame}
                    |    \frametitle{Title}
                    |    \begin{enumerate}
                    |      \item
                    |        1
                    |      \item
                    |        2
                    |      \item
                    |        \begin{flushright}
                    |          \begin{math}
                    |            1 + \frac 2 3
                    |          \end{math}
                    |        \end{flushright}
                    |    \end{enumerate}
                    |    \begin{itemize}
                    |      \item
                    |        first text
                    |      \item
                    |        second text
                    |    \end{itemize}
                    |    \begin{pyglist}[language=kotlin]
                    |      fun main(args: Array<String>) {
                    |          val a = 1
                    |      }
                    |    \end{pyglist}
                    |  \end{frame}
                    |\end{document}
                    |""".trimMargin(), res)
    }
}
