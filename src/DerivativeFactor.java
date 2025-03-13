import java.math.BigInteger;

public class DerivativeFactor implements Factor {
    private BigInteger sign;
    private Expr expr;

    public DerivativeFactor(BigInteger sign, Expr expr) {
        this.sign = sign;
        this.expr = expr;
    }

    @Override
    public Poly toPoly() {
        Poly exprPoly = new Poly();
        exprPoly = expr.toDerivative(); // 对表达式求导
        if (sign.equals(BigInteger.valueOf(-1))) {
            exprPoly = exprPoly.negPoly(exprPoly);
        }
        return exprPoly;
    }

    public Poly toDerivative() { // 对求导因子求导
        Poly poly = this.toPoly();
        String exprStr = poly.toString();
        Lexer lexer = new Lexer(exprStr);
        Parser parser = new Parser(lexer);
        Expr expr = parser.parseExpr(); // 解析得到求导后的表达式
        Poly exprPoly = expr.toDerivative();
        if (sign.equals(BigInteger.valueOf(-1))) {
            exprPoly = exprPoly.negPoly(exprPoly);
        }
        return exprPoly;
    }

}
