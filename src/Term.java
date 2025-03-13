import java.math.BigInteger;
import java.util.ArrayList;

public class Term {

    private BigInteger sign;
    private final ArrayList<Factor> factors = new ArrayList<>();
    private Poly polyCache;
    private boolean polyDirty = true;

    public Term(BigInteger sign) {
        this.sign = sign;
    }

    public void addFactor(Factor factor) {
        factors.add(factor);
        polyDirty = true;
    }

    public Poly toPoly() {
        if (!polyDirty && polyCache != null) {
            return polyCache;
        }
        Poly result = new Poly();
        for (Factor factor : factors) {
            Poly poly = factor.toPoly();
            result = result.multiplyPoly(result, poly);
        }
        // 处理term前的符号
        if (sign.equals(BigInteger.ONE)) {
            // 不用处理
        } else {
            result = result.negPoly(result);
        }
        polyCache = result;
        polyDirty = false;
        return result;
    }

    public Poly toDerivative() {
        Poly result = new Poly();
        for (int i = 0; i < factors.size(); i++) {
            Poly leftPoly = new Poly();
            leftPoly.addMono(new Mono(BigInteger.ONE, BigInteger.ZERO)); // 恒等多项式1
            for (int j = 0; j < i; j++) {
                leftPoly = leftPoly.multiplyPoly(leftPoly, factors.get(j).toPoly());
            }
            Poly rightPoly = new Poly();
            rightPoly.addMono(new Mono(BigInteger.ONE, BigInteger.ZERO)); // 恒等多项式1
            for (int j = i + 1; j < factors.size(); j++) {
                rightPoly = rightPoly.multiplyPoly(rightPoly, factors.get(j).toPoly());
            }
            Poly derivedPoly = factors.get(i).toDerivative();
            Poly poly = leftPoly.multiplyPoly(leftPoly, derivedPoly);
            poly = poly.multiplyPoly(poly, rightPoly);
            result = result.addPoly(result, poly);
        }
        if (sign.equals(BigInteger.ONE)) {
            // 不用处理
        } else {
            result = result.negPoly(result);
        }
        return result;
    }
}
