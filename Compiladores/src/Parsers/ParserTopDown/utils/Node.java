package Parsers.ParserTopDown.utils;

public sealed interface Node permits Program, ListExpr, DottedListExpr, VectorExpr, QuoteExpr, Atom, LangDirective {
}
