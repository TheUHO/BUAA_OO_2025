import java.math.BigInteger;

public class PowFactor implements Factor {
    private BigInteger base;
    private BigInteger exponent;
    private final BigInteger sign;
    private Poly polyCache;
    private boolean polyDirty = true;

    public PowFactor(BigInteger sign) {
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
        BigInteger coe = sign;
        Mono mono = new Mono(coe, exponent);
        Poly result = new Poly();
        result.addMono(mono);
        polyCache = result;
        polyDirty = false;
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
        polyDirty = true;
    }

    public void setExponent(BigInteger exponent) {
        this.exponent = exponent;
        polyDirty = true;
    }
    
    @Override
    public Poly toDerivative() {
        if (exponent.equals(BigInteger.ZERO)) {
            Mono mono = new Mono(BigInteger.ZERO, BigInteger.ZERO);
            Poly result = new Poly();
            result.addMono(mono);
            return result;
        } else {
            BigInteger coe = sign.multiply(exponent);
            BigInteger exp = exponent.subtract(BigInteger.ONE);
            Mono mono = new Mono(coe, exp);
            Poly result = new Poly();
            result.addMono(mono);
            return result;
        }
    }
}
