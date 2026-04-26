import GeradorScanner.GeradorScanner;
import ExpressoesRegulares.ExpressaoRegular;
import utils.RegexUtils;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.*;

public class Main {
    public static void main(String[] args) {
        // classes básicas
        final String DIGIT = "[0-9]";
        final String LOWER = "[a-z]";
        final String UPPER = "[A-Z]";
        final String LATIN1 = "[À-ÿ]";
        final String LETTER = "(" + LOWER + "|" + UPPER + "|" + LATIN1 + ")";

        // whitespace (ignorados na saida)
        final String WS = "([ ]|\\t|\\n|\\r)+";
        // linha de comentario
        final String PRINT_ASCII_NO_NL = "([ -~]|" + LATIN1 + ")";
        final String LINE_COMMENT = ";" + PRINT_ASCII_NO_NL + "*" + "(\\n)?";

        // bloco de comentario
        final String BLOCK_COMMENT = "#\\|" + "(" + PRINT_ASCII_NO_NL + "|\\n|\\t|\\r)*" + "\\|#";

        // diretivas/prefixos
        final String KW_LANG = "#lang";
        final String HASH_SEMI = "#;";
        final String VECTOR_START = "#\\(";

        // números
        final String SIGN = "(-|\\+)?";
        final String REAL_PART = DIGIT + "+(\\." + DIGIT + "+)?";
        final String INT = SIGN + DIGIT + "+";
        final String FLOAT = SIGN + DIGIT + "+\\." + DIGIT + "+";

        // notação científica
        final String SCI = SIGN + REAL_PART + "(e|E)" + SIGN + DIGIT + "+";

        // fração
        final String RATIO = SIGN + DIGIT + "+/" + DIGIT + "+";

        // numeros complexos
        final String COMPLEX = SIGN + REAL_PART + "(\\+|-)" + REAL_PART + "i";
        final String IMAG = SIGN + REAL_PART + "i";

        // strings
        final String BACKSLASH = "\\\\"; // literal '\\'
        final String QUOTE_DQ = "\"";
        final String ESC_QUOTE = BACKSLASH + QUOTE_DQ;

        final String STR_CHAR = "([ -!]|[#-~]|" + LATIN1 + ")";
        final String STRING_BODY = "(" + ESC_QUOTE + "|" + STR_CHAR + ")*";
        final String STRING = QUOTE_DQ + STRING_BODY + QUOTE_DQ;

        // chars
        final String CHAR_PREFIX = "#" + BACKSLASH;
        final String CHAR_NAME = "(space|newline|tab|return)";
        final String CHAR_SINGLE = "(" + STR_CHAR + ")";
        final String CHAR = CHAR_PREFIX + "(" + CHAR_NAME + "|" + CHAR_SINGLE + ")";

        // pontuação/sintaxe
        final String LPAREN = "\\(";
        final String RPAREN = "\\)";
        final String LBRACK = "\\[";
        final String RBRACK = "\\]";
        final String LBRACE = "\\{";
        final String RBRACE = "\\}";
        final String QUOTE = "'";
        final String QUASIQUOTE = "`";
        final String COMMA_AT = ",@";
        final String COMMA = ",";

        // booleanos
        final String TRUE = "#t";
        final String FALSE = "#f";

        // números com radix
        final String BIN_DIGIT = "[0-1]";
        final String OCT_DIGIT = "[0-7]";
        final String HEX_LOWER = "[a-f]";
        final String HEX_UPPER = "[A-F]";
        final String HEX_DIGIT = "(" + DIGIT + "|" + HEX_LOWER + "|" + HEX_UPPER + ")";
        final String BIN = "#b" + BIN_DIGIT + "+";
        final String OCT = "#o" + OCT_DIGIT + "+";
        final String DEC = "#d" + DIGIT + "+";
        final String HEX = "#x" + HEX_DIGIT + "+";

        // palavras-chave
        final String KW_DEFINE = "define";
        final String KW_LAMBDA = "lambda";
        final String KW_IF = "if";
        final String KW_BEGIN = "begin";
        final String KW_LET = "let";
        final String KW_QUOTE = "quote";

        // identificadores: letras e símbolos comuns
        final String IDENT_START = "(" + LETTER + "|_|-|\\+|\\*|/|<|>|=|\\?|!|$|%|&|:|\\^|~)";
        final String IDENT_REST = "(" + IDENT_START + "|" + DIGIT + "|\\.)*";
        final String IDENT = IDENT_START + IDENT_REST;


        // Prioridade = ordem na lista.
        List<ExpressaoRegular> expressoes = List.of(
            // SKIP
            new ExpressaoRegular(RegexUtils.infixaParaPosfixa(WS), "SKIP_WS"),
            new ExpressaoRegular(RegexUtils.infixaParaPosfixa(LINE_COMMENT), "SKIP_LINE_COMMENT"),
            new ExpressaoRegular(RegexUtils.infixaParaPosfixa(BLOCK_COMMENT), "SKIP_BLOCK_COMMENT"),

            // prefixos
            new ExpressaoRegular(RegexUtils.infixaParaPosfixa(KW_LANG), "KW_LANG"),
            new ExpressaoRegular(RegexUtils.infixaParaPosfixa(HASH_SEMI), "HASH_SEMI"),
            new ExpressaoRegular(RegexUtils.infixaParaPosfixa(VECTOR_START), "VECTOR_START"),

            // pontuação
            new ExpressaoRegular(RegexUtils.infixaParaPosfixa(LPAREN), "LPAREN"),
            new ExpressaoRegular(RegexUtils.infixaParaPosfixa(RPAREN), "RPAREN"),
            new ExpressaoRegular(RegexUtils.infixaParaPosfixa(LBRACK), "LBRACK"),
            new ExpressaoRegular(RegexUtils.infixaParaPosfixa(RBRACK), "RBRACK"),
            new ExpressaoRegular(RegexUtils.infixaParaPosfixa(LBRACE), "LBRACE"),
            new ExpressaoRegular(RegexUtils.infixaParaPosfixa(RBRACE), "RBRACE"),
            new ExpressaoRegular(RegexUtils.infixaParaPosfixa(QUOTE), "QUOTE"),
            new ExpressaoRegular(RegexUtils.infixaParaPosfixa(QUASIQUOTE), "QUASIQUOTE"),
            new ExpressaoRegular(RegexUtils.infixaParaPosfixa(COMMA_AT), "UNQUOTE_SPLICING"),
            new ExpressaoRegular(RegexUtils.infixaParaPosfixa(COMMA), "UNQUOTE"),

            // booleanos
            new ExpressaoRegular(RegexUtils.infixaParaPosfixa(TRUE), "BOOL_TRUE"),
            new ExpressaoRegular(RegexUtils.infixaParaPosfixa(FALSE), "BOOL_FALSE"),

            // números com radix
            new ExpressaoRegular(RegexUtils.infixaParaPosfixa(BIN), "BINARY"),
            new ExpressaoRegular(RegexUtils.infixaParaPosfixa(OCT), "OCTAL"),
            new ExpressaoRegular(RegexUtils.infixaParaPosfixa(DEC), "DECIMAL"),
            new ExpressaoRegular(RegexUtils.infixaParaPosfixa(HEX), "HEX"),

            // keywords
            new ExpressaoRegular(RegexUtils.infixaParaPosfixa(KW_DEFINE), "KW_DEFINE"),
            new ExpressaoRegular(RegexUtils.infixaParaPosfixa(KW_LAMBDA), "KW_LAMBDA"),
            new ExpressaoRegular(RegexUtils.infixaParaPosfixa(KW_IF), "KW_IF"),
            new ExpressaoRegular(RegexUtils.infixaParaPosfixa(KW_BEGIN), "KW_BEGIN"),
            new ExpressaoRegular(RegexUtils.infixaParaPosfixa(KW_LET), "KW_LET"),
            new ExpressaoRegular(RegexUtils.infixaParaPosfixa(KW_QUOTE), "KW_QUOTE"),

            // literais
            new ExpressaoRegular(RegexUtils.infixaParaPosfixa(STRING), "STRING"),
            new ExpressaoRegular(RegexUtils.infixaParaPosfixa(CHAR), "CHAR"),

            // números
            new ExpressaoRegular(RegexUtils.infixaParaPosfixa(COMPLEX), "COMPLEX"),
            new ExpressaoRegular(RegexUtils.infixaParaPosfixa(IMAG), "IMAG"),
            new ExpressaoRegular(RegexUtils.infixaParaPosfixa(SCI), "SCIENTIFIC"),
            new ExpressaoRegular(RegexUtils.infixaParaPosfixa(RATIO), "RATIO"),
            new ExpressaoRegular(RegexUtils.infixaParaPosfixa(FLOAT), "FLOAT"),
            new ExpressaoRegular(RegexUtils.infixaParaPosfixa(INT), "INT"),

            // identificadores
            new ExpressaoRegular(RegexUtils.infixaParaPosfixa(IDENT), "IDENT")
        );

        GeradorScanner scanner = new GeradorScanner(expressoes);

        Path baseDir = findBaseDirWithExemplos();
        Path inputPath = baseDir.resolve(Path.of("src", "exemplos", "teste.txt")).normalize();
        Path outputPath = baseDir.resolve(Path.of("src", "exemplos", "saida.txt")).normalize();
        try {
            String entrada = Files.readString(inputPath, StandardCharsets.UTF_8);
            var tokens = scanner.tokenizar(entrada);

            StringBuilder saida = new StringBuilder();
            for (var t : tokens) {
                if (t.tipo().equals("SKIP_WS")) {
                    continue;
                }
                saida.append(t.tipo()).append('\t').append(t.lexema()).append(System.lineSeparator());
            }

            ensureParentDir(outputPath);
            Files.writeString(
                    outputPath,
                    saida.toString(),
                    StandardCharsets.UTF_8,
                    StandardOpenOption.CREATE,
                    StandardOpenOption.TRUNCATE_EXISTING
            );
        } catch (IllegalArgumentException e) {
            try {
                ensureParentDir(outputPath);
                Files.writeString(
                        outputPath,
                        "ERRO: " + e.getMessage() + System.lineSeparator(),
                        StandardCharsets.UTF_8,
                        StandardOpenOption.CREATE,
                        StandardOpenOption.TRUNCATE_EXISTING
                );
            } catch (IOException io) {
                throw new RuntimeException("Falha ao escrever saida em '" + outputPath + "'", io);
            }
        } catch (IOException e) {
            try {
                ensureParentDir(outputPath);
                Files.writeString(
                        outputPath,
                        "ERRO: " + e.getMessage() + System.lineSeparator(),
                        StandardCharsets.UTF_8,
                        StandardOpenOption.CREATE,
                        StandardOpenOption.TRUNCATE_EXISTING
                );
            } catch (IOException io) {
                throw new RuntimeException("Falha ao escrever saida em '" + outputPath + "'", io);
            }
        }
    }

    private static Path findBaseDirWithExemplos() {
        Path cur = Path.of("").toAbsolutePath().normalize();
        for (int i = 0; i < 8; i++) {
            Path exemplosDir = cur.resolve(Path.of("src", "exemplos"));
            if (Files.isDirectory(exemplosDir)) {
                return cur;
            }
            Path parent = cur.getParent();
            if (parent == null) break;
            cur = parent;
        }
        return Path.of("").toAbsolutePath().normalize();
    }

    private static void ensureParentDir(Path path) throws IOException {
        Path parent = path.getParent();
        if (parent != null && !Files.exists(parent)) {
            Files.createDirectories(parent);
        }
    }
}