package Automatos.AFD;

import java.util.Map;
import java.util.Objects;

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

    public Map<String, EstadoAFD> getTransicoes() {
        return transicoes;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        EstadoAFD that = (EstadoAFD) o;
        return Objects.equals(nome, that.nome);
    }

    @Override
    public int hashCode() {
        return Objects.hash(nome);
    }
}