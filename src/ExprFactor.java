import java.math.BigInteger;

public class ExprFactor extends Expr implements Factor {

    private BigInteger sign;
    private BigInteger exponent;
    private Poly polyCache;
    private boolean polyDirty = true;

    public ExprFactor(BigInteger sign) {
        this.sign = sign;
    }

    public BigInteger getSign() {
        return sign;
    }

    @Override
    public Poly toPoly() {
        if (!polyDirty && polyCache != null) {
            return polyCache;
        }
        Poly result = new Poly();
        if (exponent.equals(BigInteger.ZERO)) {
            NumFactor signNumFactor = new NumFactor(sign, BigInteger.ONE);        
            result = signNumFactor.toPoly();
        } else {
            // 处理表达式
            Poly exprPoly = new Poly();
            exprPoly = super.toPoly();
            // 处理指数
            result = result.addPoly(result, exprPoly);
            for (BigInteger i = BigInteger.ONE; i.compareTo(exponent) < 0;
                i = i.add(BigInteger.ONE)) {
                result = result.multiplyPoly(result, exprPoly);
            }
        }
        polyCache = result;
        polyDirty = false;
        return result;
    }

    BigInteger getExponent() {
        return exponent;
    }

    public void setExponent(BigInteger exponent) {
        this.exponent = exponent;
        this.polyDirty = true;
    }

    // 对 f(x)= sign * (expr(x))^exponent 求导
    //f'(x) = sign * exponent * (expr(x))^(exponent-1) * expr'(x)

    @Override
    public Poly toDerivative() {
        Poly result = new Poly();
        if (exponent.equals(BigInteger.ZERO)) { // f(x)=sign
            Mono mono = new Mono(BigInteger.ZERO, BigInteger.ZERO);
            result.addMono(mono);
            return result;
        }
        BigInteger coe = sign.multiply(exponent); // sign * exponent
        Mono mono = new Mono(coe, BigInteger.ZERO);
        result.addMono(mono);
        Poly exprPoly = new Poly();
        exprPoly = super.toPoly();
        Poly exprDerivative = new Poly();
        exprDerivative = super.toDerivative(); // expr'(x)
        result = result.multiplyPoly(result, exprDerivative); // sign * exponent * expr'(x)
        Poly powerPoly = new Poly(); // (expr(x))^(exponent-1)
        if (exponent.equals(BigInteger.ONE)) {
            powerPoly.addMono(new Mono(BigInteger.ONE, BigInteger.ZERO));
        } else {
            powerPoly = exprPoly;
            for (BigInteger i = BigInteger.ONE; i.compareTo(exponent.subtract(BigInteger.ONE)) < 0;
                i = i.add(BigInteger.ONE)) {
                powerPoly = powerPoly.multiplyPoly(powerPoly, exprPoly);
            }
        }
        result = result.multiplyPoly(result, powerPoly); // sign*exp*(expr(x))^(exp-1)*expr'(x)
        return result;
    } 
}
