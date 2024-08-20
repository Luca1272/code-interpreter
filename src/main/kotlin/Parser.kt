import scanning.Token
import scanning.TokenType
import java.util.*
class Parser(val tokens: LinkedList<Token>) {
    fun parse() {
        while (true) {
            val first = tokens.poll() ?: break
            when (first.type) {
                TokenType.EOF -> {
                    break
                }
                TokenType.TRUE, TokenType.FALSE, TokenType.NIL -> {
                    println(first.type.toString().lowercase())
                }
                TokenType.NUMBER -> {
                    var value = first.lexeme
                    if (!value.contains(".")) {
                        value += ".0"
                    }
                    println(value)
                }
                TokenType.STRING -> {
                    println(first.lexeme)
                }
                else -> {
                }
            }
        }
    }
}