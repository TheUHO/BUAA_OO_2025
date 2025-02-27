public class Token {
    public enum Type {
        ADD, SUB, MUL, LPAREN, RPAREN, NUM, VAR, POW
    }
    
    private final Type type;
    private final String content;

    public Token(Type type, String content) {
        this.type = type;
        this.content = content;
    }

    public Type getType() {
        return type;
    }

    public String getContent() {
        return content;
    }
}