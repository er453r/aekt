package com.er453r.ae

import mu.KotlinLogging
import org.intellij.markdown.IElementType
import org.intellij.markdown.MarkdownElementTypes
import org.intellij.markdown.MarkdownTokenTypes
import org.intellij.markdown.ast.ASTNode
import org.intellij.markdown.ast.CompositeASTNode
import org.intellij.markdown.ast.getTextInNode
import org.intellij.markdown.flavours.commonmark.CommonMarkFlavourDescriptor
import org.intellij.markdown.html.HtmlGenerator
import org.intellij.markdown.parser.MarkdownParser

class Parser {
    private val log = KotlinLogging.logger {}

    init {
        val src = getResourceAsText("/test-script.md")
        val flavour = CommonMarkFlavourDescriptor()
        val root = MarkdownParser(flavour).buildMarkdownTreeFromString(src)

        //visitNode(root, src)

        walk(root) { node, depth ->
            val text = node.getTextInNode(src).take(10)

            println("-".repeat(depth) + " ${node.type} - $text")

            true
        }

        walk(root) { node, depth ->
            when (node.type) {
                MarkdownElementTypes.ATX_1 -> {
                    println("Processing TITLE...")

                    val title = node.findFirst(MarkdownTokenTypes.ATX_CONTENT)?.getTextInNode(src)

                    println("Title is $title")
                }
                MarkdownElementTypes.ATX_2 -> {
                    println("Processing SECTION...")

                    val text = node.findFirst(MarkdownTokenTypes.ATX_CONTENT)?.getTextInNode(src)

                    println("processing $text")

                    walk(node) { node, depth ->
                        when (node.type) {
                            MarkdownElementTypes.ATX_3 -> {
                                println("Processing SUB-SECTION...")

                                val subsction = node.findFirst(MarkdownTokenTypes.ATX_CONTENT)?.getTextInNode(src)

                                println("SUB-SECTION is $subsction")
                            }
                        }

                        true
                    }

                    return@walk false
                }
                else -> {

                }
            }

            true
        }

        val html = HtmlGenerator(src, root, flavour).generateHtml()

        println("have html $html")
    }
}

fun getResourceAsText(path: String): String {
    return object {}.javaClass.getResource(path).readText()
}

fun walk(node: ASTNode, depth: Int = 1, action: (ASTNode, Int) -> Boolean) {
    val walkChildren = action(node, depth)

    if (walkChildren && node is CompositeASTNode)
        node.children.forEach {
            walk(it, depth + 1, action)
        }
}

fun ASTNode.findFirst(targetType: IElementType): ASTNode? {
    if (this.type.name == targetType.name)
        return this
    else if (this is CompositeASTNode)
        children.forEach { child ->
            child.findFirst(targetType)?.let { return it }
        }

    return null
}
