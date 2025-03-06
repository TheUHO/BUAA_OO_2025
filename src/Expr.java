
import java.util.ArrayList;

public class Expr {
    private final ArrayList<Term> terms = new ArrayList<>();

    public void addTerm(Term term) {
        terms.add(term);
    }

    public Poly toPoly() {
        Poly result = new Poly();
        for (Term term : terms) {
            Poly poly = term.toPoly();
            result = result.addPoly(result, poly);
        }
        return result;
    }

}
