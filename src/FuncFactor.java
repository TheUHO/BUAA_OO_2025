import java.math.BigInteger;
import java.util.ArrayList;

public class FuncFactor implements Factor {
    
    private BigInteger sign;
    private String funcString; // 将实参代入函数所得字符串
    private Expr funcExpr; // 函数表达式

    public FuncFactor(BigInteger sign, String name, BigInteger seq, ArrayList<String> parameters) {
        this.sign = sign;
        this.funcString = FuncManager.callFunc(name, seq, parameters);
        this.funcExpr = toExpr();
    }

    // 将函数字符串转换为表达式
    private Expr toExpr() {
        Lexer lexer = new Lexer(funcString);
        Parser parser = new Parser(lexer);
        return parser.parseExpr();
    }

    @Override
    public Poly toPoly() {
        return funcExpr.toPoly();
    }
}
