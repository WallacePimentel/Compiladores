package Parsers.ParserTopDown;

import GeradorScanner.GeradorScanner;
import Parsers.ParserTopDown.utils.Atom;
import Parsers.ParserTopDown.utils.AtomKind;
import Parsers.ParserTopDown.utils.DelimKind;
import Parsers.ParserTopDown.utils.DottedListExpr;
import Parsers.ParserTopDown.utils.LangDirective;
import Parsers.ParserTopDown.utils.ListExpr;
import Parsers.ParserTopDown.utils.Node;
import Parsers.ParserTopDown.utils.ParseError;
import Parsers.ParserTopDown.utils.ParseResult;
import Parsers.ParserTopDown.utils.Program;
import Parsers.ParserTopDown.utils.QuoteExpr;
import Parsers.ParserTopDown.utils.QuoteKind;
import Parsers.ParserTopDown.utils.VectorExpr;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

public class ParserTopDown {


	private static final Set<String> NUMBER_TOKENS = Set.of(
			"INT", "FLOAT", "RATIO", "SCIENTIFIC", "COMPLEX", "IMAG",
			"BINARY", "OCTAL", "DECIMAL", "HEX"
	);

	private static final Set<String> SYMBOL_TOKENS = Set.of(
			"IDENT",
			"KW_DEFINE", "KW_LAMBDA", "KW_IF", "KW_BEGIN", "KW_LET", "KW_QUOTE"
	);

	private final List<GeradorScanner.Token> tokens;
	private final List<ParseError> errors = new ArrayList<>();
	private int pos = 0;

	public ParserTopDown(List<GeradorScanner.Token> tokens) {
		Objects.requireNonNull(tokens, "tokens");
		this.tokens = filterSkips(tokens);
	}

	public static ParseResult parseTokens(List<GeradorScanner.Token> tokens) {
		return new ParserTopDown(tokens).parse();
	}

	public ParseResult parse() {
		List<Node> forms = new ArrayList<>();

		while (!isAtEnd()) {
			// #lang <ident> no começo do arquivo é comum
			if (match("KW_LANG")) {
				forms.add(parseLangDirective());
				continue;
			}

			// #; comenta o próximo datum: parseia e descarta
			if (match("HASH_SEMI")) {
				Node ignored = parseDatumOrRecover(0, "Esperado datum após #;");
				// intencionalmente não adiciona no AST
				continue;
			}

			Node datum = parseDatumOrRecover(0, "Esperado datum");
			if (datum != null) {
				forms.add(datum);
			}
		}

		return new ParseResult(new Program(forms), errors);
	}

	private LangDirective parseLangDirective() {
		if (isAtEnd()) {
			errorHere("Esperado identificador da linguagem após #lang");
			return new LangDirective("<missing>");
		}

		GeradorScanner.Token tk = peek();
		if (isSymbolToken(tk.tipo())) {
			advance();
			return new LangDirective(tk.lexema());
		}

		errorHere("Esperado identificador da linguagem após #lang");
		// tenta consumir algo pra não travar
		advance();
		return new LangDirective("<invalid>");
	}

	private Node parseDatumOrRecover(String expectation) {
		return parseDatumOrRecover(0, expectation);
	}

	private Node parseDatumOrRecover(int quasiquoteDepth, String expectation) {
		int startPos = pos;
		try {
			return parseDatum(quasiquoteDepth);
		} catch (Panic e) {
			// recovery: avança até um token que possa iniciar um datum ou um fechador
			synchronize(startPos, expectation);
			return null;
		}
	}

