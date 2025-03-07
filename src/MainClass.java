import java.math.BigInteger;
import java.util.Scanner;

public class MainClass {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        BigInteger funcNum = new BigInteger(scanner.nextLine());
        for (BigInteger i = BigInteger.ZERO; i.compareTo(funcNum) < 0; i = i.add(BigInteger.ONE)) {
            for (BigInteger j = BigInteger.ZERO; j.compareTo(BigInteger.valueOf(3)) < 0; 
                j = j.add(BigInteger.ONE)) {
                String input = scanner.nextLine();
                input = input.replaceAll("\\s", "");
                FuncManager.FuncDefinition(input);
            }
        }
        String input = scanner.nextLine();
        input = input.replaceAll("\\s", "");
        Lexer lexer = new Lexer(input);
        Parser parser = new Parser(lexer);
        Expr expr = parser.parseExpr();
        Poly exprPoly = expr.toPoly();      
        System.out.println(exprPoly.toString());
        scanner.close();    
    }
}