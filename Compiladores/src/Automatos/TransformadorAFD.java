package Automatos;

import Automatos.AFD.AFD;
import Automatos.AFD.EstadoAFD;
import Automatos.AFND.AFND;
import Automatos.AFND.EstadoAFND;

import java.util.*;

public class TransformadorAFD {
    private AFND afnd;
    private AFD afd;

    public TransformadorAFD(AFND afnd) {
        this.afnd = afnd;
        this.afd = transformar();
    }

    private AFD transformar() {
        Map<Set<EstadoAFND>, EstadoAFD> mapeamento = new HashMap<>();
        Queue<Set<EstadoAFND>> fila = new LinkedList<>();
        Map<String, EstadoAFD> estadosAFD = new HashMap<>();

        Set<EstadoAFND> estadoInicialAFND = fechoEpsilon(Set.of(afnd.getEstadoInicial()));
        String nomeEstadoInicial = gerarNomeEstado(estadoInicialAFND);

        FinalInfo finIni = calcularFinalInfo(estadoInicialAFND);
        EstadoAFD estadoInicialAFD = new EstadoAFD(
                nomeEstadoInicial,
                finIni.isFinal,
                new HashMap<>(),
                finIni.token,
                finIni.prioridade
        );

        mapeamento.put(estadoInicialAFND, estadoInicialAFD);
        estadosAFD.put(nomeEstadoInicial, estadoInicialAFD);
        fila.add(estadoInicialAFND);

        while (!fila.isEmpty()) {
            Set<EstadoAFND> conjuntoAtual = fila.poll();
            EstadoAFD estadoAtualAFD = mapeamento.get(conjuntoAtual);

            for (String simbolo : afnd.getAlfabeto()) {
                Set<EstadoAFND> proximoConjunto = calcularTransicao(conjuntoAtual, simbolo);

                if (!proximoConjunto.isEmpty()) {
                    if (!mapeamento.containsKey(proximoConjunto)) {
                        String nomeNovoEstado = gerarNomeEstado(proximoConjunto);
                        FinalInfo fin = calcularFinalInfo(proximoConjunto);
                        EstadoAFD novoEstadoAFD = new EstadoAFD(
                                nomeNovoEstado,
                                fin.isFinal,
                                new HashMap<>(),
                                fin.token,
                                fin.prioridade
                        );

                        mapeamento.put(proximoConjunto, novoEstadoAFD);
                        estadosAFD.put(nomeNovoEstado, novoEstadoAFD);
                        fila.add(proximoConjunto);
                    }

                    estadoAtualAFD.getTransicoes().put(simbolo, mapeamento.get(proximoConjunto));
                }
            }
        }

        return new AFD(estadoInicialAFD, estadosAFD, afnd.getAlfabeto());
    }

    private Set<EstadoAFND> fechoEpsilon(Set<EstadoAFND> estados) {
        Set<EstadoAFND> fecho = new HashSet<>(estados);
        Queue<EstadoAFND> fila = new LinkedList<>(estados);

        while (!fila.isEmpty()) {
            EstadoAFND estado = fila.poll();
            Set<EstadoAFND> proximosEstados = estado.getTransicoes().getOrDefault("ε", Set.of());

            for (EstadoAFND proximo : proximosEstados) {
                if (fecho.add(proximo)) {
                    fila.add(proximo);
                }
            }
        }

        return fecho;
    }

    private Set<EstadoAFND> calcularTransicao(Set<EstadoAFND> estados, String simbolo) {
        Set<EstadoAFND> proximos = new HashSet<>();

        for (EstadoAFND estado : estados) {
            Set<EstadoAFND> transicoes = estado.getTransicoes().getOrDefault(simbolo, Set.of());
            proximos.addAll(transicoes);
        }

        return fechoEpsilon(proximos);
    }

    private String gerarNomeEstado(Set<EstadoAFND> estados) {
        List<Integer> ids = estados.stream()
                .map(EstadoAFND::getId)
                .sorted()
                .toList();
        return "{" + String.join(",", ids.stream().map(String::valueOf).toList()) + "}";
    }

    private record FinalInfo(boolean isFinal, String token, int prioridade) {}

    private FinalInfo calcularFinalInfo(Set<EstadoAFND> estados) {
        String tokenEscolhido = null;
        int prioEscolhida = Integer.MAX_VALUE;

        for (EstadoAFND e : estados) {
            if (!e.isFinal()) continue;
            int prio = e.getPrioridade();
            if (prio < prioEscolhida) {
                prioEscolhida = prio;
                tokenEscolhido = e.getToken();
            }
        }

        boolean isFinal = tokenEscolhido != null;
        return new FinalInfo(isFinal, tokenEscolhido, prioEscolhida);
    }

    public AFD getAFD() {
        return afd;
    }
}