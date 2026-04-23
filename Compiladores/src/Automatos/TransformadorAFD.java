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

        // Estado inicial do AFD é o fecho epsilon do estado inicial do AFND
        Set<EstadoAFND> estadoInicialAFND = fechoEpsilon(Set.of(afnd.getEstadoInicial()));
        String nomeEstadoInicial = gerarNomeEstado(estadoInicialAFND);
        EstadoAFD estadoInicialAFD = new EstadoAFD(nomeEstadoInicial, verificarFinal(estadoInicialAFND), new HashMap<>());

        mapeamento.put(estadoInicialAFND, estadoInicialAFD);
        estadosAFD.put(nomeEstadoInicial, estadoInicialAFD);
        fila.add(estadoInicialAFND);

        // Subset construction
        while (!fila.isEmpty()) {
            Set<EstadoAFND> conjuntoAtual = fila.poll();
            EstadoAFD estadoAtualAFD = mapeamento.get(conjuntoAtual);

            for (String simbolo : afnd.getAlfabeto()) {
                Set<EstadoAFND> proximoConjunto = calcularTransicao(conjuntoAtual, simbolo);

                if (!proximoConjunto.isEmpty()) {
                    if (!mapeamento.containsKey(proximoConjunto)) {
                        String nomeNovoEstado = gerarNomeEstado(proximoConjunto);
                        EstadoAFD novoEstadoAFD = new EstadoAFD(nomeNovoEstado, verificarFinal(proximoConjunto), new HashMap<>());

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

    private boolean verificarFinal(Set<EstadoAFND> estados) {
        return estados.stream().anyMatch(EstadoAFND::isFinal);
    }

    private String gerarNomeEstado(Set<EstadoAFND> estados) {
        List<Integer> ids = estados.stream()
                .map(EstadoAFND::getId)
                .sorted()
                .toList();
        return "{" + String.join(",", ids.stream().map(String::valueOf).toList()) + "}";
    }

    public AFD getAFD() {
        return afd;
    }
}