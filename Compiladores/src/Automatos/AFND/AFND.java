package Automatos.AFND;

import java.util.Map;

public class AFND {
    private final EstadoAFND estadoInicial;
    private final Map<Integer, EstadoAFND> estados;
    private final String[] alfabeto;

    public AFND(EstadoAFND estadoInicial, Map<Integer, EstadoAFND> estados, String[] alfabeto) {
        this.estadoInicial = estadoInicial;
        this.estados = estados;
        this.alfabeto = alfabeto;
    }

    public EstadoAFND getEstadoInicial() {
        return estadoInicial;
    }

    public Map<Integer, EstadoAFND> getEstados() {
        return estados;
    }

    public String[] getAlfabeto() {
        return alfabeto;
    }
}