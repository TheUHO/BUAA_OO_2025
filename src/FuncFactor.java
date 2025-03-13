import java.math.BigInteger;
import java.util.ArrayList;

public class FuncFactor implements Factor {
    
    private BigInteger sign;
    private String funcString; // 将实参代入函数所得字符串
    private Expr funcExpr; // 函数表达式
    private Poly polyCache;
    private boolean polyDirty = true;

    public FuncFactor(BigInteger sign, String name, ArrayList<String> parameters) {
        this.sign = sign;
        this.funcString = FuncManager.callFunc(name, parameters);
        this.funcExpr = toExpr();
    }

    // 将函数字符串转换为表达式
    private Expr toExpr() {
        if (sign.equals(BigInteger.ONE.negate())) {
            funcString = "-(" + funcString + ")";
        }
        Lexer lexer = new Lexer(funcString);
        Parser parser = new Parser(lexer);
        return parser.parseExpr();
    }

    @Override
    public Poly toPoly() {
        if (!polyDirty && polyCache != null) {
            return polyCache;
        }
        Poly result = funcExpr.toPoly();
        polyCache = result;
        polyDirty = false;
        return result;
    }

    @Override
    public Poly toDerivative() {
        Poly result = funcExpr.toDerivative();
        if (sign.equals(BigInteger.ONE.negate())) {
            result = result.negPoly(result);
        }
        return result;
    }
}
