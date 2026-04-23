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

    public GeradorScanner(List<ExpressaoRegular> expressoes) {
        if (expressoes == null || expressoes.isEmpty()) {
            throw new IllegalArgumentException("Lista de expressoes regulares vazia");
        }
        this.expressoes = List.copyOf(expressoes);

        // 1) AFND para cada ER (Thompson)
        AFNDBuilderThompson AFNDBuilder = new AFNDBuilderThompson();
        List<AFND> afnds = new ArrayList<>();
        for (int i = 0; i < this.expressoes.size(); i++) {
            ExpressaoRegular expressaoRegular = this.expressoes.get(i);
            AFND afnd = AFNDBuilder.construir(expressaoRegular);
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

    /**
     * Identifica o token aceito pela string (match completo). Retorna vazio se não aceitar.
     */
    public Optional<String> identificarToken(String lexema) {
        if (lexema == null) return Optional.empty();

        EstadoAFD estado = afd.getEstadoInicial();
        for (int i = 0; i < lexema.length(); i++) {
            String s = String.valueOf(lexema.charAt(i));
            EstadoAFD prox = estado.getTransicoes().get(s);
            if (prox == null) return Optional.empty();
            estado = prox;
        }

        if (!estado.isFinal()) return Optional.empty();
        return Optional.ofNullable(estado.getToken());
    }
}
