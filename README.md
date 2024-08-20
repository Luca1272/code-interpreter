Lox Interpreter in Kotlin
This repository contains a Kotlin-based interpreter for the Lox scripting language, following the implementation style outlined in Robert Nystrom's book "Crafting Interpreters". Lox is a simple, dynamically-typed language with a focus on simplicity and expressiveness.

Features
Tokenizer/Lexer: Converts a sequence of characters into a sequence of tokens.
Parser: Builds a syntax tree from tokens.
Interpreter: Evaluates the syntax tree to execute Lox programs.
Project Structure
Main.kt: The entry point for the interpreter. It initializes the interpreter and handles command-line input.
Token.kt: Defines the structure of a token in the Lox language, including its type and literal value.
TokenType.kt: Enum class listing all possible types of tokens in Lox (keywords, operators, literals, etc.).
Tokenizer.kt: Responsible for breaking up input strings into tokens that the parser can then process.
Parser.kt: Takes tokens produced by the Tokenizer and builds an Abstract Syntax Tree (AST) that represents the Lox program.
Getting Started
Prerequisites
Kotlin 1.5 or higher
A command-line environment
Building the Project
To build the project, use the Kotlin compiler. You can compile the code using the following command:

bash
kotlinc Main.kt Token.kt TokenType.kt Tokenizer.kt Parser.kt -include-runtime -d LoxInterpreter.jar
This command compiles the source files and packages them into a single executable JAR file.

Running the Interpreter
After building the project, you can run the interpreter with:

bash
java -jar LoxInterpreter.jar [script.lox]
If you do not specify a script file, the interpreter will start in interactive mode, allowing you to enter Lox code directly.

Example
Create a file example.lox with the following content:

lox
print "Hello, World!";
Run the interpreter:

bash
java -jar LoxInterpreter.jar example.lox
Expected output:

bash
Hello, World!
Contributing
Contributions are welcome! If you'd like to contribute, please fork the repository and make changes as you'd like. Pull requests are warmly welcome.

Issues
If you find a bug or have a feature request, please open an issue on GitHub.