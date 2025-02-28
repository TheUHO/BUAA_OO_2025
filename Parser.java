import java.math.BigInteger;

public class Parser {
    private final Lexer lexer;

    public Parser(Lexer lexer) {
        this.lexer = lexer;
    }

    public BigInteger parseSign(BigInteger sign) {
        if (!lexer.isEnd() && lexer.getCurToken().getType() == Token.Type.SUB) {
            lexer.nextToken();
            return sign.multiply(BigInteger.valueOf(-1));
        } else if (!lexer.isEnd() && lexer.getCurToken().getType() == Token.Type.ADD) {
            lexer.nextToken();
            return sign.multiply(BigInteger.valueOf(1));
        }
        // 没有移动Token指针，以适应省略符号的情况
        return sign.multiply(BigInteger.valueOf(1));
    }

    public Expr parseExpr() {
        BigInteger exprSign = parseSign(BigInteger.ONE);
        Expr expr = new Expr();
        // 解析term，符号由exprSign决定
        expr.addTerm(parseTerm(exprSign));
        while (!lexer.isEnd() && (lexer.getCurToken().getType() == Token.Type.ADD || 
        lexer.getCurToken().getType() == Token.Type.SUB)) {
            exprSign = parseSign(BigInteger.ONE);
            expr.addTerm(parseTerm(exprSign));
        }
        return expr;
    }

    public Term parseTerm(BigInteger sign) {
        Term term = new Term(sign); // 传入term的符号，来自于expr，是term的属性
        term.addFactor(parseFactor());
        while (!lexer.isEnd() && lexer.getCurToken().getType() == Token.Type.MUL) {
            lexer.nextToken();
            term.addFactor(parseFactor());
        }
        return term;
    }

    public Factor parseFactor() {
        // factor的符号解析统一在此处进行
        BigInteger factorSign = parseSign(BigInteger.ONE);
        Token token = lexer.getCurToken();
        if (token.getType() == Token.Type.NUM) {
            return parseNum(factorSign);
        } else if (token.getType() == Token.Type.VAR) {
            lexer.nextToken();
            return parsePower(factorSign);
        } else {
            Factor subExpr = new ExprFactor(factorSign);
            if (token.getType() == Token.Type.LPAREN) {
                lexer.nextToken();
                subExpr = parseExprFactor(factorSign);
            }
            if (lexer.getCurToken().getType() == Token.Type.RPAREN) {
                // 跳过右括号
                lexer.nextToken();
            }
            if (!lexer.isEnd() && lexer.getCurToken().getType() == Token.Type.POW) {
                // 有指数，解析指数
                lexer.nextToken();
                BigInteger powSign = parseSign(BigInteger.ONE);
                if (powSign.equals(BigInteger.ONE)) {
                    BigInteger exponent = new BigInteger(lexer.getCurToken().getContent());
                    lexer.nextToken();
                    ((ExprFactor) subExpr).setExponent(exponent);
                } else {
                    // 出现负指数，无法处理
                    throw new RuntimeException("Negative exponent is not supported.");
                }
            } else {
                // 没有指数，则默认指数为1
                ((ExprFactor) subExpr).setExponent(BigInteger.ONE);
            }
            return subExpr;
        }
    }

    public NumFactor parseNum(BigInteger sign) {
        NumFactor num;
        Token token = lexer.getCurToken();
        BigInteger value = new BigInteger(token.getContent());
        lexer.nextToken();
        num = new NumFactor(sign, value);
        return num;
    }

    public PowFactor parsePower(BigInteger sign) {
        PowFactor power = new PowFactor(sign);
        power.setBase(BigInteger.ONE); // 默认底数为1
        if (!lexer.isEnd() && lexer.getCurToken().getType() == Token.Type.POW) {
            // 有指数，解析指数
            lexer.nextToken();
            BigInteger powSign = parseSign(BigInteger.ONE);
            if (powSign.equals(BigInteger.ONE)) { 
                // 只允许正指数 
                BigInteger exponent = new BigInteger(lexer.getCurToken().getContent());
                lexer.nextToken();
                power.setExponent(exponent);
            } else {
                // 出现负指数，无法处理
                throw new RuntimeException("Negative exponent is not supported.");
            }
        } else {
            // 没有指数，则默认指数为1
            power.setExponent(BigInteger.ONE);
        }
    
        return power;
    }

    public ExprFactor parseExprFactor(BigInteger sign) {
        BigInteger exprSign = parseSign(sign);
        ExprFactor expr = new ExprFactor(sign);
        expr.addTerm(parseTerm(exprSign));
        while (!lexer.isEnd() && (lexer.getCurToken().getType() == Token.Type.ADD || 
        lexer.getCurToken().getType() == Token.Type.SUB)) {
            exprSign = parseSign(sign);
            expr.addTerm(parseTerm(exprSign));
        }
        return expr;
    }
}
