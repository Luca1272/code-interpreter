package scanning
class Tokenizer(input: String) {
    private var remaining = input
    private var line = 1
    fun nextToken(): Token {
        if (remaining.isEmpty()) {
            return Token(TokenType.EOF, "", null)
        }
        return when (remaining[0]) {
            '(' -> {
                remaining = remaining.substring(1)
                Token(TokenType.LEFT_PAREN, "(", null)
            }
            ')' -> {
                remaining = remaining.substring(1)
                Token(TokenType.RIGHT_PAREN, ")", null)
            }
            '{' -> {
                remaining = remaining.substring(1)
                Token(TokenType.LEFT_BRACE, "{", null)
            }
            '}' -> {
                remaining = remaining.substring(1)
                Token(TokenType.RIGHT_BRACE, "}", null)
            }
            '*' -> {
                remaining = remaining.substring(1)
                Token(TokenType.STAR, "*", null)
            }
            '.' -> {
                remaining = remaining.substring(1)
                Token(TokenType.DOT, ".", null)
            }
            ',' -> {
                remaining = remaining.substring(1)
                Token(TokenType.COMMA, ",", null)
            }
            '+' -> {
                remaining = remaining.substring(1)
                Token(TokenType.PLUS, "+", null)
            }
            '-' -> {
                remaining = remaining.substring(1)
                Token(TokenType.MINUS, "-", null)
            }
            ':' -> {
                remaining = remaining.substring(1)
                Token(TokenType.COLON, ":", null)
            }
            ';' -> {
                remaining = remaining.substring(1)
                Token(TokenType.SEMICOLON, ";", null)
            }
            '=' -> {
                if (remaining.length >= 2 && remaining[1] == '=') {
                    remaining = remaining.substring(2)
                    Token(TokenType.EQUAL_EQUAL, "==", null)
                } else {
                    remaining = remaining.substring(1)
                    Token(TokenType.EQUAL, "=", null)
                }
            }
            '!' -> {
                if (remaining.length >= 2 && remaining[1] == '=') {
                    remaining = remaining.substring(2)
                    Token(TokenType.BANG_EQUAL, "!=", null)
                } else {
                    remaining = remaining.substring(1)
                    Token(TokenType.BANG, "!", null)
                }
            }
            '<' -> {
                if (remaining.length >= 2 && remaining[1] == '=') {
                    remaining = remaining.substring(2)
                    Token(TokenType.LESS_EQUAL, "<=", null)
                } else {
                    remaining = remaining.substring(1)
                    Token(TokenType.LESS, "<", null)
                }
            }
            '>' -> {
                if (remaining.length >= 2 && remaining[1] == '=') {
                    remaining = remaining.substring(2)
                    Token(TokenType.GREATER_EQUAL, ">=", null)
                } else {
                    remaining = remaining.substring(1)
                    Token(TokenType.GREATER, ">", null)
                }
            }
            '/' -> {
                if (remaining.length >= 2 && remaining[1] == '/') {
                    var n = false
                    var index = remaining.indexOf("\r\n")
                    if (index == -1) {
                        n = true
                        index = remaining.indexOf("\n")
                    }
                    remaining = if (index == -1) {
                        ""
                    } else {
                        if (n) {
                            remaining.substring(index + 1)
                        } else {
                            remaining.substring(index + 2)
                        }
                    }
                    line++
                    Token(TokenType.COMMENT, "", null)
                } else {
                    remaining = remaining.substring(1)
                    Token(TokenType.SLASH, "/", null)
                }
            }
            ' ', '\t', '\r', '\n' -> {
                if (remaining[0] == '\n') {
                    line++
                }
                remaining = remaining.substring(1)
                Token(TokenType.WHITESPACE, "", null)
            }
            '"' -> {
                remaining = remaining.substring(1)
                val index = remaining.indexOf('"')
                if (index == -1) {
                    val nlIndex = remaining.indexOf("\n")
                    remaining = remaining.substring(0, nlIndex + 1)
                    Token(TokenType.ERR, "", null).apply {
                        errLine = line
                        errMsg = "Unterminated string."
                    }
                } else {
                    val str = remaining.substring(0, index)
                    remaining = remaining.substring(index + 1)
                    Token(TokenType.STRING, str, str)
                }
            }
            in ('0'..'9') -> {
                var i = 0
                var dotCount = 0
                while (true) {
                    if (i < remaining.length && remaining[i] in ('0'..'9')) {
                        i++
                        continue
                    }
                    if (i < remaining.length && remaining[i] == '.' && dotCount == 0) {
                        i++
                        dotCount++
                        continue
                    }
                    break
                }
                val value = remaining.substring(0, i)
                remaining = remaining.substring(i)
                if (value.endsWith(".")) {
                    if (remaining.isEmpty()) {
                        remaining = "."
                        Token(TokenType.NUMBER, value.substring(0, value.length - 1), value + "0")
                    } else {
                        Token(TokenType.NUMBER, value.substring(0, value.length - 1), value + "0")
                    }
                } else {
                    var literal = value
                    if (!literal.contains(".") && !literal.endsWith(".")) {
                        literal += ".0"
                    }
                    while (literal.contains(".0") && literal.endsWith("0") && !literal.endsWith(".0")) {
                        literal = literal.substring(0, literal.length - 1)
                    }
                    Token(TokenType.NUMBER, value, literal)
                }
            }
            in ('a'..'z'), in ('A'..'Z'), '_' -> {
                if (remaining.startsWith("and")) {
                    remaining = remaining.substring(3)
                    Token(TokenType.AND, "and", null)
                } else if (remaining.startsWith("class")) {
                    remaining = remaining.substring(5)
                    Token(TokenType.CLASS, "class", null)
                } else if (remaining.startsWith("else")) {
                    remaining = remaining.substring(4)
                    Token(TokenType.ELSE, "else", null)
                } else if (remaining.startsWith("false")) {
                    remaining = remaining.substring(5)
                    Token(TokenType.FALSE, "false", null)
                } else if (remaining.startsWith("fun")) {
                    remaining = remaining.substring(3)
                    Token(TokenType.FUN, "fun", null)
                } else if (remaining.startsWith("for")) {
                    remaining = remaining.substring(3)
                    Token(TokenType.FOR, "for", null)
                } else if (remaining.startsWith("if")) {
                    remaining = remaining.substring(2)
                    Token(TokenType.IF, "if", null)
                } else if (remaining.startsWith("nil")) {
                    remaining = remaining.substring(3)
                    Token(TokenType.NIL, "nil", null)
                } else if (remaining.startsWith("or")) {
                    remaining = remaining.substring(2)
                    Token(TokenType.OR, "or", null)
                } else if (remaining.startsWith("print")) {
                    remaining = remaining.substring(5)
                    Token(TokenType.PRINT, "print", null)
                } else if (remaining.startsWith("return")) {
                    remaining = remaining.substring(6)
                    Token(TokenType.RETURN, "return", null)
                } else if (remaining.startsWith("super")) {
                    remaining = remaining.substring(5)
                    Token(TokenType.SUPER, "super", null)
                } else if (remaining.startsWith("this")) {
                    remaining = remaining.substring(4)
                    Token(TokenType.THIS, "this", null)
                } else if (remaining.startsWith("true")) {
                    remaining = remaining.substring(4)
                    Token(TokenType.TRUE, "true", null)
                } else if (remaining.startsWith("var")) {
                    remaining = remaining.substring(3)
                    Token(TokenType.VAR, "var", null)
                } else if (remaining.startsWith("while")) {
                    remaining = remaining.substring(5)
                    Token(TokenType.WHILE, "while", null)
                } else {
                    var index = 0
                    while (index < remaining.length && (
                                remaining[index] in ('a'..'z') ||
                                        remaining[index] in ('A'..'Z') ||
                                        remaining[index] in ('0'..'9') ||
                                        remaining[index] == '_'
                                )
                    ) {
                        index++
                    }
                    val value = remaining.substring(0, index)
                    remaining = remaining.substring(index)
                    Token(TokenType.IDENTIFIER, value, null)
                }
            }
            else -> {
                val char = remaining[0]
                remaining = remaining.substring(1)
                Token(TokenType.ERR, "", null).apply {
                    errLine = line
                    errMsg = "Unexpected character: $char"
                }
            }
        }
    }
}