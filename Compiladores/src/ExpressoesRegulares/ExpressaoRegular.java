package ExpressoesRegulares;

public class ExpressaoRegular {
    private final String expressao;
    private final String token;

    public ExpressaoRegular(String expressao, String token) {
        this.expressao = expressao;
        this.token = token;
    }

    public String getExpressao() {
        return expressao;
    }
    public String getToken() {
        return token;
    }
}
