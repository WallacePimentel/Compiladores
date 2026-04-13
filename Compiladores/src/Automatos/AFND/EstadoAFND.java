package Automatos.AFND;

import java.util.List;
import java.util.Map;

public class EstadoAFND {
    private final int id;
    private final boolean isFinal;
    private final Map<String, List<EstadoAFND>> transicoes;

    public EstadoAFND(int id, boolean isFinal, Map<String, List<EstadoAFND>> transicoes) {
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

    public Map<String, List<EstadoAFND>> getTransicoes() {
        return transicoes;
    }
}
