
import java.math.BigInteger;

public class NumFactor implements Factor {
    private final BigInteger sign;
    private final BigInteger value;
    private Poly polyCache;
    private boolean polyDirty = true;

    public NumFactor(BigInteger sign, BigInteger value) {
        this.sign = sign;
        this.value = value;
    }

    public BigInteger getSign() {
        return sign;
    }

    public BigInteger getValue() {
        return value;
    }

    @Override
    public Poly toPoly() {
        if (!polyDirty && polyCache != null) {
            return polyCache;
        }
        BigInteger coe = sign.multiply(value);
        Mono mono = new Mono(coe, BigInteger.ZERO);
        Poly result = new Poly();
        result.addMono(mono);
        polyCache = result;
        polyDirty = false;
        return result;
    }

    @Override
    public Poly toDerivative() {
        Mono mono = new Mono(BigInteger.ZERO, BigInteger.ZERO);
        Poly result = new Poly();
        result.addMono(mono);
        return result;
    }
}
