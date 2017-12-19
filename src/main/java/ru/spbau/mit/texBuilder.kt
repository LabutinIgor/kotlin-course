package ru.spbau.mit

import java.io.OutputStream
import java.io.OutputStreamWriter
import java.io.StringWriter
import java.io.Writer


interface Element {
    fun write(out: Writer, indent: String)
}

class TextElement(private val text: String) : Element {
    override fun write(out: Writer, indent: String) {
        val indentedText = text.replace("\n", "\n" + indent)
        out.write("$indent$indentedText\n")
    }
}

@DslMarker
annotation class TexCommandMarker

@TexCommandMarker
abstract class Command(val name: String,
                       private val simpleAttributes: List<String> = listOf(),
                       private val attributesWithValue: Map<String, String> = mapOf()) : Element {
    val children = arrayListOf<Element>()

    protected fun <T : Element> initCommand(command: T, init: T.() -> Unit): T {
        command.init()
        children.add(command)
        return command
    }

    override fun toString(): String {
        val writer = StringWriter()
        write(writer, "")
        return writer.toString()

    }

    fun toOutputStream(stream: OutputStream) {
        OutputStreamWriter(stream).use {
            write(it, "")
        }
    }

    fun writeAttributes(out: Writer): String {
        val attributesToPrint: List<String> = simpleAttributes +
                attributesWithValue.map { it.key + "=" + it.value }.toList()
        if (attributesToPrint.isNotEmpty()) {
            out.write(attributesToPrint.joinToString(", ", "[", "]"))
        }
        return out.toString()
    }

    fun writeChildren(out: Writer, indent: String) {
        for (c in children) {
            c.write(out, indent)
        }
    }

    operator fun String.unaryPlus() {
        children.add(TextElement(this))
    }

    fun itemize(init: Itemize.() -> Unit) = initCommand(Itemize(), init)

    fun enumerate(init: Enumerate.() -> Unit) = initCommand(Enumerate(), init)

    fun customCommand(name: String,
                      simpleAttributes: List<String> = listOf(),
                      attributesWithValue: Map<String, String> = mapOf(),
                      init: CustomCommand.() -> Unit) {
        initCommand(CustomCommand(name, simpleAttributes, attributesWithValue), init)
    }

    fun customCommand(name: String,
                      value: String,
                      simpleAttributes: List<String> = listOf(),
                      attributesWithValue: Map<String, String> = mapOf()) {
        initCommand(CustomOneLineCommand(name, value, simpleAttributes, attributesWithValue), {})
    }

    fun math(init: Math.() -> Unit) {
        initCommand(Math(), init)
    }

    fun center(init: Center.() -> Unit) {
        initCommand(Center(), init)
    }

    fun flushLeft(init: FlushLeft.() -> Unit) {
        initCommand(FlushLeft(), init)
    }

    fun flushRight(init: FlushRight.() -> Unit) {
        initCommand(FlushRight(), init)
    }
}

abstract class MultiLineCommand(name: String,
                                simpleAttributes: List<String> = listOf(),
                                attributesWithValue: Map<String, String> = mapOf()) :
        Command(name, simpleAttributes, attributesWithValue) {

    override fun write(out: Writer, indent: String) {
        out.write("$indent\\begin{$name}")
        writeAttributes(out)
        out.write("\n")
        writeChildren(out, indent + "  ")
        out.write("$indent\\end{$name}\n")
    }
}

abstract class OneLineCommand(name: String,
                              private val value: String,
                              simpleAttributes: List<String> = listOf(),
                              attributesWithValue: Map<String, String> = mapOf()) :
        Command(name, simpleAttributes, attributesWithValue) {

    override fun write(out: Writer, indent: String) {
        out.write("$indent\\$name")
        writeAttributes(out)
        if (value != "") {
            out.write("{$value}")
        }
        out.write("\n")
        writeChildren(out, indent + "  ")
    }
}

class Tex : Command("") {
    override fun write(out: Writer, indent: String) {
        writeChildren(out, indent)
    }

    fun documentClass(value: String,
                      simpleAttributes: List<String> = listOf(),
                      attributesWithValue: Map<String, String> = mapOf()) {
        initCommand(DocumentClass(value, simpleAttributes, attributesWithValue), {})
    }

    fun usePackage(value: String,
                   simpleAttributes: List<String> = listOf(),
                   attributesWithValue: Map<String, String> = mapOf()) {
        initCommand(UsePackage(value, simpleAttributes, attributesWithValue), {})
    }

    fun document(init: Document.() -> Unit) {
        initCommand(Document(), init)
    }
}

class Document : MultiLineCommand("document") {
    fun frame(frameTitle: String,
              simpleAttributes: List<String> = listOf(),
              attributesWithValue: Map<String, String> = mapOf(),
              init: Frame.() -> Unit) {
        val frame = initCommand(Frame(simpleAttributes, attributesWithValue), init)
        frame.children.add(0, FrameTitle(frameTitle))
    }
}

class FrameTitle(value: String) : OneLineCommand("frametitle", value)

class DocumentClass(value: String, simpleAttributes: List<String>, attributesWithValue: Map<String, String>) :
        OneLineCommand("documentclass", value, simpleAttributes, attributesWithValue)

class UsePackage(value: String, simpleAttributes: List<String>, attributesWithValue: Map<String, String>) :
        OneLineCommand("usepackage", value, simpleAttributes, attributesWithValue)

class Frame(simpleAttributes: List<String>, attributesWithValue: Map<String, String>) :
        MultiLineCommand("frame", simpleAttributes, attributesWithValue)

class CustomCommand(name: String, simpleAttributes: List<String>, attributesWithValue: Map<String, String>) :
        MultiLineCommand(name, simpleAttributes, attributesWithValue)

class CustomOneLineCommand(name: String,
                           value: String,
                           simpleAttributes: List<String>,
                           attributesWithValue: Map<String, String>) :
        OneLineCommand(name, value, simpleAttributes, attributesWithValue)

abstract class CommandWithItem(name: String) : MultiLineCommand(name) {
    fun item(value: String = "", simpleAttributes: List<String> = listOf(),
             attributesWithValue: Map<String, String> = mapOf(), init: Item.() -> Unit) {
        initCommand(Item(value, simpleAttributes, attributesWithValue), init)
    }
}

class Itemize : CommandWithItem("itemize")

class Enumerate : CommandWithItem("enumerate")

class Item(value: String, simpleAttributes: List<String>, attributesWithValue: Map<String, String>) :
        OneLineCommand("item", value, simpleAttributes, attributesWithValue)

class Math : MultiLineCommand("math")

class Center : MultiLineCommand("center")

class FlushLeft : MultiLineCommand("flushleft")

class FlushRight : MultiLineCommand("flushright")

fun tex(init: Tex.() -> Unit): Tex = Tex().apply(init)

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
