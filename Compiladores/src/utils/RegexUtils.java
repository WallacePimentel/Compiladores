package utils;

import java.util.*;

public class RegexUtils {

    public static String infixaParaPosfixa(String regex) {
        if (regex == null || regex.isEmpty()) {
            throw new IllegalArgumentException("Expressão vazia");
        }

        String comConcat = inserirConcatenacao(regex);
        return converterParaPosfixa(comConcat);
    }

    private static String inserirConcatenacao(String regex) {
        StringBuilder sb = new StringBuilder();

        for (int i = 0; i < regex.length(); i++) {
            char c1 = regex.charAt(i);
            sb.append(c1);

            if (i + 1 < regex.length()) {
                char c2 = regex.charAt(i + 1);

                if (precisaConcatenar(c1, c2)) {
                    sb.append('.');
                }
            }
        }

        return sb.toString();
    }

    private static boolean precisaConcatenar(char c1, char c2) {
        boolean c1Valido =
                ehOperando(c1) ||
                        c1 == ')' ||
                        ehUnario(c1);

        boolean c2Valido =
                ehOperando(c2) ||
                        c2 == '(';

        return c1Valido && c2Valido;
    }

    private static String converterParaPosfixa(String regex) {
        StringBuilder output = new StringBuilder();
        Deque<Character> pilha = new ArrayDeque<>();

        for (char c : regex.toCharArray()) {

            if (ehOperando(c)) {
                output.append(c);
            }

            else if (c == '(') {
                pilha.push(c);
            }

            else if (c == ')') {
                while (!pilha.isEmpty() && pilha.peek() != '(') {
                    output.append(pilha.pop());
                }
                if (pilha.isEmpty()) {
                    throw new IllegalArgumentException("Parênteses desbalanceados");
                }
                pilha.pop();
            }

            else if (ehOperador(c)) {
                while (!pilha.isEmpty() && (
                        precedencia(pilha.peek()) > precedencia(c) ||
                                (precedencia(pilha.peek()) == precedencia(c) && !ehUnario(c))
                )) {
                    output.append(pilha.pop());
                }
                pilha.push(c);
            }

            else {
                throw new IllegalArgumentException("Símbolo inválido na regex: " + c);
            }
        }

        while (!pilha.isEmpty()) {
            char op = pilha.pop();
            if (op == '(' || op == ')') {
                throw new IllegalArgumentException("Parênteses desbalanceados");
            }
            output.append(op);
        }

        return output.toString();
    }


    private static boolean ehOperador(char c) {
        return c == '|' || c == '.' || c == '*' || c == '+' || c == '?';
    }

    private static boolean ehOperando(char c) {
        return !ehOperador(c) && c != '(' && c != ')';
    }

    private static boolean ehUnario(char c) {
        return c == '*' || c == '+' || c == '?';
    }

    private static int precedencia(char op) {
        return switch (op) {
            case '*', '+', '?' -> 3;
            case '.' -> 2;
            case '|' -> 1;
            default -> 0;
        };
    }
}