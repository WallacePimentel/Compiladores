package Parsers.ParserTopDown.utils;

import java.util.List;

public record ParseResult(Program program, List<ParseError> errors) {
	public ParseResult {
		errors = List.copyOf(errors);
	}

	public boolean accepted() {
		return errors.isEmpty();
	}
}
