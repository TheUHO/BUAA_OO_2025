import java.math.BigInteger;
import java.util.HashMap;
import java.util.Objects;

public class Mono {
    private BigInteger coefficient;
    private String variable; 
    private BigInteger exponent;
    private HashMap<Poly, BigInteger> sinMap; // 因子和指数的映射
    private HashMap<Poly, BigInteger> cosMap; // 因子和指数的映射

    public Mono(BigInteger coefficient, BigInteger exponent) {
        this.coefficient = coefficient;
        this.variable = "x"; // only "x" for now  
        this.exponent = exponent;
        this.sinMap = new HashMap<>();
        this.cosMap = new HashMap<>();
    }

    public BigInteger getCoefficient() {
        return coefficient;
    }

    public void setCoefficient(BigInteger coefficient) {
        this.coefficient = coefficient;
    }

    public String getVariable() {
        return variable;
    }

    public BigInteger getExponent() {
        return exponent;
    }

    public HashMap<Poly, BigInteger> getSinMap() {
        return sinMap;
    }

    public HashMap<Poly, BigInteger> getCosMap() {
        return cosMap;
    }

    public void addSinFactor(Poly factor, BigInteger exponent) {
        sinMap.put(factor, sinMap.getOrDefault(factor, BigInteger.ZERO).add(exponent));
    }

    public void addCosFactor(Poly factor, BigInteger exponent) {
        cosMap.put(factor, cosMap.getOrDefault(factor, BigInteger.ZERO).add(exponent));
    }

    public Mono multiply(Mono other) {
        BigInteger newCoefficient = this.coefficient.multiply(other.coefficient);
        BigInteger newExponent = this.exponent.add(other.exponent);
        Mono result = new Mono(newCoefficient, newExponent);
        result.sinMap.putAll(this.sinMap);
        for (Poly factor : other.sinMap.keySet()) {
            result.addSinFactor(factor, other.sinMap.get(factor));
        }
        result.cosMap.putAll(this.cosMap);
        for (Poly factor : other.cosMap.keySet()) {
            result.addCosFactor(factor, other.cosMap.get(factor));
        }
        return result;
    }

    // 用于比较两个单项式的指数是否相等
    public Mono normalized() {
        Mono result = new Mono(BigInteger.ONE, this.exponent);
        result.sinMap.putAll(this.sinMap);
        result.cosMap.putAll(this.cosMap);
        return result;
    }

    // 重写equals和hashCode方法
    @Override
    public boolean equals(Object obj) {
        if (obj instanceof Mono) {
            Mono other = (Mono) obj;
            return exponent.equals(other.exponent)
                && sinMap.equals(other.sinMap) && cosMap.equals(other.cosMap);
        } else {
            return false;
        }
    }

    @Override
    public int hashCode() {
        return Objects.hash(exponent, sinMap, cosMap);
    }

    public String toString() {
        if (coefficient.equals(BigInteger.ZERO)) { // 如果系数为0，则整个项为0
            return "0";
        }
        boolean isPureConstant = exponent.equals(BigInteger.ZERO) 
            && sinMap.isEmpty() && cosMap.isEmpty(); // 判断是否为纯常数项（无 x、无 sin、无 cos）
        StringBuilder sb = new StringBuilder();
        if (isPureConstant) { // 如果是纯常数项，直接返回系数字符串
            sb.append(coefficient);
            return sb.toString();
        } else {
            // 非纯常数项：判断系数是否为 1 或 -1
            if (coefficient.equals(BigInteger.ONE)) {
                // 当系数为1时，不输出 "1*"，对于纯常数项已处理
            } else if (coefficient.equals(BigInteger.ONE.negate())) {
                // 当系数为-1时，只输出 "-" 号
                sb.append("-");
            } else {
                // 其他情况输出系数
                sb.append(coefficient);
            }
        }     
        if (!exponent.equals(BigInteger.ZERO)) { // 处理 x 部分
            if (sb.length() > 0) { // 系数不为0时，输出 "*"
                sb.append("*");
            }
            if (exponent.equals(BigInteger.ONE)) {
                sb.append("x");
            } else {
                sb.append("x^").append(exponent);
            }
        }
        for (Poly poly : sinMap.keySet()) { // 处理 sin 部分
            BigInteger exp = sinMap.get(poly);
            if (sb.length() > 0) { // 系数不为0时，输出 "*"
                sb.append("*");
            }
            sb.append("sin(").append(poly.toString()).append(")");
            if (!exp.equals(BigInteger.ONE)) {
                sb.append("^").append(exp);
            }
        }
        for (Poly poly : cosMap.keySet()) { // 处理 cos 部分
            BigInteger exp = cosMap.get(poly);
            if (sb.length() > 0) { // 系数不为0时，输出 "*"
                sb.append("*");
            }
            sb.append("cos(").append(poly.toString()).append(")");
            if (!exp.equals(BigInteger.ONE)) {
                sb.append("^").append(exp);
            }
        }
        return sb.toString();
    }

}
