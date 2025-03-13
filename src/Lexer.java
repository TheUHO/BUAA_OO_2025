
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
            sb.append(input.charAt(digitPos));
            digitPos++;
        }
        tokens.add(new Token(Token.Type.NUM, sb.toString()));
        return digitPos;
    }

    private int handleSpecialCharacters(String input, int pos, char currentChar) {
        int charPos = pos;
        if (currentChar == '(') {
            tokens.add(new Token(Token.Type.LPAREN, "("));
        } else if (currentChar == ')') {
            tokens.add(new Token(Token.Type.RPAREN, ")"));
        } else if (currentChar == '+' || currentChar == '-') { // 处理多个连续符号
            int count = 0;
            while (charPos < input.length() && 
                (input.charAt(charPos) == '+' || input.charAt(charPos) == '-')) {
                if (input.charAt(charPos) == '-') {
                    count++;
                }
                charPos++;
            }
            if (count % 2 == 0) {
                tokens.add(new Token(Token.Type.ADD, "+"));
            } else {
                tokens.add(new Token(Token.Type.SUB, "-"));
            }
            charPos--;
        } else if (currentChar == '*') {
            tokens.add(new Token(Token.Type.MUL, "*"));
        } else if (currentChar == 'x') {
            tokens.add(new Token(Token.Type.X, "x"));
        } else if (currentChar == '^') {
            tokens.add(new Token(Token.Type.POW, "^"));
        } else if (currentChar == 's' && input.startsWith("sin", charPos)) {
            tokens.add(new Token(Token.Type.SIN, "sin"));
            return charPos + 3;
        } else if (currentChar == 'c' && input.startsWith("cos", charPos)) {
            tokens.add(new Token(Token.Type.COS, "cos"));
            return charPos + 3;
        } else if (currentChar == 'd' && input.startsWith("dx", charPos)) {
            tokens.add(new Token(Token.Type.DX, "dx"));
            return charPos + 2;
        } else if (currentChar == 'f' || currentChar == 'g' || currentChar == 'h') {
            tokens.add(new Token(Token.Type.FUNC, String.valueOf(currentChar)));
        } else if (currentChar == ',') {
            tokens.add(new Token(Token.Type.COMMA, ","));
        } else if (currentChar == '{') {
            tokens.add(new Token(Token.Type.LBRACE, "{"));
        } else if (currentChar == '}') {
            tokens.add(new Token(Token.Type.RBRACE, "}"));
        }  else if (currentChar == 'y') {
            tokens.add(new Token(Token.Type.Y, "y"));
        }
        return charPos + 1;
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
