import GeradorScanner.GeradorScanner;
import ExpressoesRegulares.ExpressaoRegular;
import utils.RegexUtils;

import java.util.*;

public class Main {
    public static void main(String[] args) {

        // --- Alfabeto base (simplificado) ---
        final String DIGIT = "(0|1|2|3|4|5|6|7|8|9)";
        final String LETTER = "(a|b|c|d|e|f|g|h|i|j|k|l|m|n|o|p|q|r|s|t|u|v|w|x|y|z)";

        // Observação: este projeto usa uma notação própria de ER (ex.: '+' e '*' e '@').
        // Aqui, mantemos um subconjunto de tokens típicos de Racket/Scheme.

        // --- Tokens de Racket (subset) ---
        // números: inteiro e float com separador '@' (conforme seu projeto)
        final String INT = DIGIT + "+";
        final String FLOAT = DIGIT + "+@" + DIGIT + "+";

        // strings: " ... " (conteúdo simplificado para letras/dígitos apenas)
        // Como não há suporte óbvio a escape/qualquer-char no alfabeto atual, mantemos simplificado.
        final String STRING_BODY = "(" + LETTER + "|" + DIGIT + ")";
        final String STRING = "\"" + STRING_BODY + "\"";

        // identificadores: começam com letra, seguido de letra/dígito
        // (Em Racket real, identificadores aceitam muito mais caracteres: !?+-*/<>= etc.)
        final String IDENT = LETTER + "(" + LETTER + "|" + DIGIT + ")*";

        // símbolos/pontuação
        final String LPAREN = "(";   // token "("
        final String RPAREN = ")";   // token ")"
        final String QUOTE = "'";    // token "'"

        // booleanos
        final String TRUE = "#t";
        final String FALSE = "#f";

        // palavras-chave (subset comum)
        final String KW_DEFINE = "define";
        final String KW_LAMBDA = "lambda";
        final String KW_IF = "if";
        final String KW_BEGIN = "begin";


        // Importante: prioridade = ordem na lista.
        // Keywords e tokens fixos vêm antes de IDENT.
        List<ExpressaoRegular> expressoes = List.of(
                // pontuação
//                new ExpressaoRegular(RegexUtils.infixaParaPosfixa(LPAREN), "LPAREN"),
//                new ExpressaoRegular(RegexUtils.infixaParaPosfixa(RPAREN), "RPAREN"),
                new ExpressaoRegular(RegexUtils.infixaParaPosfixa(QUOTE), "QUOTE"), // token de aspas simples (')

                // booleanos
                new ExpressaoRegular(RegexUtils.infixaParaPosfixa(TRUE), "BOOL_TRUE"), // token de booleano verdadeiro (#t)
                new ExpressaoRegular(RegexUtils.infixaParaPosfixa(FALSE), "BOOL_FALSE"), // token de booleano falso (#f)

                // keywords
                new ExpressaoRegular(RegexUtils.infixaParaPosfixa(KW_DEFINE), "KW_DEFINE"), // token de palavra-chave "define"
                new ExpressaoRegular(RegexUtils.infixaParaPosfixa(KW_LAMBDA), "KW_LAMBDA"), // token de palavra-chave "lambda"
                new ExpressaoRegular(RegexUtils.infixaParaPosfixa(KW_IF), "KW_IF"), // token de palavra-chave "if"
                new ExpressaoRegular(RegexUtils.infixaParaPosfixa(KW_BEGIN), "KW_BEGIN"), // token de palavra-chave "begin"

                // literais
                new ExpressaoRegular(RegexUtils.infixaParaPosfixa(STRING), "STRING"), // token de string (ex.: "hello123")

                // números (FLOAT antes de INT)
                new ExpressaoRegular(RegexUtils.infixaParaPosfixa(FLOAT), "FLOAT"), // token de número float (ex.: 3@14)
                new ExpressaoRegular(RegexUtils.infixaParaPosfixa(INT), "INT"), // token de número inteiro (ex.: 42)

                // identificadores (por último)
                new ExpressaoRegular(RegexUtils.infixaParaPosfixa(IDENT), "IDENT") // token de identificador (ex.: foo, bar123)
        );

        GeradorScanner scanner = new GeradorScanner(expressoes);

        System.out.println(scanner.getAfd().getEstados());


        System.out.println();
        System.out.println("Scanner pronto. Digite uma string para identificar o token (ENTER vazio para sair).\n");

        try (java.util.Scanner in = new java.util.Scanner(System.in)) {
            while (true) {
                System.out.print("> ");
                if (!in.hasNextLine()) break;

                String s = in.nextLine();
                if (s.isEmpty()) break;

                String[] tokens = s.trim().split("\\s+");
                for (String t : tokens) {
                    var token = scanner.identificarToken(t);

                    if (token.isPresent()) {
                        System.out.println("TOKEN: " + token.get());
                    } else {
                        System.out.println("TOKEN: (nenhum) - string rejeitada");
                    }
                }
            }
        }
    }
}