	private Node parseDatum(int quasiquoteDepth) {
		if (isAtEnd()) {
			throw panic("Fim inesperado de arquivo");
		}

		// quotes
		if (match("QUOTE")) {
			return new QuoteExpr(QuoteKind.QUOTE, requireDatum(quasiquoteDepth, "Esperado datum após '\''"));
		}
		if (match("QUASIQUOTE")) {
			return new QuoteExpr(QuoteKind.QUASIQUOTE, requireDatum(quasiquoteDepth + 1, "Esperado datum após '`'"));
		}
		if (match("UNQUOTE_SPLICING")) {
			if (quasiquoteDepth <= 0) {
				errorPrevious("unquote-splicing (,@) fora de quasiquote");
			}
			return new QuoteExpr(QuoteKind.UNQUOTE_SPLICING, requireDatum(quasiquoteDepth, "Esperado datum após ',@'"));
		}
		if (match("UNQUOTE")) {
			if (quasiquoteDepth <= 0) {
				errorPrevious("unquote (,) fora de quasiquote");
			}
			return new QuoteExpr(QuoteKind.UNQUOTE, requireDatum(quasiquoteDepth, "Esperado datum após ','"));
		}

		// estruturas
		if (match("VECTOR_START")) {
			return parseVector(quasiquoteDepth);
		}
		if (match("LPAREN")) {
			return parseList(quasiquoteDepth, DelimKind.PAREN, "RPAREN");
		}
		if (match("LBRACK")) {
			return parseList(quasiquoteDepth, DelimKind.BRACKET, "RBRACK");
		}
		if (match("LBRACE")) {
			return parseList(quasiquoteDepth, DelimKind.BRACE, "RBRACE");
		}

		// fechadores inesperados
		if (checkOneOf("RPAREN", "RBRACK", "RBRACE")) {
			GeradorScanner.Token t = advance();
			throw panic("Fechamento inesperado: " + t.tipo());
		}

		// átomos
		GeradorScanner.Token tk = advance();
		String type = tk.tipo();
		if ("STRING".equals(type)) {
			return new Atom(AtomKind.STRING, type, tk.lexema());
		}
		if ("CHAR".equals(type)) {
			return new Atom(AtomKind.CHAR, type, tk.lexema());
		}
		if ("BOOL_TRUE".equals(type) || "BOOL_FALSE".equals(type)) {
			return new Atom(AtomKind.BOOLEAN, type, tk.lexema());
		}
		if (NUMBER_TOKENS.contains(type)) {
			return new Atom(AtomKind.NUMBER, type, tk.lexema());
		}
		if (isSymbolToken(type)) {
			return new Atom(AtomKind.SYMBOL, type, tk.lexema());
		}

		throw panic("Token inesperado: " + type);
	}

	private Node requireDatum(int quasiquoteDepth, String messageIfMissing) {
		if (isAtEnd()) {
			throw panic(messageIfMissing);
		}

		// permitir #; antes do datum (p.ex. "' #; x y")
		if (match("HASH_SEMI")) {
			parseDatumOrRecover(quasiquoteDepth, "Esperado datum após #;");
			if (isAtEnd()) throw panic(messageIfMissing);
		}

		return parseDatum(quasiquoteDepth);
	}

	private VectorExpr parseVector(int quasiquoteDepth) {
		List<Node> elements = new ArrayList<>();
		while (!isAtEnd() && !check("RPAREN")) {
			if (match("HASH_SEMI")) {
				parseDatumOrRecover(quasiquoteDepth, "Esperado datum após #;");
				continue;
			}
			Node e = parseDatumOrRecover(quasiquoteDepth, "Esperado elemento do vetor");
			if (e != null) elements.add(e);
		}

		if (!match("RPAREN")) {
			errorHere("Esperado ')' para fechar vetor iniciado por '#('");
		}

		return new VectorExpr(elements);
	}

