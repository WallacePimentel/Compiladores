package GeradorScanner;

import java.util.HashMap;
import java.util.Map;

public class TokenTranslator {
    private static final Map<String, String> TOKEN_DESCRIPTIONS = new HashMap<>();
    
    static {
        // Pontuação
        TOKEN_DESCRIPTIONS.put("LPAREN", "parêntese aberto '('");
        TOKEN_DESCRIPTIONS.put("RPAREN", "parêntese fechado ')'");
        TOKEN_DESCRIPTIONS.put("LBRACK", "colchete aberto '['");
        TOKEN_DESCRIPTIONS.put("RBRACK", "colchete fechado ']'");
        TOKEN_DESCRIPTIONS.put("LBRACE", "chave aberta '{'");
        TOKEN_DESCRIPTIONS.put("RBRACE", "chave fechada '}'");
        TOKEN_DESCRIPTIONS.put("QUOTE", "aspa simples de citação \"'\"");
        TOKEN_DESCRIPTIONS.put("QUASIQUOTE", "aspa de quase-citação \"`\"");
        TOKEN_DESCRIPTIONS.put("UNQUOTE_SPLICING", "desquote com splicing \",@\"");
        TOKEN_DESCRIPTIONS.put("UNQUOTE", "desquote \",\"");
        TOKEN_DESCRIPTIONS.put("DOT", "ponto '.'");
        TOKEN_DESCRIPTIONS.put("VECTOR_START", "início de vetor \"#(\"");
        
        // Palavras-chave
        TOKEN_DESCRIPTIONS.put("KW_DEFINE", "palavra-chave 'define'");
        TOKEN_DESCRIPTIONS.put("KW_LAMBDA", "palavra-chave 'lambda'");
        TOKEN_DESCRIPTIONS.put("KW_IF", "palavra-chave 'if'");
        TOKEN_DESCRIPTIONS.put("KW_BEGIN", "palavra-chave 'begin'");
        TOKEN_DESCRIPTIONS.put("KW_LET", "palavra-chave 'let'");
        TOKEN_DESCRIPTIONS.put("KW_QUOTE", "palavra-chave 'quote'");
        TOKEN_DESCRIPTIONS.put("KW_LANG", "diretiva '#lang'");
        
        // Booleanos
        TOKEN_DESCRIPTIONS.put("BOOL_TRUE", "booleano verdadeiro '#t'");
        TOKEN_DESCRIPTIONS.put("BOOL_FALSE", "booleano falso '#f'");
        
        // Números
        TOKEN_DESCRIPTIONS.put("INT", "número inteiro");
        TOKEN_DESCRIPTIONS.put("FLOAT", "número em ponto flutuante");
        TOKEN_DESCRIPTIONS.put("COMPLEX", "número complexo");
        TOKEN_DESCRIPTIONS.put("IMAG", "número imaginário");
        TOKEN_DESCRIPTIONS.put("SCIENTIFIC", "número em notação científica");
        TOKEN_DESCRIPTIONS.put("RATIO", "número racional (fração)");
        TOKEN_DESCRIPTIONS.put("BINARY", "número binário");
        TOKEN_DESCRIPTIONS.put("OCTAL", "número octal");
        TOKEN_DESCRIPTIONS.put("DECIMAL", "número decimal");
        TOKEN_DESCRIPTIONS.put("HEX", "número hexadecimal");
        
        // Literais
        TOKEN_DESCRIPTIONS.put("STRING", "string (cadeia de caracteres)");
        TOKEN_DESCRIPTIONS.put("CHAR", "caractere");
        
        // Identificadores
        TOKEN_DESCRIPTIONS.put("IDENT", "identificador");
        
        // Comentários (normalmente ignorados)
        TOKEN_DESCRIPTIONS.put("SKIP_WS", "espaço em branco");
        TOKEN_DESCRIPTIONS.put("SKIP_LINE_COMMENT", "comentário de linha");
        TOKEN_DESCRIPTIONS.put("SKIP_BLOCK_COMMENT", "comentário de bloco");
        
        // Especiais
        TOKEN_DESCRIPTIONS.put("HASH_SEMI", "comentário de datum \"#;\"");
        TOKEN_DESCRIPTIONS.put("<EOF>", "fim do arquivo");
    }
    
    public static String describeToken(String tokenType) {
        return TOKEN_DESCRIPTIONS.getOrDefault(tokenType, tokenType);
    }
    
    public static String describeTokenWithLexeme(String tokenType, String lexeme) {
        String description = describeToken(tokenType);
        
        if (tokenType.equals("LPAREN") || tokenType.equals("RPAREN") || 
            tokenType.equals("LBRACK") || tokenType.equals("RBRACK") ||
            tokenType.equals("LBRACE") || tokenType.equals("RBRACE") ||
            tokenType.equals("QUOTE") || tokenType.equals("QUASIQUOTE") ||
            tokenType.equals("UNQUOTE_SPLICING") || tokenType.equals("UNQUOTE") ||
            tokenType.equals("DOT") || tokenType.equals("VECTOR_START") ||
            tokenType.equals("HASH_SEMI") || tokenType.equals("KW_LANG") ||
            tokenType.equals("BOOL_TRUE") || tokenType.equals("BOOL_FALSE")) {
            return description;
        }
        
        return description + " \"" + escapeLexeme(lexeme) + "\"";
    }
    
    private static String escapeLexeme(String lexeme) {
        return lexeme
            .replace("\\", "\\\\")
            .replace("\n", "\\n")
            .replace("\r", "\\r")
            .replace("\t", "\\t");
    }

    public static String translateErrorMessage(String message) {
        String[] tokens = {
            "RPAREN", "LPAREN", "RBRACK", "LBRACK", "RBRACE", "LBRACE",
            "UNQUOTE_SPLICING", "UNQUOTE", "QUASIQUOTE", "QUOTE", "DOT",
            "EOF", "VECTOR_START", "HASH_SEMI"
        };
        
        String resultado = message;
        for (String token : tokens) {
            String descricao = describeToken(token);
            // Substitui o token pelo token com aspas removidas (sem incluir aspas redundantes)
            resultado = resultado.replace(token, descricao);
        }
        return resultado;
    }
}
