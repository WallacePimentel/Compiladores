import GeradorScanner.GeradorScanner;
import GeradorScanner.TokenTranslator;
import ExpressoesRegulares.ExpressaoRegular;
import Parsers.ParserTopDown.ParserTopDown;
import Parsers.ParserTopDown.utils.AstVisualizer;
import Parsers.ParserTopDown.utils.ParseResult;
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
        final String DOT = "\\.";

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
            new ExpressaoRegular(RegexUtils.infixaParaPosfixa(DOT), "DOT"),

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
        Path inputPath;
        Scanner console = new Scanner(System.in, StandardCharsets.UTF_8.name());
        inputPath = escolherArquivoDeTeste(console, baseDir);
        Path outputPath = baseDir.resolve(Path.of("src", "exemplos", "saida_scanner.txt")).normalize();
        Path outputParserPath = baseDir.resolve(Path.of("src", "exemplos", "saida_parser.txt")).normalize();
        Path outputUsuarioPath = baseDir.resolve(Path.of("src", "exemplos", "saida_usuario.txt")).normalize();
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

            // Parser Top-Down: tokens -> AST ou lista de erros
            ParseResult parseResult = ParserTopDown.parseTokens(tokens);
            StringBuilder saidaParser = new StringBuilder();
            if (parseResult.accepted()) {
                saidaParser.append("ACEITO").append(System.lineSeparator());
                saidaParser.append(AstVisualizer.render(parseResult.program())).append(System.lineSeparator());
            } else {
                saidaParser.append("ERROS").append(System.lineSeparator());
                for (var err : parseResult.errors()) {
                    saidaParser
                            .append("[")
                            .append(err.tokenIndex())
                            .append("] ")
                            .append(err.tokenType())
                            .append(" '")
                            .append(err.lexeme())
                            .append("': ")
                            .append(err.message())
                            .append(System.lineSeparator());
                }
            }

            ensureParentDir(outputParserPath);
            Files.writeString(
                    outputParserPath,
                    saidaParser.toString(),
                    StandardCharsets.UTF_8,
                    StandardOpenOption.CREATE,
                    StandardOpenOption.TRUNCATE_EXISTING
            );

            String saidaUsuario = generateUsuarioOutput(entrada, tokens, parseResult);
            ensureParentDir(outputUsuarioPath);
            Files.writeString(
                    outputUsuarioPath,
                    saidaUsuario,
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

                ensureParentDir(outputParserPath);
                Files.writeString(
                        outputParserPath,
                        "ERRO: " + e.getMessage() + System.lineSeparator(),
                        StandardCharsets.UTF_8,
                        StandardOpenOption.CREATE,
                        StandardOpenOption.TRUNCATE_EXISTING
                );

                ensureParentDir(outputUsuarioPath);
                Files.writeString(
                        outputUsuarioPath,
                        "ERRO NO SCANNER\n\nNão foi possível processar o arquivo de entrada.\n" +
                        "Mensagem: " + e.getMessage() + System.lineSeparator(),
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

                ensureParentDir(outputParserPath);
                Files.writeString(
                        outputParserPath,
                        "ERRO: " + e.getMessage() + System.lineSeparator(),
                        StandardCharsets.UTF_8,
                        StandardOpenOption.CREATE,
                        StandardOpenOption.TRUNCATE_EXISTING
                );

                ensureParentDir(outputUsuarioPath);
                Files.writeString(
                        outputUsuarioPath,
                        "ERRO AO LER O ARQUIVO\n\nNão foi possível ler o arquivo de entrada.\n" +
                        "Mensagem: " + e.getMessage() + System.lineSeparator(),
                        StandardCharsets.UTF_8,
                        StandardOpenOption.CREATE,
                        StandardOpenOption.TRUNCATE_EXISTING
                );
            } catch (IOException io) {
                throw new RuntimeException("Falha ao escrever saida em '" + outputPath + "'", io);
            }
        }
    }

    private static Path escolherArquivoDeTeste(Scanner console, Path baseDir) {
        // 1) escolher categoria: erro ou sucesso
        String categoria;
        while (true) {
            System.out.print("Escolha o tipo de teste (erro/sucesso): ");
            if (!console.hasNextLine()) {
                throw new IllegalStateException("Entrada encerrada antes de escolher o tipo de teste.");
            }
            String raw = console.nextLine().trim().toLowerCase(Locale.ROOT);

            if (raw.equals("erro") || raw.equals("e") || raw.equals("error")) {
                categoria = "erro";
                break;
            }
            if (raw.equals("sucesso") || raw.equals("s") || raw.equals("success")) {
                categoria = "sucesso";
                break;
            }
            System.out.println("Entrada invalida. Digite 'erro' ou 'sucesso'.");
        }

        // 2) escolher numero 1/2/3
        int n;
        while (true) {
            System.out.print("Escolha o numero do teste (1/2/3): ");
            if (!console.hasNextLine()) {
                throw new IllegalStateException("Entrada encerrada antes de escolher o numero do teste.");
            }
            String raw = console.nextLine().trim();
            try {
                n = Integer.parseInt(raw);
            } catch (NumberFormatException ex) {
                n = -1;
            }
            if (n >= 1 && n <= 3) {
                break;
            }
            System.out.println("Entrada invalida. Digite 1, 2 ou 3.");
        }

        String nomeArquivo = "teste_" + categoria + n + ".txt";
        return baseDir.resolve(Path.of("src", "exemplos", nomeArquivo)).normalize();
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

    private static String generateUsuarioOutput(String entrada, List<GeradorScanner.Token> tokens, ParseResult parseResult) {
        StringBuilder resultado = new StringBuilder();

        if (parseResult.accepted()) {
            resultado.append("PROGRAMA ACEITO").append(System.lineSeparator());
            resultado.append(System.lineSeparator());
            resultado.append("Seu programa foi processado com sucesso!").append(System.lineSeparator());
        } else {
            resultado.append("ERROS ENCONTRADOS").append(System.lineSeparator());
            resultado.append(System.lineSeparator());

            String[] linhas = entrada.split("\n", -1);

            for (var err : parseResult.errors()) {
                resultado.append("---").append(System.lineSeparator());
                
                int linhaDoErro = resolverLinhaErro(tokens, err.tokenIndex(), err.tokenType(), err.message());
                
                resultado.append("Linha ").append(linhaDoErro).append(": ");
                
                if (linhaDoErro > 0 && linhaDoErro <= linhas.length) {
                    String linhaTexto = linhas[linhaDoErro - 1];
                    resultado.append(linhaTexto).append(System.lineSeparator());
                } else {
                    resultado.append("[fim do arquivo]").append(System.lineSeparator());
                }
                
                resultado.append(System.lineSeparator());
                
                String mensagemTraduzida = TokenTranslator.translateErrorMessage(err.message());
                resultado.append("Problema: ").append(mensagemTraduzida).append(System.lineSeparator());
                
                String tokenDesc = TokenTranslator.describeTokenWithLexeme(err.tokenType(), err.lexeme());
                resultado.append("Token recebido: ").append(tokenDesc).append(System.lineSeparator());
                resultado.append(System.lineSeparator());
            }
        }

        return resultado.toString();
    }

    private static int resolverLinhaErro(List<GeradorScanner.Token> tokens, int tokenIndex, String tokenType, String errorMessage) {
        List<GeradorScanner.Token> tokensNaoSkip = new ArrayList<>();
        for (var token : tokens) {
            if (!token.tipo().startsWith("SKIP_")) {
                tokensNaoSkip.add(token);
            }
        }

        if (tokensNaoSkip.isEmpty()) {
            return 1;
        }

        if (tokenType != null && tokenType.equals("<EOF>")) {
            if (errorMessage.contains("vetor")) {
                int linhaAbertura = encontrarAberturaNaoFechada(tokensNaoSkip, "VECTOR_START");
                if (linhaAbertura >= 0) {
                    return tokensNaoSkip.get(linhaAbertura).linha();
                }
            }
            
            String expectedClose = extrairTokenEsperado(errorMessage);
            if (expectedClose != null) {
                String expectedOpen = getCorrespondingOpeningDelimiter(expectedClose);
                int linhaAbertura = encontrarAberturaNaoFechada(tokensNaoSkip, expectedOpen);
                if (linhaAbertura >= 0) {
                    return tokensNaoSkip.get(linhaAbertura).linha();
                }
            }
        }

        String tokenReal = extrairTokenRecebido(errorMessage);
        if (tokenReal != null) {
            int indiceToken = encontrarUltimaOcorrenciaAte(tokensNaoSkip, tokenReal, tokenIndex);
            if (indiceToken >= 0) {
                return tokensNaoSkip.get(indiceToken).linha();
            }
        }

        if (tokenIndex >= 0 && tokenIndex < tokensNaoSkip.size()) {
            return tokensNaoSkip.get(tokenIndex).linha();
        }

        return tokensNaoSkip.get(tokensNaoSkip.size() - 1).linha();
    }

    private static int encontrarUltimaOcorrenciaAte(List<GeradorScanner.Token> tokens, String tokenType, int tokenIndex) {
        int limite = Math.min(tokenIndex, tokens.size() - 1);
        for (int i = limite; i >= 0; i--) {
            if (tokens.get(i).tipo().equals(tokenType)) {
                return i;
            }
        }
        for (int i = tokens.size() - 1; i > limite; i--) {
            if (tokens.get(i).tipo().equals(tokenType)) {
                return i;
            }
        }
        return -1;
    }

    private static int encontrarAberturaNaoFechada(List<GeradorScanner.Token> tokens, String openingType) {
        List<Integer> pilha = new ArrayList<>();
        String closingType = switch (openingType) {
            case "LPAREN" -> "RPAREN";
            case "LBRACK" -> "RBRACK";
            case "LBRACE" -> "RBRACE";
            case "VECTOR_START" -> "RPAREN";
            default -> null;
        };

        if (closingType == null) {
            return -1;
        }

        for (int i = 0; i < tokens.size(); i++) {
            String tipo = tokens.get(i).tipo();
            if (tipo.equals(openingType)) {
                pilha.add(i);
            } else if (tipo.equals(closingType)) {
                if (!pilha.isEmpty()) {
                    pilha.remove(pilha.size() - 1);
                }
            }
        }

        if (pilha.isEmpty()) {
            return -1;
        }
        // Retorna o PRIMEIRO não-fechado
        return pilha.get(0);
    }

    private static String getCorrespondingOpeningDelimiter(String closingDelimiter) {
        return switch (closingDelimiter) {
            case "RPAREN" -> "LPAREN";
            case "RBRACK" -> "LBRACK";
            case "RBRACE" -> "LBRACE";
            default -> closingDelimiter;
        };
    }

    private static String extrairTokenEsperado(String message) {
        String[] tokens = {"RPAREN", "RBRACK", "RBRACE"};
        int idx = message.indexOf("esperado ");
        if (idx >= 0) {
            String trecho = message.substring(idx + "esperado ".length());
            for (String token : tokens) {
                if (trecho.contains(token)) {
                    return token;
                }
            }
        }
        return null;
    }

    private static String extrairTokenRecebido(String message) {
        String[] tokens = {
            "RPAREN", "LPAREN", "RBRACK", "LBRACK", "RBRACE", "LBRACE",
            "UNQUOTE_SPLICING", "UNQUOTE", "QUASIQUOTE", "QUOTE", "DOT"
        };

        int idx = message.indexOf("mas veio ");
        if (idx >= 0) {
            String trecho = message.substring(idx + "mas veio ".length());
            for (String token : tokens) {
                if (trecho.contains(token)) {
                    return token;
                }
            }
        }

        idx = message.indexOf(": ");
        if (idx >= 0) {
            String trecho = message.substring(idx + 2);
            for (String token : tokens) {
                if (trecho.contains(token)) {
                    return token;
                }
            }
        }

        return null;
    }
}