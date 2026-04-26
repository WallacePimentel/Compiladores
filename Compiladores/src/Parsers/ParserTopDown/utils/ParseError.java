package Parsers.ParserTopDown.utils;

public record ParseError(int tokenIndex, String tokenType, String lexeme, String message) {
}
