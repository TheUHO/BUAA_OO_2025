import java.util.ArrayList;

public class Lexer {
    private final ArrayList<Token> tokens = new ArrayList<>();
    private int index = 0;

    public Lexer(String input) {
        int pos = 0;
        while (pos < input.length()) {
            char currentChar = input.charAt(pos);
            switch (currentChar) {
                case ' ':
                case '\t':
                    pos++;
                    break;
                case '(':
                    tokens.add(new Token(Token.Type.LPAREN, "("));
                    pos++;
                    break;
                case ')':
                    tokens.add(new Token(Token.Type.RPAREN, ")"));
                    pos++;
                    break;
                case '+':
                    tokens.add(new Token(Token.Type.ADD, "+"));
                    pos++;
                    break;
                case '-':
                    tokens.add(new Token(Token.Type.SUB, "-"));
                    pos++;
                    break;
                case '*':
                    tokens.add(new Token(Token.Type.MUL, "*"));
                    pos++;
                    break;
                case 'x':
                    tokens.add(new Token(Token.Type.VAR, "x"));
                    pos++;
                    break;
                case '^':
                    tokens.add(new Token(Token.Type.POW, "^"));
                    pos++;
                    break;
                default:
                    if (Character.isDigit(currentChar)) {
                        StringBuilder sb = new StringBuilder();
                        while (pos < input.length() && Character.isDigit(input.charAt(pos))) {
                            sb.append(input.charAt(pos));
                            pos++;
                        }
                        tokens.add(new Token(Token.Type.NUM, sb.toString()));
                    } else {
                        // Handle unexpected characters if necessary
                        pos++;
                    }
                    break;
            }
        }
    }

    public Token getCurToken() {
        return tokens.get(index);
    }

    public void nextToken() {
        index++;
    }

    public boolean isEnd() {
        return index >= tokens.size();
    }
}
