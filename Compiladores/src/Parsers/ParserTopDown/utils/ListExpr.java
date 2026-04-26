package Parsers.ParserTopDown.utils;

import java.util.List;

public record ListExpr(DelimKind delim, List<Node> elements) implements Node {
	public ListExpr {
		elements = List.copyOf(elements);
	}
}
