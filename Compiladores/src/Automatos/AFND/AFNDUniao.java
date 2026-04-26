package Automatos.AFND;

import java.util.*;

public final class AFNDUniao {
    private AFNDUniao() {}

    public static AFND unir(List<AFND> afnds) {
        if (afnds == null || afnds.isEmpty()) {
            throw new IllegalArgumentException("Lista de AFNDs vazia");
        }

        // alfabeto = união de alfabetos
        Set<String> alfabeto = new LinkedHashSet<>();
        for (AFND a : afnds) alfabeto.addAll(Arrays.asList(a.getAlfabeto()));

        Map<Integer, EstadoAFND> estadosNovos = new HashMap<>();

        int nextId = 0;
        // novo inicial
        EstadoAFND novoInicial = new EstadoAFND(nextId++, false, new HashMap<>());
        estadosNovos.put(novoInicial.getId(), novoInicial);

        for (AFND original : afnds) {
            // remapeia ids
            Map<EstadoAFND, EstadoAFND> map = new HashMap<>();
            for (EstadoAFND e : original.getEstados().values()) {
                EstadoAFND c = new EstadoAFND(nextId++, e.isFinal(), new HashMap<>());
                c.setToken(e.getToken());
                c.setPrioridade(e.getPrioridade());
                map.put(e, c);
                estadosNovos.put(c.getId(), c);
            }

            // copia transições
            for (EstadoAFND e : original.getEstados().values()) {
                EstadoAFND c = map.get(e);
                for (var ent : e.getTransicoes().entrySet()) {
                    String simb = ent.getKey();
                    for (EstadoAFND dest : ent.getValue()) {
                        c.getTransicoes().computeIfAbsent(simb, k -> new HashSet<>()).add(map.get(dest));
                    }
                }
            }

            // conecta novoInicial -> ε -> inicial do AFND corrente
            EstadoAFND iniCopiado = map.get(original.getEstadoInicial());
            novoInicial.getTransicoes().computeIfAbsent(AFNDBuilderThompson.EPSILON, k -> new HashSet<>()).add(iniCopiado);
        }

        return new AFND(novoInicial, estadosNovos, alfabeto.toArray(new String[0]));
    }
}

