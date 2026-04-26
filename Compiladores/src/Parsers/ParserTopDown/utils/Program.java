package Parsers.ParserTopDown.utils;

import java.util.List;

public record Program(List<Node> forms) implements Node {
	public Program {
		forms = List.copyOf(forms);
	}
}
