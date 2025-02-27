import java.math.BigInteger;

public class Mono {
    private BigInteger coefficient;
    private String variable; 
    private BigInteger exponent;

    public Mono(BigInteger coefficient, BigInteger exponent) {
        this.coefficient = coefficient;
        this.variable = "x"; // only "x" for now  
        this.exponent = exponent;
    }

    public BigInteger getCoefficient() {
        return coefficient;
    }

    public String getVariable() {
        return variable;
    }

    public BigInteger getExponent() {
        return exponent;
    }

    public String toString() {
        if (coefficient.equals(BigInteger.ZERO)) {
            return "0";
        } else if (exponent.equals(BigInteger.ZERO)) {
            return coefficient.toString();
        } else  if (exponent.equals(BigInteger.ONE)) {
            if (coefficient.equals(BigInteger.ONE)) {
                return variable;
            } else if (coefficient.equals(BigInteger.ONE.negate())) {
                return "-" + variable;
            } else {
                return coefficient + "*" + variable;
            }
        } else if (coefficient.equals(BigInteger.ONE)) {
            return variable + "^" + exponent;
        } else if (coefficient.equals(BigInteger.ONE.negate())) {
            return  "-" + variable + "^" + exponent;
        } else {
            return coefficient + "*" + variable + "^" + exponent;
        }
    }
}