	private Node parseList(int quasiquoteDepth, DelimKind kind, String expectedClose) {
		List<Node> elements = new ArrayList<>();
		boolean sawDot = false;
		Node tail = null;

		while (!isAtEnd() && !check(expectedClose)) {
			if (match("HASH_SEMI")) {
				parseDatumOrRecover(quasiquoteDepth, "Esperado datum após #;");
				continue;
			}

			// dotted list: (a b . c)
			if (match("DOT")) {
				if (sawDot) {
					errorHere("Ponto '.' duplicado em lista pontilhada");
					// continua tentando achar o fechamento
					continue;
				}
				if (elements.isEmpty()) {
					errorHere("Ponto '.' não pode ser o primeiro elemento da lista");
				}
				sawDot = true;
				tail = parseDatumOrRecover(quasiquoteDepth, "Esperado datum após '.'");

				// após o tail, só pode vir o fechador
				while (!isAtEnd() && !check(expectedClose)) {
					if (checkOneOf("RPAREN", "RBRACK", "RBRACE")) break;
					errorHere("Após '.' deve haver exatamente um datum antes do fechador");
					parseDatumOrRecover(quasiquoteDepth, "Ignorando token inesperado após '.'");
				}
				break;
			}

			Node e = parseDatumOrRecover(quasiquoteDepth, "Esperado elemento da lista");
			if (e != null) elements.add(e);
		}

		if (match(expectedClose)) {
			if (sawDot) {
				if (tail == null) {
					errorHere("Esperado datum após '.'");
					return new ListExpr(kind, elements);
				}
				return new DottedListExpr(kind, elements, tail);
			}
			return new ListExpr(kind, elements);
		}

		// tentativa de recuperação: se veio outro fechador, consome e reporta mismatch
		if (checkOneOf("RPAREN", "RBRACK", "RBRACE")) {
			GeradorScanner.Token got = advance();
			errors.add(new ParseError(
					pos - 1,
					got.tipo(),
					got.lexema(),
					"Fechador incorreto: esperado " + expectedClose + ", mas veio " + got.tipo()
			));
			if (sawDot && tail != null) {
				return new DottedListExpr(kind, elements, tail);
			}
			return new ListExpr(kind, elements);
		}

		errorHere("Fim inesperado: esperado " + expectedClose);
		if (sawDot && tail != null) {
			return new DottedListExpr(kind, elements, tail);
		}
		return new ListExpr(kind, elements);
	}

	// ------------------- helpers -------------------

	private boolean isSymbolToken(String tokenType) {
		return SYMBOL_TOKENS.contains(tokenType);
	}

	private boolean check(String tokenType) {
		return !isAtEnd() && peek().tipo().equals(tokenType);
	}

	private boolean checkOneOf(String... types) {
		if (isAtEnd()) return false;
		String t = peek().tipo();
		for (String s : types) {
			if (t.equals(s)) return true;
		}
		return false;
	}

	private boolean match(String tokenType) {
		if (check(tokenType)) {
			advance();
			return true;
		}
		return false;
	}

	private GeradorScanner.Token advance() {
		if (!isAtEnd()) pos++;
		return previous();
	}

	private boolean isAtEnd() {
		return pos >= tokens.size();
	}

	private GeradorScanner.Token peek() {
		return tokens.get(pos);
	}

	private GeradorScanner.Token previous() {
		return tokens.get(pos - 1);
	}

	private void errorHere(String message) {
		if (isAtEnd()) {
			errors.add(new ParseError(pos, "<EOF>", "", message));
			return;
		}
		GeradorScanner.Token t = peek();
		errors.add(new ParseError(pos, t.tipo(), t.lexema(), message));
	}

	private void errorPrevious(String message) {
		if (pos <= 0) {
			errors.add(new ParseError(0, "<BOF>", "", message));
			return;
		}
		GeradorScanner.Token t = previous();
		errors.add(new ParseError(pos - 1, t.tipo(), t.lexema(), message));
	}

	private Panic panic(String message) {
		errorHere(message);
		return new Panic();
	}

	private void synchronize(int startPos, String expectation) {
		if (pos == startPos) {
			// garante progresso
			if (!isAtEnd()) advance();
		}

		Set<String> starters = new HashSet<>();
		starters.addAll(SYMBOL_TOKENS);
		starters.addAll(NUMBER_TOKENS);
		starters.addAll(Set.of(
				"KW_LANG", "HASH_SEMI",
				"LPAREN", "LBRACK", "LBRACE", "VECTOR_START",
				"QUOTE", "QUASIQUOTE", "UNQUOTE", "UNQUOTE_SPLICING",
				"STRING", "CHAR", "BOOL_TRUE", "BOOL_FALSE"
		));

		while (!isAtEnd()) {
			String tt = peek().tipo();
			if (tt.equals("RPAREN") || tt.equals("RBRACK") || tt.equals("RBRACE")) return;
			if (starters.contains(tt)) return;
			advance();
		}
	}

	private static List<GeradorScanner.Token> filterSkips(List<GeradorScanner.Token> raw) {
		List<GeradorScanner.Token> out = new ArrayList<>(raw.size());
		for (GeradorScanner.Token t : raw) {
			if (t == null) continue;
			if (t.tipo() != null && t.tipo().startsWith("SKIP_")) continue;
			out.add(t);
		}
		return out;
	}

	private static final class Panic extends RuntimeException {
		private Panic() {
			super(null, null, false, false);
		}
	}
}
