package scanning
class Token(val type: TokenType, val lexeme: String, val literal: String?) {
    var errLine = 0
    var errMsg = ""
    override fun toString(): String {
        return "${type.name} ${
            if (lexeme.isEmpty()) ""
            else {
                if (type == TokenType.STRING) {
                    "\"" + lexeme + "\""
                } else {
                    lexeme
                }
            }
        } $literal"
    }
}