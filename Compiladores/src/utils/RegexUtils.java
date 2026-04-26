package utils;

import java.util.*;

public class RegexUtils {

    public static String infixaParaPosfixa(String regex) {
        if (regex == null || regex.isEmpty()) {
            throw new IllegalArgumentException("Expressão vazia");
        }

        List<String> tokens = tokenizarInfixa(regex);
        List<String> comConcat = inserirConcatenacao(tokens);
        return converterParaPosfixa(comConcat);
    }
    
    private static List<String> tokenizarInfixa(String regex) {
        List<String> out = new ArrayList<>();

        for (int i = 0; i < regex.length(); i++) {
            char c = regex.charAt(i);

            if (Character.isWhitespace(c)) {
                continue;
            }

            if (c == '\\') {
                if (i + 1 >= regex.length()) {
                    throw new IllegalArgumentException("Escape incompleto no fim da expressão");
                }
                char n = regex.charAt(++i);
                out.add("\\" + n);
                continue;
            }

            if (c == '[') {
                int end = regex.indexOf(']', i + 1);
                if (end < 0) {
                    throw new IllegalArgumentException("Classe de caracteres sem ']': " + regex.substring(i));
                }
                out.add(regex.substring(i, end + 1));
                i = end;
                continue;
            }

            // operadores e parênteses de agrupamento
            if (c == '(' || c == ')' || c == '|' || c == '.' || c == '*' || c == '+' || c == '?') {
                out.add(String.valueOf(c));
                continue;
            }

            // literal simples
            out.add(String.valueOf(c));
        }

        if (out.isEmpty()) {
            throw new IllegalArgumentException("Expressão vazia");
        }
        return out;
    }

    private static List<String> inserirConcatenacao(List<String> tokens) {
        List<String> out = new ArrayList<>();

        for (int i = 0; i < tokens.size(); i++) {
            String t1 = tokens.get(i);
            out.add(t1);

            if (i + 1 < tokens.size()) {
                String t2 = tokens.get(i + 1);
                if (precisaConcatenar(t1, t2)) {
                    out.add(".");
                }
            }
        }
        return out;
    }

    private static boolean precisaConcatenar(String t1, String t2) {
        boolean t1Valido =
                ehOperando(t1) ||
                        ")".equals(t1) ||
                        ehUnario(t1);

        boolean t2Valido =
                ehOperando(t2) ||
                        "(".equals(t2);

        return t1Valido && t2Valido;
    }

    private static String converterParaPosfixa(List<String> tokens) {
        StringBuilder output = new StringBuilder();
        Deque<String> pilha = new ArrayDeque<>();

        for (String tk : tokens) {

            if (ehOperando(tk)) {
                output.append(tk);
            }

            else if ("(".equals(tk)) {
                pilha.push(tk);
            }

            else if (")".equals(tk)) {
                while (!pilha.isEmpty() && !"(".equals(pilha.peek())) {
                    output.append(pilha.pop());
                }
                if (pilha.isEmpty()) {
                    throw new IllegalArgumentException("Parênteses desbalanceados");
                }
                pilha.pop();
            }

            else if (ehOperador(tk)) {
                while (!pilha.isEmpty() && ehOperador(pilha.peek()) && (
                        precedencia(pilha.peek()) > precedencia(tk) ||
                                (precedencia(pilha.peek()) == precedencia(tk) && !ehUnario(tk))
                )) {
                    output.append(pilha.pop());
                }
                pilha.push(tk);
            }

            else {
                throw new IllegalArgumentException("Token inválido na regex: " + tk);
            }
        }

        while (!pilha.isEmpty()) {
            String op = pilha.pop();
            if ("(".equals(op) || ")".equals(op)) {
                throw new IllegalArgumentException("Parênteses desbalanceados");
            }
            output.append(op);
        }

        return output.toString();
    }


    private static boolean ehOperador(String tk) {
        return tk.length() == 1 && "|.*+?".indexOf(tk.charAt(0)) >= 0;
    }

    private static boolean ehOperando(String tk) {
        return !ehOperador(tk) && !"(".equals(tk) && !")".equals(tk);
    }

    private static boolean ehUnario(String tk) {
        return "*".equals(tk) || "+".equals(tk) || "?".equals(tk);
    }

    private static int precedencia(String tk) {
        char op = tk.charAt(0);
        return switch (op) {
            case '*', '+', '?' -> 3;
            case '.' -> 2;
            case '|' -> 1;
            default -> 0;
        };
    }
}