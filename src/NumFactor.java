
import java.math.BigInteger;

public class NumFactor implements Factor {
    private final BigInteger sign;
    private final BigInteger value;

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
        BigInteger coe = sign.multiply(value);
        Mono mono = new Mono(coe, BigInteger.ZERO);
        Poly result = new Poly();
        result.addMono(mono);
        return result;
    }
}
