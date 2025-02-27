import java.util.ArrayList;

public class Term {

    private final ArrayList<Factor> factors = new ArrayList<>();

    public void addFactor(Factor factor) {
        factors.add(factor);
    }

    public Poly toPoly() {
        Poly result = new Poly();
        for (Factor factor : factors) {
            Poly poly = factor.toPoly();
            result = result.multiplyPoly(result, poly);
        }
        return result;
    }
}
