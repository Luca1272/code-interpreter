import scanning.Token
import scanning.TokenType
import scanning.Tokenizer
import java.io.File
import java.util.*
import kotlin.system.exitProcess
fun main(args: Array<String>) {
    if (args.size < 2) {
        System.err.println("Usage: ./your_program.sh tokenize <filename>")
        exitProcess(1)
    }
    val command = args[0]
    val filename = args[1]
    when (command) {
        "tokenize" -> {
            tokenize(filename, true)
        }
        "parse" -> {
            val tokens = tokenize(filename, false)
            val parser = Parser(tokens)
            parser.parse()
        }
        else -> {
            System.err.println("Unknown command: $command")
            exitProcess(1)
        }
    }
}
private fun tokenize(filename: String, print: Boolean): LinkedList<Token> {
    val fileContents = File(filename).readText()
    val tokenizer = Tokenizer(fileContents)
    var errCount = 0
    val tokens = LinkedList<Token>()
    while (true) {
        val token = tokenizer.nextToken()
        if (token.type != TokenType.ERR) {
            if (token.type != TokenType.COMMENT && token.type != TokenType.WHITESPACE) {
                if (print) {
                    println(token)
                }
                tokens.add(token)
            }
        } else {
            System.err.println("[line ${token.errLine}] Error: ${token.errMsg}")
            errCount++
        }
        if (token.type == TokenType.EOF) {
            break
        }
    }
    if (errCount > 0) {
        exitProcess(65)
    }
    return tokens
}