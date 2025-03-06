import java.math.BigInteger;

public class SinFactor implements Factor {
    private BigInteger sign;
    private Factor factor;
    private BigInteger exponent;

    public SinFactor(BigInteger sign) {
        this.sign = sign;
    }

    public BigInteger getSign() {
        return sign;
    }

    public BigInteger getExponent() {
        return exponent;
    }

    public void setExponent(BigInteger exponent) {
        this.exponent = exponent;
    }

    public Factor getFactor() {
        return factor;
    }

    public void setFactor(Factor factor) {
        this.factor = factor;
    }

    // 重写toPoly()方法
    @Override public Poly toPoly() {
        // TODO
        return null;
    }
    
}
