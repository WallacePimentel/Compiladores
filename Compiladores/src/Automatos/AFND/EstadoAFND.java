package Automatos.AFND;

import java.util.Map;
import java.util.Objects;
import java.util.Set;

public class EstadoAFND {
    private final int id;
    private boolean isFinal;
    private String token;
    private int prioridade = Integer.MAX_VALUE;

    private Map<String, Set<EstadoAFND>> transicoes;

    public EstadoAFND(int id, boolean isFinal, Map<String, Set<EstadoAFND>> transicoes) {
        this.id = id;
        this.isFinal = isFinal;
        this.transicoes = transicoes;
    }

    public int getId() {
        return id;
    }

    public boolean isFinal() {
        return isFinal;
    }

    public void setFinal(boolean aFinal) {
        isFinal = aFinal;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public int getPrioridade() {
        return prioridade;
    }

    public void setPrioridade(int prioridade) {
        this.prioridade = prioridade;
    }

    public void setTransicoes(Map<String, Set<EstadoAFND>> transicoes) {
        this.transicoes = transicoes;
    }

    public Map<String, Set<EstadoAFND>> getTransicoes() {
        return transicoes;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        EstadoAFND that = (EstadoAFND) o;
        return id == that.id;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}