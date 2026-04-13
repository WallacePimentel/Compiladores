package Automatos.AFND;

public class AFND {
    private final String estadoInicial;
    private final String[] estadosFinais;
    private final String[] alfabeto;
    private final EstadoAFND[][] transicoes;

    public AFND(String estadoInicial, String[] estadosFinais, String[] alfabeto, EstadoAFND[][] transicoes) {
        this.estadoInicial = estadoInicial;
        this.estadosFinais = estadosFinais;
        this.alfabeto = alfabeto;
        this.transicoes = transicoes;
    }
}
