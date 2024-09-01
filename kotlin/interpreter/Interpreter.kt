package interpreter
import ast.*
import java.math.BigDecimal
import java.math.RoundingMode
enum class TokenType {
    // literals
    STRING, NUMBER,
    // tokens of one or two characters
    LEFT_PAREN, RIGHT_PAREN, LEFT_BRACE, RIGHT_BRACE, COMMA, DOT, MINUS, PLUS, SEMICOLON, STAR, SLASH, EQUAL, EQUAL_EQUAL, BANG, BANG_EQUAL, LESS, LESS_EQUAL, GREATER, GREATER_EQUAL,
    // special
    EOF, IDENTIFIER,
    // reserved keywords
    AND, CLASS, ELSE, FALSE, FOR, FUN, IF, NIL, OR, PRINT, RETURN, SUPER, THIS, TRUE, VAR, WHILE
}
data class Token(val type: TokenType, val lexeme: String, val literal: String?, val start: Int, val end: Int)
val reservedWords = mapOf(
    "and" to TokenType.AND,
    "class" to TokenType.CLASS,
    "else" to TokenType.ELSE,
    "false" to TokenType.FALSE,
    "for" to TokenType.FOR,
    "fun" to TokenType.FUN,
    "if" to TokenType.IF,
    "nil" to TokenType.NIL,
    "or" to TokenType.OR,
    "print" to TokenType.PRINT,
    "return" to TokenType.RETURN,
    "super" to TokenType.SUPER,
    "this" to TokenType.THIS,
    "true" to TokenType.TRUE,
    "var" to TokenType.VAR,
    "while" to TokenType.WHILE,
)
fun interpret(string: String, onErrorCallback: ((offset: Int, line: Int, message: String) -> Unit)? = null) = sequence {
    var offset: Int = 0
    var line: Int = 1
    val peek = { string.getOrNull(offset) }
    val peekMore = { string.getOrNull(offset + 1) }
    val skipWhitespaces = {
        while (offset < string.length) {
            if (string[offset].isWhitespace()) {
                if (string[offset] == '\n') {
                    ++line
                }
                ++offset
            } else if (string[offset] == '/' && peekMore() == '/') {
                // comment
                while (offset < string.length && string[offset] != '\n') {
                    ++offset
                }
            } else {
                break
            }
        }
    }
    val yieldToken: suspend SequenceScope<Token>.(TokenType, Int) -> Unit = { type, length ->
        val token = Token(type, string.substring(offset, offset + length), null, offset, offset + length)
        offset += length
        yield(token)
    }
    while (true) {
        skipWhitespaces()
        if (offset > string.length) {
            throw IllegalStateException("End of string")
        } else if (offset == string.length) {
            val token = Token(TokenType.EOF, "", null, offset, offset + 1)
            yield(token)
            return@sequence
        }
        when (peek()) {
            '(' -> yieldToken(TokenType.LEFT_PAREN, 1)
            ')' -> yieldToken(TokenType.RIGHT_PAREN, 1)
            '{' -> yieldToken(TokenType.LEFT_BRACE, 1)
            '}' -> yieldToken(TokenType.RIGHT_BRACE, 1)
            ',' -> yieldToken(TokenType.COMMA, 1)
            '.' -> yieldToken(TokenType.DOT, 1)
            '-' -> yieldToken(TokenType.MINUS, 1)
            '+' -> yieldToken(TokenType.PLUS, 1)
            ';' -> yieldToken(TokenType.SEMICOLON, 1)
            '*' -> yieldToken(TokenType.STAR, 1)
            '/' -> yieldToken(TokenType.SLASH, 1)
            '=' -> when (peekMore()) {
                '=' -> yieldToken(TokenType.EQUAL_EQUAL, 2)
                else -> yieldToken(TokenType.EQUAL, 1)
            }
            '!' -> when (peekMore()) {
                '=' -> yieldToken(TokenType.BANG_EQUAL, 2)
                else -> yieldToken(TokenType.BANG, 1)
            }
            '<' -> when (peekMore()) {
                '=' -> yieldToken(TokenType.LESS_EQUAL, 2)
                else -> yieldToken(TokenType.LESS, 1)
            }
            '>' -> when (peekMore()) {
                '=' -> yieldToken(TokenType.GREATER_EQUAL, 2)
                else -> yieldToken(TokenType.GREATER, 1)
            }
            '"' -> {
                val start = offset
                ++offset
                while (offset < string.length && string[offset] != '"') {
                    if (string[offset] == '\n') {
                        ++line
                    }
                    ++offset
                }
                if (offset == string.length) {
                    // unterminated string
                    onErrorCallback?.invoke(offset, line, "[line ${line}] Error: Unterminated string.")
                    continue
                } else {
                    ++offset
                }
                yield(
                    Token(
                        TokenType.STRING,
                        string.substring(start, offset),
                        string.substring(start + 1, offset - 1),
                        start,
                        offset
                    )
                )
            }
            in '0'..'9' -> {
                val start = offset
                while (peek()?.isDigit() == true) {
                    ++offset
                }
                val dotted = peek() == '.' && peekMore()?.isDigit() == true
                if (dotted) {
                    offset += 2
                    while (peek()?.isDigit() == true) {
                        ++offset
                    }
                }
                val lexeme = string.substring(start, offset)
                val literal = lexeme.toDouble().toString()
                yield(Token(TokenType.NUMBER, lexeme, literal, start, offset))
            }
            '_', in 'a'..'z', in 'A'..'Z' -> {
                val start = offset
                ++offset
                while (offset < string.length) {
                    when (string[offset]) {
                        '_', in 'a'..'z', in 'A'..'Z', in '0'..'9' -> ++offset
                        else -> break
                    }
                }
                val lexeme = string.substring(start, offset)
                yield(
                    when (val type = reservedWords[lexeme]) {
                        null -> Token(TokenType.IDENTIFIER, lexeme, null, start, offset)
                        else -> Token(type, lexeme, null, start, offset)
                    }
                )
            }
            else -> {
                onErrorCallback?.invoke(
                    offset, line, "[line ${line}] Error: Unexpected character: ${string[offset]}"
                )
                ++offset
                continue
            }
        }
    }
}
sealed interface RPNNode
data class RPNOperand(val value: ASTNode) : RPNNode
data class RPNOperator(val operator: Operator) : RPNNode
class ParseException(message: String) : IllegalArgumentException(message)
class EvaluationException(message: String) : IllegalArgumentException(message)
interface PeekingIterator<T> : Iterator<T> {
    fun peek(): T
}
fun <T> Iterator<T>.peeking(): PeekingIterator<T> {
    return object : PeekingIterator<T> {
        var hasPeeked = false
        var peeked: T? = null
        override fun peek(): T {
            if (!hasPeeked) {
                peeked = this@peeking.next()
                hasPeeked = true
            }
            @Suppress("UNCHECKED_CAST") return peeked as T
        }
        override fun next(): T {
            return if (hasPeeked) {
                hasPeeked = false
                @Suppress("UNCHECKED_CAST") peeked as T
            } else {
                this@peeking.next()
            }
        }
        override fun hasNext(): Boolean {
            return hasPeeked || this@peeking.hasNext()
        }
    }
}
private fun makeTree(rpn: ArrayList<RPNNode>): ASTNode {
    if (rpn.isEmpty()) {
        throw ParseException("Unexpected empty RPN")
    }
    return when (val node = rpn.removeLast()) {
        is RPNOperator -> when (val operator = node.operator) {
            is BinaryOperator -> {
                val right = makeTree(rpn)
                val left = makeTree(rpn)
                BinaryOperatorNode(operator, left, right)
            }
            is UnaryOperator -> {
                val operand = makeTree(rpn)
                UnaryOperatorNode(operator, operand)
            }
            is AssignmentOperator -> {
                val right = makeTree(rpn)
                val left = rpn.removeLast()
                if (left !is RPNOperand || left.value !is VariableNode) {
                    throw ParseException("Unexpected $left on the left side of an assignment")
                }
                AssignmentOperatorNode(left.value.name, right)
            }
        }
        is RPNOperand -> {
            node.value
        }
    }
}
private object OperatorPrecedence {
    const val UNARY = 3
    const val MULTIPLICATION_DIVISION = 5
    const val ADDITION_SUBTRACTION = 6
    const val RELATIONAL = 9
    const val EQUALITY = 10
    const val LOGICAL_AND = 14
    const val LOGICAL_OR = 15
    const val ASSIGNMENT = 16
}
fun PeekingIterator<Token>.nextExpression(): ASTNode {
    val operators = ArrayDeque<Pair<Operator, Int>>()
    val rpn = ArrayList<RPNNode>()
    var needUnary = true
    val buildAST = {
        while (operators.isNotEmpty()) {
            if (operators.last().first is Group) {
                throw ParseException("Unclosed left parentheses")
            }
            rpn.add(RPNOperator(operators.removeLast().first))
        }
        val tree = makeTree(rpn)
        if (rpn.isNotEmpty()) {
            throw ParseException("Unexpected non-empty RPN")
        }
        tree
    }
    while (hasNext()) {
        val token = peek()
        if (token.type == TokenType.SEMICOLON || token.type == TokenType.EOF) {
            return buildAST()
        }
        next()
        // https://en.cppreference.com/w/cpp/language/operator_precedence
        if (needUnary) {
            when (token.type) {
                TokenType.PLUS -> operators.add(Positive to OperatorPrecedence.UNARY)
                TokenType.MINUS -> operators.add(Negative to OperatorPrecedence.UNARY)
                TokenType.BANG -> operators.add(LogicalNot to OperatorPrecedence.UNARY)
                TokenType.LEFT_PAREN -> operators.add(Group to 100)
                TokenType.NUMBER -> {
                    rpn.add(RPNOperand(ValueNode(RealValue(token.literal!!.toDouble()))))
                    needUnary = false
                }
                TokenType.STRING -> {
                    rpn.add(RPNOperand(ValueNode(StringValue(token.literal!!))))
                    needUnary = false
                }
                TokenType.TRUE -> {
                    rpn.add(RPNOperand(ValueNode(BooleanValue(true))))
                    needUnary = false
                }
                TokenType.FALSE -> {
                    rpn.add(RPNOperand(ValueNode(BooleanValue(false))))
                    needUnary = false
                }
                TokenType.NIL -> {
                    rpn.add(RPNOperand(ValueNode(NilValue)))
                    needUnary = false
                }
                TokenType.IDENTIFIER -> {
                    rpn.add(RPNOperand(VariableNode(token.lexeme)))
                    needUnary = false
                }
                else -> throw ParseException("Unexpected token $token")
            }
        } else {
            val pushBinaryOperator: (Operator, Int) -> Unit = { operator, precedence ->
                while (operators.isNotEmpty()) {
                    val (last, lastPrecedence) = operators.last()
                    if (lastPrecedence < precedence || (lastPrecedence == precedence && last is BinaryOperator && last.isLeftAssociative())) {
                        operators.removeLast()
                        rpn.add(RPNOperator(last))
                    } else {
                        break
                    }
                }
                operators.add(operator to precedence)
                needUnary = true
            }
            when (token.type) {
                TokenType.PLUS -> pushBinaryOperator(Plus, OperatorPrecedence.ADDITION_SUBTRACTION)
                TokenType.MINUS -> pushBinaryOperator(Minus, OperatorPrecedence.ADDITION_SUBTRACTION)
                TokenType.STAR -> pushBinaryOperator(Times, OperatorPrecedence.MULTIPLICATION_DIVISION)
                TokenType.SLASH -> pushBinaryOperator(Div, OperatorPrecedence.MULTIPLICATION_DIVISION)
                TokenType.EQUAL_EQUAL -> pushBinaryOperator(EqualTo, OperatorPrecedence.EQUALITY)
                TokenType.BANG_EQUAL -> pushBinaryOperator(NotEqualTo, OperatorPrecedence.EQUALITY)
                TokenType.LESS -> pushBinaryOperator(LessThan, OperatorPrecedence.RELATIONAL)
                TokenType.LESS_EQUAL -> pushBinaryOperator(LessThanOrEqualTo, OperatorPrecedence.RELATIONAL)
                TokenType.GREATER -> pushBinaryOperator(GreaterThan, OperatorPrecedence.RELATIONAL)
                TokenType.GREATER_EQUAL -> pushBinaryOperator(GreaterThanOrEqualTo, OperatorPrecedence.RELATIONAL)
                TokenType.OR -> pushBinaryOperator(LogicalOr, OperatorPrecedence.LOGICAL_OR)
                TokenType.AND -> pushBinaryOperator(LogicalAnd, OperatorPrecedence.LOGICAL_AND)
                TokenType.EQUAL -> pushBinaryOperator(AssignmentOperator, OperatorPrecedence.ASSIGNMENT)
                TokenType.RIGHT_PAREN -> {
                    while (operators.isNotEmpty() && operators.last().first !is Group) {
                        val (last, _) = operators.removeLast()
                        rpn.add(RPNOperator(last))
                    }
                    if (operators.lastOrNull()?.first !is Group) {
                        throw ParseException("Unmatched right parenthesis: $token")
                    } else {
                        // we keep the group operator as output specs
                        val (last, _) = operators.removeLast()
                        rpn.add(RPNOperator(last))
                    }
                    needUnary = false
                }
                else -> throw ParseException("Unexpected token $token")
            }
        }
    }
    throw ParseException("Unexpected end of input when parsing an expression")
}
fun PeekingIterator<Token>.consume(type: TokenType): Token {
    val token = next()
    if (token.type != type) {
        throw ParseException("Expected $type but got ${token.type}")
    }
    return token
}
fun PeekingIterator<Token>.nextStatement(): StatementNode {
    if (!hasNext()) {
        throw ParseException("Unexpected end of input when parsing a statement")
    }
    val token = peek()
    return when (token.type) {
        TokenType.PRINT -> {
            next()
            val expression = nextExpression()
            consume(TokenType.SEMICOLON)
            SingleStatementNode(CallableNode("print", listOf(expression)) { _, args ->
                println(
                    when (val value = args[0]) {
                        is RealValue -> {
                            BigDecimal(value.value).setScale(10, RoundingMode.HALF_UP).stripTrailingZeros()
                                .toPlainString()
                        }
                        else -> value.format()
                    }
                )
                NilValue
            })
        }
        TokenType.VAR -> {
            next()
            val identifier = consume(TokenType.IDENTIFIER)
            when (peek().type) {
                TokenType.EQUAL -> {
                    next()
                    val expression = nextExpression()
                    consume(TokenType.SEMICOLON)
                    SingleStatementNode(CallableNode("defineVariable", listOf(expression)) { ctx, args ->
                        ctx.declareVariable(identifier.lexeme)
                        ctx.assignVariable(identifier.lexeme, args[0])
                        NilValue
                    })
                }
                else -> {
                    consume(TokenType.SEMICOLON)
                    SingleStatementNode(CallableNode("declareVariable", listOf(ValueNode(NilValue))) { ctx, args ->
                        ctx.declareVariable(identifier.lexeme)
                        NilValue
                    })
                }
            }
        }
        TokenType.IDENTIFIER -> {
            val identifier = next()
            consume(TokenType.EQUAL)
            val expression = nextExpression()
            consume(TokenType.SEMICOLON)
            SingleStatementNode(CallableNode("assignVariable", listOf(expression)) { ctx, args ->
                if (ctx.getVariable(identifier.lexeme) == null) {
                    throw EvaluationException("Variable ${identifier.lexeme} is not defined")
                }
                ctx.assignVariable(identifier.lexeme, args[0])
                args[0]
            })
        }
        TokenType.LEFT_BRACE -> {
            next()
            val statements = mutableListOf<StatementNode>()
            while (peek().type != TokenType.RIGHT_BRACE) {
                statements.add(nextStatement())
            }
            consume(TokenType.RIGHT_BRACE)
            BlockStatementNode(statements)
        }
        else -> {
            val expression = nextExpression()
            consume(TokenType.SEMICOLON)
            SingleStatementNode(expression)
        }
    }
}
fun Sequence<Token>.parse() = sequence {
    val stream = iterator().peeking()
    while (stream.hasNext()) {
        val token = stream.peek()
        when (token.type) {
            TokenType.EOF -> return@sequence
            else -> yield(stream.nextStatement())
        }
    }
}