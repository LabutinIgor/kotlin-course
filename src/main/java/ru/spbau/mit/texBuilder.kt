package ru.spbau.mit

import java.io.OutputStream
import java.io.OutputStreamWriter
import java.io.StringWriter
import java.io.Writer


abstract class Element(val out: Writer, val indent: String)

@DslMarker
annotation class TexCommandMarker

@TexCommandMarker
abstract class Command(val name: String,
                       out: Writer,
                       indent: String,
                       private val simpleAttributes: List<String> = listOf(),
                       private val attributesWithValue: Map<String, String> = mapOf()) : Element(out, indent) {
    fun writeAttributes() {
        val attributesToPrint: List<String> = simpleAttributes +
                attributesWithValue.map { it.key + "=" + it.value }
        if (attributesToPrint.isNotEmpty()) {
            out.write(attributesToPrint.joinToString(", ", "[", "]"))
        }
    }

    operator fun String.unaryPlus() {
        val indentedText = this.replace("\n", "\n" + indent + "  ")
        out.write("$indent  $indentedText\n")
    }

    fun itemize(init: Itemize.() -> Unit) {
        MultiLineCommand.writeCommand(Itemize(out, indent + "  "), init)
    }

    fun enumerate(init: Enumerate.() -> Unit) {
        MultiLineCommand.writeCommand(Enumerate(out, indent + "  "), init)
    }

    fun customCommand(name: String,
                      simpleAttributes: List<String> = listOf(),
                      attributesWithValue: Map<String, String> = mapOf(),
                      init: CustomCommand.() -> Unit) {
        MultiLineCommand.writeCommand(CustomCommand(name, out, indent + "  ", simpleAttributes,
                attributesWithValue), init)
    }

    fun customCommand(name: String,
                      value: String,
                      simpleAttributes: List<String> = listOf(),
                      attributesWithValue: Map<String, String> = mapOf()) {
        CustomOneLineCommand(name, out, indent + "  ", value, simpleAttributes, attributesWithValue).write()
    }

    fun math(init: Math.() -> Unit) {
        MultiLineCommand.writeCommand(Math(out, indent + "  "), init)
    }

    fun center(init: Center.() -> Unit) {
        MultiLineCommand.writeCommand(Center(out, indent + "  "), init)
    }

    fun flushLeft(init: FlushLeft.() -> Unit) {
        MultiLineCommand.writeCommand(FlushLeft(out, indent + "  "), init)
    }

    fun flushRight(init: FlushRight.() -> Unit) {
        MultiLineCommand.writeCommand(FlushRight(out, indent + "  "), init)
    }
}

abstract class MultiLineCommand(name: String,
                                out: Writer,
                                indent: String,
                                simpleAttributes: List<String> = listOf(),
                                attributesWithValue: Map<String, String> = mapOf()) :
        Command(name, out, indent, simpleAttributes, attributesWithValue) {

    fun writeStart() {
        out.write("$indent\\begin{$name}")
        writeAttributes()
        out.write("\n")
    }

    fun writeEnd() {
        out.write("$indent\\end{$name}\n")
    }

    companion object {
        fun <T : MultiLineCommand> writeCommand(command: T, init: T.() -> Unit) {
            command.writeStart()
            command.init()
            command.writeEnd()
        }
    }
}

abstract class OneLineCommand(name: String,
                              out: Writer,
                              indent: String,
                              private val value: String,
                              simpleAttributes: List<String> = listOf(),
                              attributesWithValue: Map<String, String> = mapOf()) :
        Command(name, out, indent, simpleAttributes, attributesWithValue) {

    fun write() {
        out.write("$indent\\$name")
        writeAttributes()
        if (value != "") {
            out.write("{$value}")
        }
        out.write("\n")
    }

    companion object {
        fun <T : OneLineCommand> writeCommand(command: T, init: T.() -> Unit) {
            command.write()
            command.init()
        }
    }
}

class TexWriter(private val init: Tex.() -> Unit) {
    override fun toString(): String {
        val out = StringWriter()
        Tex(out).init()
        return out.toString()
    }

    fun toOutputStream(out: OutputStream) {
        OutputStreamWriter(out).use {
            Tex(it).init()
        }
    }
}

