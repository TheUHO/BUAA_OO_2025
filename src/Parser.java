import java.math.BigInteger;
import java.util.ArrayList;

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
        BigInteger termSign = parseSign(sign);
        Term term = new Term(termSign); // 传入term的符号，来自于expr，是term的属性
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
        } else if (token.getType() == Token.Type.X) {
            lexer.nextToken();
            return parsePower(factorSign);
        } else if (token.getType() == Token.Type.FUNC) {
            return parseFunction(factorSign);
        } else if (token.getType() == Token.Type.SIN) {
            return parseSinFactor(factorSign);
        }  else if (token.getType() == Token.Type.COS) {
            return parseCosFactor(factorSign);
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
            ((ExprFactor) subExpr).setExponent(parsePow());
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
        power.setExponent(parsePow()); // 解析指数
        return power;
    }

    public BigInteger parsePow() {
        if (!lexer.isEnd() && lexer.getCurToken().getType() == Token.Type.POW) { // 有指数，解析指数
            lexer.nextToken();
            BigInteger powSign = parseSign(BigInteger.ONE);
            if (powSign.equals(BigInteger.ONE)) { // 只允许正指数 
                BigInteger exponent = new BigInteger(lexer.getCurToken().getContent());
                lexer.nextToken();
                return exponent;
            } else { // 出现负指数，无法处理
                throw new RuntimeException("Negative exponent is not supported.");
            }
        } else {
            return BigInteger.ONE; // 没有指数，则默认指数为1
        }
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

    public FuncFactor parseFunction(BigInteger sign) {
        final String funcName = lexer.getCurToken().getContent();
        lexer.nextToken();
        if (lexer.getCurToken().getType() == Token.Type.LBRACE) {
            lexer.nextToken(); // 跳过左大括号
        }
        final BigInteger seq = parseNum(BigInteger.ONE).getValue(); // 序号
        if (lexer.getCurToken().getType() == Token.Type.RBRACE) {
            lexer.nextToken(); // 跳过右大括号
        }
        if (lexer.getCurToken().getType() == Token.Type.LPAREN) {
            lexer.nextToken(); // 跳过左括号
        }
        ArrayList<String> parameters = new ArrayList<>();
        while (lexer.getCurToken().getType() != Token.Type.RPAREN) {
            parameters.add(parseFactor().toPoly().toString());
            if (lexer.getCurToken().getType() == Token.Type.COMMA) {
                lexer.nextToken(); // 跳过逗号
            }
        }
        FuncFactor func = new FuncFactor(sign, funcName, seq, parameters);
        lexer.nextToken(); // 跳过右括号
        return func;
    }

    public SinFactor parseSinFactor(BigInteger sign) {
        SinFactor sin = new SinFactor(sign);
        if (lexer.getCurToken().getType() == Token.Type.SIN) {
            lexer.nextToken(); // 跳过sin
        } 
        if (lexer.getCurToken().getType() == Token.Type.LPAREN) {
            lexer.nextToken(); // 跳过左括号
        }
        Factor factor = parseFactor(); // 解析sin的参数
        sin.setFactor(factor);
        if (lexer.getCurToken().getType() == Token.Type.RPAREN) {
            lexer.nextToken(); // 跳过右括号
        }
        sin.setExponent(parsePow());
        return sin;
    }

    CosFactor parseCosFactor(BigInteger sign) {
        CosFactor cos = new CosFactor(sign);
        if (lexer.getCurToken().getType() == Token.Type.COS) {
            lexer.nextToken(); // 跳过cos
        } 
        if (lexer.getCurToken().getType() == Token.Type.LPAREN) {
            lexer.nextToken(); // 跳过左括号
        }
        Factor factor = parseFactor(); // 解析cos的参数
        cos.setFactor(factor);
        if (lexer.getCurToken().getType() == Token.Type.RPAREN) {
            lexer.nextToken(); // 跳过右括号
        }
        cos.setExponent(parsePow()); // 解析指数
        return cos;
    }
}