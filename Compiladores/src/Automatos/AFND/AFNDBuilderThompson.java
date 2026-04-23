package Automatos.AFND;

import ExpressoesRegulares.ExpressaoRegular;

import java.util.*;

/**
 * Constrói AFND (com ε) a partir de expressão regular em pós-fixa (notação polonesa reversa)
 * via construção de Thompson.
 *
 * Convenções de operadores (pós-fixa):
 * - '.'  concatenação
 * - '|'  união
 * - '*'  fecho de Kleene
 * - '+'  um ou mais
 * - '?'  opcional
 *
 * Operandos:
 * - literais (1 caractere) ou escapes (ex.: \", \\, \\n)
 * - classes de caracteres: [a-z], [0-9] (sem negar ^)
 *
 * Observação: o caractere '.' é reservado como operador; para ponto literal use escape: \\.
 */
public final class AFNDBuilderThompson {
    public static final String EPSILON = "ε";

    public record Fragmento(EstadoAFND inicio, Set<EstadoAFND> saidas) {}

    private int nextId = 0;

    public AFND construir(ExpressaoRegular er) {
        return construir(er.getExpressao(), er.getToken());
    }

    public AFND construir(String posfixa, String token) {
        Objects.requireNonNull(posfixa, "posfixa");
        Deque<Fragmento> pilha = new ArrayDeque<>();

        Map<Integer, EstadoAFND> estados = new HashMap<>();
        Set<String> alfabeto = new LinkedHashSet<>();

        List<String> tokens = tokenizar(posfixa);
        for (String tk : tokens) {
            if (ehOperador(tk)) {
                char ch = tk.charAt(0);
                switch (ch) {
                    case '.': {
                        Fragmento b = pop(pilha, '.');
                        Fragmento a = pop(pilha, '.');
                        for (EstadoAFND out : a.saidas) {
                            addTransicao(out, EPSILON, b.inicio);
                        }
                        pilha.push(new Fragmento(a.inicio, b.saidas));
                        break;
                    }
                    case '|': {
                        Fragmento b = pop(pilha, '|');
                        Fragmento a = pop(pilha, '|');
                        EstadoAFND s = novoEstado(estados);
                        EstadoAFND t = novoEstado(estados);

                        addTransicao(s, EPSILON, a.inicio);
                        addTransicao(s, EPSILON, b.inicio);

                        for (EstadoAFND out : a.saidas) addTransicao(out, EPSILON, t);
                        for (EstadoAFND out : b.saidas) addTransicao(out, EPSILON, t);

                        pilha.push(new Fragmento(s, Set.of(t)));
                        break;
                    }
                    case '*': {
                        Fragmento a = pop(pilha, '*');
                        EstadoAFND s = novoEstado(estados);
                        EstadoAFND t = novoEstado(estados);

                        addTransicao(s, EPSILON, a.inicio);
                        addTransicao(s, EPSILON, t);

                        for (EstadoAFND out : a.saidas) {
                            addTransicao(out, EPSILON, a.inicio);
                            addTransicao(out, EPSILON, t);
                        }

                        pilha.push(new Fragmento(s, Set.of(t)));
                        break;
                    }
                    case '+': {
                        Fragmento a = pop(pilha, '+');
                        EstadoAFND s = novoEstado(estados);
                        EstadoAFND t = novoEstado(estados);

                        addTransicao(s, EPSILON, a.inicio);
                        for (EstadoAFND out : a.saidas) {
                            addTransicao(out, EPSILON, a.inicio);
                            addTransicao(out, EPSILON, t);
                        }

                        pilha.push(new Fragmento(s, Set.of(t)));
                        break;
                    }
                    case '?': {
                        Fragmento a = pop(pilha, '?');
                        EstadoAFND s = novoEstado(estados);
                        EstadoAFND t = novoEstado(estados);

                        addTransicao(s, EPSILON, a.inicio);
                        addTransicao(s, EPSILON, t);
                        for (EstadoAFND out : a.saidas) addTransicao(out, EPSILON, t);

                        pilha.push(new Fragmento(s, Set.of(t)));
                        break;
                    }
                    default:
                        throw new IllegalStateException("Operador desconhecido: " + tk);
                }
            } else {
                // operando: literal (1 char)
                alfabeto.add(tk);

                EstadoAFND s = novoEstado(estados);
                EstadoAFND t = novoEstado(estados);
                addTransicao(s, tk, t);
                pilha.push(new Fragmento(s, Set.of(t)));
            }
        }

        if (pilha.size() != 1) {
            throw new IllegalArgumentException("Expressão pós-fixa inválida (sobraram fragmentos: " + pilha.size() + ")");
        }

        Fragmento frag = pilha.pop();

        for (EstadoAFND out : frag.saidas) {
            out.setFinal(true);
            out.setToken(token);
        }

        return new AFND(frag.inicio, estados, alfabeto.toArray(new String[0]));
    }

