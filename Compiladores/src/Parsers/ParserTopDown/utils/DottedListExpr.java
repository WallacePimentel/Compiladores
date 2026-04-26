package Parsers.ParserTopDown.utils;

import java.util.List;
import java.util.Objects;

/**
 * Representa listas pontilhadas do reader de Scheme/Racket: (a b . c).
 * - prefix: elementos antes do '.' (>= 1)
 * - tail: o datum após '.'
 */
public record DottedListExpr(DelimKind delim, List<Node> prefix, Node tail) implements Node {
	public DottedListExpr {
		prefix = List.copyOf(prefix);
		Objects.requireNonNull(tail, "tail");
	}
}
