package Parsers.ParserTopDown.utils;

import java.util.List;

public record VectorExpr(List<Node> elements) implements Node {
	public VectorExpr {
		elements = List.copyOf(elements);
	}
}