class Tex(out: Writer, indent: String = "") :
        Command("", out, indent) {
    fun documentClass(value: String,
                      simpleAttributes: List<String> = listOf(),
                      attributesWithValue: Map<String, String> = mapOf()) {
        DocumentClass(out, indent, value, simpleAttributes, attributesWithValue).write()
    }

    fun usePackage(value: String,
                   simpleAttributes: List<String> = listOf(),
                   attributesWithValue: Map<String, String> = mapOf()) {
        UsePackage(out, indent, value, simpleAttributes, attributesWithValue).write()
    }

    fun document(init: Document.() -> Unit) {
        MultiLineCommand.writeCommand(Document(out, indent), init)
    }
}

class Document(out: Writer, indent: String = "") : MultiLineCommand("document", out, indent) {
    fun frame(frameTitle: String,
              simpleAttributes: List<String> = listOf(),
              attributesWithValue: Map<String, String> = mapOf(),
              init: Frame.() -> Unit) {
        val child = Frame(out, indent + "  ", simpleAttributes, attributesWithValue)
        child.writeStart()
        FrameTitle(out, indent + "    ", frameTitle).write()
        child.init()
        child.writeEnd()
    }
}

class DocumentClass(out: Writer,
                    indent: String = "",
                    value: String,
                    simpleAttributes: List<String>,
                    attributesWithValue: Map<String, String>) :
        OneLineCommand("documentclass", out, indent, value, simpleAttributes, attributesWithValue)

class UsePackage(out: Writer,
                 indent: String = "",
                 value: String,
                 simpleAttributes: List<String>,
                 attributesWithValue: Map<String, String>) :
        OneLineCommand("usepackage", out, indent, value, simpleAttributes, attributesWithValue)

class FrameTitle(out: Writer, indent: String = "", value: String) : OneLineCommand("frametitle", out, indent, value)

class Frame(out: Writer,
            indent: String = "",
            simpleAttributes: List<String>,
            attributesWithValue: Map<String, String>) :
        MultiLineCommand("frame", out, indent, simpleAttributes, attributesWithValue)

class CustomCommand(name: String,
                    out: Writer,
                    indent: String = "",
                    simpleAttributes: List<String>,
                    attributesWithValue: Map<String, String>) :
        MultiLineCommand(name, out, indent, simpleAttributes, attributesWithValue)

class CustomOneLineCommand(name: String,
                           out: Writer,
                           indent: String = "",
                           value: String,
                           simpleAttributes: List<String>,
                           attributesWithValue: Map<String, String>) :
        OneLineCommand(name, out, indent, value, simpleAttributes, attributesWithValue)

abstract class CommandWithItem(name: String, out: Writer, indent: String = "") : MultiLineCommand(name, out, indent) {
    fun item(value: String = "",
             simpleAttributes: List<String> = listOf(),
             attributesWithValue: Map<String, String> = mapOf(), init: Item.() -> Unit) {
        OneLineCommand.writeCommand(Item(out, indent + "  ", value, simpleAttributes, attributesWithValue),
                init)
    }
}

class Itemize(out: Writer, indent: String = "") : CommandWithItem("itemize", out, indent)

class Enumerate(out: Writer, indent: String = "") : CommandWithItem("enumerate", out, indent)

class Item(out: Writer,
           indent: String = "",
           value: String,
           simpleAttributes: List<String>,
           attributesWithValue: Map<String, String>) :
        OneLineCommand("item", out, indent, value, simpleAttributes, attributesWithValue)

class Math(out: Writer, indent: String = "") : MultiLineCommand("math", out, indent)

class Center(out: Writer, indent: String = "") : MultiLineCommand("center", out, indent)

class FlushLeft(out: Writer, indent: String = "") : MultiLineCommand("flushleft", out, indent)

class FlushRight(out: Writer, indent: String = "") : MultiLineCommand("flushright", out, indent)

fun tex(init: Tex.() -> Unit): TexWriter = TexWriter(init)

fun main(args: Array<String>) {
    val rows = listOf("first", "second")

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
//                begin { pyglist }[language = kotlin]...\end{ pyglist }
                customCommand(name = "pyglist", attributesWithValue = mapOf("language" to "kotlin")) {
                    +"""
                       |fun main(args: Array<String>) {
                       |    val a = 1
                       |}
                    """.trimMargin()
                }
//                \foobar{fizz buzz}
                customCommand(name = "foobar", value = "fizz buzz")
            }
        }
    }.toOutputStream(System.out)
}
