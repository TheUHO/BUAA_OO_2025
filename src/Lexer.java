
import java.util.ArrayList;

public class Lexer {
    private final ArrayList<Token> tokens = new ArrayList<>();
    private int index = 0;

    public Lexer(String input) {
        int pos = 0;
        while (pos < input.length()) {
            char currentChar = input.charAt(pos);
            if (currentChar == ' ' || currentChar == '\t') {
                pos++;
            } else if (Character.isDigit(currentChar)) {
                pos = handleDigit(input, pos);
            } else {
                pos = handleSpecialCharacters(input, pos, currentChar);
            }
        }
    }

    private int handleDigit(String input, int pos) {
        int digitPos = pos;
        StringBuilder sb = new StringBuilder();
        while (digitPos < input.length() && Character.isDigit(input.charAt(digitPos))) {
            sb.append(input.charAt(pos));
            digitPos++;
        }
        tokens.add(new Token(Token.Type.NUM, sb.toString()));
        return digitPos;
    }

    private int handleSpecialCharacters(String input, int pos, char currentChar) {
        if (currentChar == '(') {
            tokens.add(new Token(Token.Type.LPAREN, "("));
        } else if (currentChar == ')') {
            tokens.add(new Token(Token.Type.RPAREN, ")"));
        } else if (currentChar == '+') {
            tokens.add(new Token(Token.Type.ADD, "+"));
        } else if (currentChar == '-') {
            tokens.add(new Token(Token.Type.SUB, "-"));
        } else if (currentChar == '*') {
            tokens.add(new Token(Token.Type.MUL, "*"));
        } else if (currentChar == 'x') {
            tokens.add(new Token(Token.Type.X, "x"));
        } else if (currentChar == '^') {
            tokens.add(new Token(Token.Type.POW, "^"));
        } else if (currentChar == 's' && input.startsWith("sin", pos)) {
            tokens.add(new Token(Token.Type.SIN, "sin"));
            return pos + 3;
        } else if (currentChar == 'c' && input.startsWith("cos", pos)) {
            tokens.add(new Token(Token.Type.COS, "cos"));
            return pos + 3;
        } else if (currentChar == 'f') {
            tokens.add(new Token(Token.Type.FUNC, "f"));
        } else if (currentChar == ',') {
            tokens.add(new Token(Token.Type.COMMA, ","));
        } else if (currentChar == 'y') {
            tokens.add(new Token(Token.Type.Y, "y"));
        } else if (currentChar == '{') {
            tokens.add(new Token(Token.Type.LBRACE, "{"));
        } else if (currentChar == '}') {
            tokens.add(new Token(Token.Type.RBRACE, "}"));
        } else if (currentChar == '=') {
            tokens.add(new Token(Token.Type.EQ, "="));
        }
        return pos + 1;
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
