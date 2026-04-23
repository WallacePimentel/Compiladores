package Automatos.AFD;

import java.util.Map;
import java.util.Objects;

public class EstadoAFD {
    private final String nome;
    private final boolean isFinal;
    private final Map<String, EstadoAFD> transicoes;

    /** Token escolhido quando este estado for final (p/ scanner). */
    private final String token;
    /** Prioridade do token (menor = maior prioridade). */
    private final int prioridade;

    public EstadoAFD(String nome, boolean isFinal, Map<String, EstadoAFD> transicoes) {
        this(nome, isFinal, transicoes, null, Integer.MAX_VALUE);
    }

    public EstadoAFD(String nome, boolean isFinal, Map<String, EstadoAFD> transicoes, String token, int prioridade) {
        this.nome = nome;
        this.isFinal = isFinal;
        this.transicoes = transicoes;
        this.token = token;
        this.prioridade = prioridade;
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

    public String getToken() {
        return token;
    }

    public int getPrioridade() {
        return prioridade;
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