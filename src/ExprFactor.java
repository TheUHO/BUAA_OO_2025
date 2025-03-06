import java.math.BigInteger;

public class ExprFactor extends Expr implements Factor {

    private BigInteger sign;
    private BigInteger exponent;

    public ExprFactor(BigInteger sign) {
        this.sign = sign;
    }

    public BigInteger getSign() {
        return sign;
    }

    @Override
    public Poly toPoly() {
        if (exponent.equals(BigInteger.ZERO)) {
            NumFactor signNumFactor = new NumFactor(sign, BigInteger.ONE);
            return signNumFactor.toPoly();
        } else {
            // 处理表达式
            Poly exprPoly = new Poly();
            exprPoly = super.toPoly();
            // 处理指数
            Poly result = new Poly();
            result = result.addPoly(result, exprPoly);
            for (BigInteger i = BigInteger.ONE; i.compareTo(exponent) < 0;
                i = i.add(BigInteger.ONE)) {
                result = result.multiplyPoly(result, exprPoly);
            }
            return result;
        }
    }

    BigInteger getExponent() {
        return exponent;
    }

    public void setExponent(BigInteger exponent) {
        this.exponent = exponent;
    }
}
