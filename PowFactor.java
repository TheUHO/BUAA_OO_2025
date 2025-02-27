import java.math.BigInteger;

public class PowFactor implements Factor {
    private BigInteger base;
    private BigInteger exponent;
    private final BigInteger sign;

    public PowFactor(BigInteger sign) {
        this.sign = sign;
    }

    @Override
    public BigInteger getSign() {
        return sign;
    }

    @Override
    public Poly toPoly() {
        BigInteger coe = sign;
        Mono mono = new Mono(coe, exponent);
        Poly result = new Poly();
        result.addMono(mono);
        return result;
    }

    public BigInteger getBase() {
        return base;
    }

    public BigInteger getExponent() {
        return exponent;
    }

    public void setBase(BigInteger base) {
        this.base = base;
    }

    public void setExponent(BigInteger exponent) {
        this.exponent = exponent;
    }
}
