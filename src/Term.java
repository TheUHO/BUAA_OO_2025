import java.math.BigInteger;
import java.util.ArrayList;

public class Term {

    private BigInteger sign;
    private final ArrayList<Factor> factors = new ArrayList<>();

    public Term(BigInteger sign) {
        this.sign = sign;
    }

    public void addFactor(Factor factor) {
        factors.add(factor);
    }

    public Poly toPoly() {
        Poly result = new Poly();
        for (Factor factor : factors) {
            Poly poly = factor.toPoly();
            result = result.multiplyPoly(result, poly);
        }
        // 处理term前的符号
        if (sign.equals(BigInteger.ONE)) {
            return result;
        } else {
            return result.negPoly(result);
        }
    }
}
