package GeradorScanner;

import Automatos.AFD.AFD;
import Automatos.AFD.EstadoAFD;
import Automatos.AFND.AFND;
import Automatos.AFND.AFNDBuilderThompson;
import Automatos.AFND.AFNDUniao;
import Automatos.TransformadorAFD;
import ExpressoesRegulares.ExpressaoRegular;

import java.util.*;

public class GeradorScanner {
    private final List<ExpressaoRegular> expressoes;
    private final AFD afd;

    public record Token(String tipo, String lexema) {}

    public GeradorScanner(List<ExpressaoRegular> expressoes) {
        if (expressoes == null || expressoes.isEmpty()) {
            throw new IllegalArgumentException("Lista de expressoes regulares vazia");
        }
        this.expressoes = List.copyOf(expressoes);

        // 1) AFND para cada ER (Thompson)
        AFNDBuilderThompson afndBuilder = new AFNDBuilderThompson();
        List<AFND> afnds = new ArrayList<>();
        for (int i = 0; i < this.expressoes.size(); i++) {
            ExpressaoRegular expressaoRegular = this.expressoes.get(i);
            final AFND afnd;
            try {
                afnd = afndBuilder.construir(expressaoRegular);
            } catch (RuntimeException e) {
                throw new IllegalArgumentException(
                        "Falha ao construir AFND para token '" + expressaoRegular.getToken() +
                                "' (posfixa='" + expressaoRegular.getExpressao() + "')",
                        e
                );
            }

            // prioridade = ordem declarada
            for (var estadoAFND : afnd.getEstados().values()) {
                if (estadoAFND.isFinal()) {
                    estadoAFND.setPrioridade(i);
                }
            }
            afnds.add(afnd);
        }

        // 2) União de todos AFNDs
        AFND afndUnido = AFNDUniao.unir(afnds);

        // 3) AFND -> AFD
        this.afd = new TransformadorAFD(afndUnido).getAFD();
    }

    public AFD getAfd() {
        return afd;
    }

    public List<Token> tokenizar(String entrada) {
        if (entrada == null || entrada.isEmpty()) return List.of();

        List<Token> out = new ArrayList<>();
        int i = 0;
        while (i < entrada.length()) {
            Match m = matchLongestAt(entrada, i);
            if (m == null) {
                char c = entrada.charAt(i);
                throw new IllegalArgumentException(
                        "ERRO: Nenhuma regra casa na posicao " + i + " (char '" + printable(c) + "')"
                );
            }

            String tipo = m.token;
            String lexema = entrada.substring(i, m.endExclusive);
            
            out.add(new Token(tipo, lexema));
            
            i = m.endExclusive;
        }

        return out;
    }

    private static String printable(char c) {
        return switch (c) {
            case '\n' -> "\\n";
            case '\r' -> "\\r";
            case '\t' -> "\\t";
            default -> String.valueOf(c);
        };
    }

    private record Match(String token, int endExclusive) {}

    private Match matchLongestAt(String input, int start) {
        EstadoAFD estado = afd.getEstadoInicial();

        int i = start;
        int lastAccept = -1;
        String lastToken = null;

        while (i < input.length()) {
            String s = String.valueOf(input.charAt(i));
            EstadoAFD prox = estado.getTransicoes().get(s);
            if (prox == null) break;

            estado = prox;
            i++;

            if (estado.isFinal()) {
                lastAccept = i;
                lastToken = estado.getToken();
            }
        }

        if (lastAccept < 0) return null;
        return new Match(lastToken, lastAccept);
    }
}
