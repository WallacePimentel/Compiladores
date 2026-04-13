package Automatos.AFD;

import java.util.HashMap;
import java.util.Map;

public class AFD {
    private final EstadoAFD estadoInicial;
    private final Map<String, EstadoAFD> estados;
    private final String[] alfabeto;

    public AFD(EstadoAFD estadoInicial, Map<String, EstadoAFD> estados, String[] alfabeto) {
        this.estadoInicial = estadoInicial;
        this.estados = estados;
        this.alfabeto = alfabeto;
    }

    public EstadoAFD getEstadoInicial() {
        return estadoInicial;
    }

    public Map<String, EstadoAFD> getEstados() {
        return estados;
    }

    public String[] getAlfabeto() {
        return alfabeto;
    }
}