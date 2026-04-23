import Automatos.AFD.AFD;
import Automatos.AFD.EstadoAFD;
import Automatos.AFND.AFND;
import Automatos.AFND.EstadoAFND;
import Automatos.TransformadorAFD;

import java.util.*;

public class Main {
    public static void main(String[] args) {
        System.out.println("═══════════════════════════════════════════════════════════");
        System.out.println("        CONVERSOR DE AFND PARA AFD");
        System.out.println("═══════════════════════════════════════════════════════════\n");

        // Criar AFND
        AFND afnd = criarAFNDExemplo();

        System.out.println(">>> AUTÔMATO FINITO NÃO DETERMINÍSTICO (AFND) <<<\n");
        imprimirAFND(afnd);

        // Transformar AFND em AFD
        TransformadorAFD transformador = new TransformadorAFD(afnd);
        AFD afd = transformador.getAFD();

        System.out.println("\n>>> AUTÔMATO FINITO DETERMINÍSTICO (AFD) <<<\n");
        imprimirAFD(afd);

        // Testar aceitação de strings
        System.out.println("\n═══════════════════════════════════════════════════════════");
        System.out.println("        TESTE DE ACEITAÇÃO");
        System.out.println("═══════════════════════════════════════════════════════════\n");

        String[] strings = {"abb", "aabb", "babb", "aaabbb", "ab", "abab", "asjdnjalsn", "abbbb"};
        for (String s : strings) {
            boolean aceita = testarCadeia(afd, s);
            System.out.printf("String '%s': %s\n", s, aceita ? "✓ ACEITA" : "✗ REJEITA");
        }
    }

    private static AFND criarAFNDExemplo() {
        String[] alfabeto = {"a", "b"};

        // Criar estados
        Map<Integer, EstadoAFND> estados = new HashMap<>();

        EstadoAFND q0 = new EstadoAFND(0, false, new HashMap<>());
        EstadoAFND q1 = new EstadoAFND(1, false, new HashMap<>());
        EstadoAFND q2 = new EstadoAFND(2, false, new HashMap<>());
        EstadoAFND q3 = new EstadoAFND(3, true, new HashMap<>());

        estados.put(0, q0);
        estados.put(1, q1);
        estados.put(2, q2);
        estados.put(3, q3);

        // Configurar transições para AFND
        q0.getTransicoes().put("a", new HashSet<>(List.of(q0, q1)));
        q0.getTransicoes().put("b", new HashSet<>(Collections.singletonList(q0)));

        q1.getTransicoes().put("a", new HashSet<>());
        q1.getTransicoes().put("b", new HashSet<>(Collections.singletonList(q2)));

        q2.getTransicoes().put("a", new HashSet<>());
        q2.getTransicoes().put("b", new HashSet<>(Collections.singletonList(q3)));

        q3.getTransicoes().put("a", new HashSet<>());
        q3.getTransicoes().put("b", new HashSet<>());

        return new AFND(q0, estados, alfabeto);
    }

    private static void imprimirAFND(AFND afnd) {
        System.out.printf("Alfabeto: %s\n", Arrays.toString(afnd.getAlfabeto()));
        System.out.printf("Estado Inicial: q%d\n\n", afnd.getEstadoInicial().getId());

        System.out.println("Estados e Transições:");
        System.out.println("┌─────┬──────────┬─────────────┐");
        System.out.println("│ Est │ Final?   │ Transições  │");
        System.out.println("├─────┼──────────┼─────────────┤");

        for (EstadoAFND estado : afnd.getEstados().values()) {
            String nome = "q" + estado.getId();
            String isFinal = estado.isFinal() ? "Sim" : "Não";
            StringBuilder transicoes = new StringBuilder();

            for (Map.Entry<String, Set<EstadoAFND>> entry : estado.getTransicoes().entrySet()) {
                if (!entry.getValue().isEmpty()) {
                    transicoes.append(entry.getKey()).append("→{");
                    transicoes.append(entry.getValue().stream()
                            .map(e -> "q" + e.getId())
                            .sorted()
                            .reduce((a, b) -> a + "," + b)
                            .orElse(""));
                    transicoes.append("} ");
                }
            }

            System.out.printf("│ %s  │ %-8s │ %s│\n", nome, isFinal, transicoes);
        }

        System.out.println("└─────┴──────────┴─────────────┘");
    }

    private static void imprimirAFD(AFD afd) {
        System.out.printf("Alfabeto: %s\n", Arrays.toString(afd.getAlfabeto()));
        System.out.printf("Estado Inicial: %s\n\n", afd.getEstadoInicial().getNome());

        System.out.println("Estados e Transições:");
        System.out.println("┌──────────────────┬──────────┬──────────────────────────┐");
        System.out.println("│ Estado           │ Final?   │ Transições               │");
        System.out.println("├──────────────────┼──────────┼──────────────────────────┤");

        for (EstadoAFD estado : afd.getEstados().values()) {
            String nome = estado.getNome();
            String isFinal = estado.isFinal() ? "Sim" : "Não";
            StringBuilder transicoes = new StringBuilder();

            for (Map.Entry<String, EstadoAFD> entry : estado.getTransicoes().entrySet()) {
                transicoes.append(entry.getKey()).append("→")
                        .append(entry.getValue().getNome()).append(" ");
            }

            System.out.printf("│ %-16s │ %-8s │ %-24s│\n",
                    nome, isFinal, transicoes.toString());
        }

        System.out.println("└──────────────────┴──────────┴──────────────────────────┘");
    }

    private static boolean testarCadeia(AFD afd, String cadeia) {
        EstadoAFD estadoAtual = afd.getEstadoInicial();

        for (char c : cadeia.toCharArray()) {
            String simbolo = String.valueOf(c);
            EstadoAFD proximoEstado = estadoAtual.getTransicoes().get(simbolo);

            if (proximoEstado == null) {
                return false;
            }
            estadoAtual = proximoEstado;
        }

        return estadoAtual.isFinal();
    }
}