
import java.util.ArrayList;

public class Expr {
    private final ArrayList<Term> terms = new ArrayList<>();
    private Poly polyCache;
    private boolean polyDirty = true;

    public void addTerm(Term term) {
        terms.add(term);
        polyDirty = true;
    }

    public Poly toPoly() {
        if (!polyDirty && polyCache != null) {
            return polyCache;
        }
        Poly result = new Poly();
        for (Term term : terms) {
            Poly poly = term.toPoly();
            result = result.addPoly(result, poly);
        }
        polyCache = result;
        polyDirty = false;
        return result;
    }
    
    Poly toDerivative() {
        Poly result = new Poly();
        for (Term term : terms) {
            Poly poly = term.toDerivative();
            result = result.addPoly(result, poly);
        }
        return result;
    }
}
