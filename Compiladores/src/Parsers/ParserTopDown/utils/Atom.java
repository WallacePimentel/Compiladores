package Parsers.ParserTopDown.utils;

public record Atom(AtomKind kind, String tokenType, String lexeme) implements Node {
}