    private static boolean ehOperador(String tk) {
        return tk.length() == 1 && ".|*+?".indexOf(tk.charAt(0)) >= 0;
    }

    /** Tokeniza a string pós-fixa em: operadores (1 char) e operandos (literais/escapes/classes). */
    private static List<String> tokenizar(String s) {
        List<String> out = new ArrayList<>();
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            if (Character.isWhitespace(c)) continue;

            if (c == '\\') {
                if (i + 1 >= s.length()) throw new IllegalArgumentException("Escape incompleto no fim da expressão");
                char n = s.charAt(++i);
                out.add(String.valueOf(desescape(n)));
                continue;
            }

            if (c == '[') {
                int end = s.indexOf(']', i + 1);
                if (end < 0) throw new IllegalArgumentException("Classe de caracteres sem ']': " + s.substring(i));
                String cls = s.substring(i + 1, end);
                expandirClasse(cls, out);
                i = end;
                continue;
            }

            // operador ou literal simples
            out.add(String.valueOf(c));
        }
        return out;
    }

    private static char desescape(char n) {
        return switch (n) {
            case 'n' -> '\n';
            case 't' -> '\t';
            case 'r' -> '\r';
            case '\\' -> '\\';
            case '\'' -> '\'';
            case '"' -> '"';
            case '.' -> '.'; // permite ponto literal
            default -> n;
        };
    }

    /**
     * Expande somente padrões do tipo a-z ou 0-9; retorna como união pós-fixa "ab|c|...".
     * Em pós-fixa, a expansão de [a-z] vira uma sequência de literais e operadores '|' adicionados ao stream.
     */
    private static void expandirClasse(String cls, List<String> out) {
        // suportar exatamente X-Y
        if (cls.length() == 3 && cls.charAt(1) == '-') {
            char ini = cls.charAt(0);
            char fim = cls.charAt(2);
            if (ini > fim) throw new IllegalArgumentException("Range inválido: [" + cls + "]");

            // empilha literais e vai unindo: a b | c | d | ...
            boolean first = true;
            for (char ch = ini; ch <= fim; ch++) {
                out.add(String.valueOf(ch));
                if (!first) out.add("|");
                first = false;
            }
            return;
        }

        throw new IllegalArgumentException("Classe não suportada: [" + cls + "] (use [a-z] ou [0-9])");
    }

    private static Fragmento pop(Deque<Fragmento> pilha, char op) {
        Fragmento f = pilha.pollFirst();
        if (f == null) throw new IllegalArgumentException("Operador '" + op + "' sem operandos suficientes");
        return f;
    }

    private EstadoAFND novoEstado(Map<Integer, EstadoAFND> estados) {
        int id = nextId++;
        EstadoAFND e = new EstadoAFND(id, false, new HashMap<>());
        estados.put(id, e);
        return e;
    }

    private static void addTransicao(EstadoAFND de, String simbolo, EstadoAFND para) {
        de.getTransicoes().computeIfAbsent(simbolo, ignored -> new HashSet<>()).add(para);
    }
}

