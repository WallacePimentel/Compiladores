import GeradorScanner.GeradorScanner;
import ExpressoesRegulares.ExpressaoRegular;
import utils.RegexUtils;

import java.util.*;

public class Main {
    public static void main(String[] args) {

        // Base digit = (0|1|2|3)
        final String DIGIT = "(0|1|2|3|4|5|6|7|8|9)";
        final String INT = DIGIT + "+";

        final String FLOAT = DIGIT + "+@" + DIGIT + "+";

        final String CHAR = "(a|b|c|d|e|f|g|h|i|j|k|l|m|n|o|p|q|r|s|t|u|v|w|x|y|z)";

        final String STRING = CHAR + "(" + CHAR + "|" + DIGIT + ")*";

        System.out.println("Expressões regulares (posFixa):");
        System.out.println("INT: " + RegexUtils.infixaParaPosfixa(INT));
        System.out.println("FLOAT: " + RegexUtils.infixaParaPosfixa(FLOAT));
        System.out.println("CHAR: " + RegexUtils.infixaParaPosfixa(CHAR));
        System.out.println("STRING: " + RegexUtils.infixaParaPosfixa(STRING));
        List<ExpressaoRegular> expressoes = List.of(
                // palavras-chave (maior prioridade por vir primeiro)
                new ExpressaoRegular(RegexUtils.infixaParaPosfixa("if"), "KW_IF"),

                // números
                new ExpressaoRegular(RegexUtils.infixaParaPosfixa(FLOAT), "FLOAT"),
                new ExpressaoRegular(RegexUtils.infixaParaPosfixa(INT), "INT"),

                // literais
                new ExpressaoRegular(RegexUtils.infixaParaPosfixa(CHAR), "CHAR"),
                new ExpressaoRegular(RegexUtils.infixaParaPosfixa(STRING), "STRING")
        );

        GeradorScanner scanner = new GeradorScanner(expressoes);

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