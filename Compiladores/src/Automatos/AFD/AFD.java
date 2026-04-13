package Automatos.AFD;

public class AFD {
    private final String estadoInicial;
    private final String[] estadosFinais;
    private final String[] alfabeto;
    private final String[][] transicoes;

    public AFD(String estadoInicial, String[] estadosFinais, String[] alfabeto, String[][] transicoes) {
        this.estadoInicial = estadoInicial;
        this.estadosFinais = estadosFinais;
        this.alfabeto = alfabeto;
        this.transicoes = transicoes;
    }

    public String getEstadoInicial() {
        return estadoInicial;
    }

    public String[] getEstadosFinais() {
        return estadosFinais;
    }

    public String[] getAlfabeto() {
        return alfabeto;
    }

    public String[][] getTransicoes() {
        return transicoes;
    }
}
