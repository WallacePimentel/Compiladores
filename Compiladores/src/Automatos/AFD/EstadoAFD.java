package Automatos.AFD;

import java.util.Map;

public class EstadoAFD {
    private final String nome;
    private final boolean isFinal;
    private final Map<String, EstadoAFD> transicoes;

    public EstadoAFD(String nome, boolean isFinal, Map<String, EstadoAFD> transicoes) {
        this.nome = nome;
        this.isFinal = isFinal;
        this.transicoes = transicoes;
    }

    public String getNome() {
        return nome;
    }

    public boolean isFinal() {
        return isFinal;
    }
}
