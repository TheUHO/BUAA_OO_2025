import java.math.BigInteger;
import java.util.HashMap;
import java.util.Map;
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
            if (sb.length() > 0 && sb.charAt(sb.length() - 1) != '-') { // 系数不为0时，输出 "*"
                sb.append("*");
            }
            if (exponent.equals(BigInteger.ONE)) {
                sb.append("x");
            } else {
                sb.append("x^").append(exponent);
            }
        }
        for (Map.Entry<Poly, BigInteger> entry : sinMap.entrySet()) {
            if (sb.length() > 0 && sb.charAt(sb.length() - 1) != '-') {
                sb.append("*");
            }
            String trigExpr = buildTrigExpr("sin", entry.getKey());
            sb.append(trigExpr);
            BigInteger exp = entry.getValue();
            if (!exp.equals(BigInteger.ONE)) {
                sb.append("^").append(exp);
            }
        }
        for (Map.Entry<Poly, BigInteger> entry : cosMap.entrySet()) {
            if (sb.length() > 0 && sb.charAt(sb.length() - 1) != '-') {
                sb.append("*");
            }
            String trigExpr = buildTrigExpr("cos", entry.getKey());
            sb.append(trigExpr);
            BigInteger exp = entry.getValue();
            if (!exp.equals(BigInteger.ONE)) {
                sb.append("^").append(exp);
            }
        }
        if (sb.length() == 0) {
            sb.append(coefficient);
        }
        return sb.toString();
    }

    private String buildTrigExpr(String trigName, Poly poly) {
        // 如果多项式只有一个单项式，则做细分判断
        if (poly.getMonos().size() == 1) {
            Mono onlyMono = poly.getMonos().keySet().iterator().next();
            BigInteger coe = onlyMono.getCoefficient();
            BigInteger exp = onlyMono.getExponent();
            // 三角函数因子数量（如 > 1 则视为较复杂表达式）
            int trigCount = onlyMono.getSinMap().size() + onlyMono.getCosMap().size();
            
            // 1) 系数为 0，sin(0) 或 cos(0)
            if (coe.equals(BigInteger.ZERO)) {
                return trigName + "(" + onlyMono.toString() + ")";
            }
            // 2) 系数为 1
            else if (coe.equals(BigInteger.ONE)) {
                // 2a) 指数为 0
                if (exp.equals(BigInteger.ZERO)) {
                    // 如果三角因子总数 > 1，需要额外括号
                    if (trigCount > 1) {
                        return trigName + "((" + poly.toString() + "))";
                    } else {
                        return trigName + "(" + poly.toString() + ")";
                    }
                }
                // 2b) 指数不为 0
                else {
                    // 如果有三角因子，((...))
                    if (trigCount > 0) {
                        return trigName + "((" + poly.toString() + "))";
                    } else {
                        return trigName + "(" + poly.toString() + ")";
                    }
                }
            }
            // 3) 系数不为 0 和 1
            else {
                // 3a) 指数为 0
                if (exp.equals(BigInteger.ZERO)) {
                    // 无论三角因子多少，代码都一致
                    return trigName + "(" + poly.toString() + ")";
                }
                // 3b) 指数不为 0，需要额外括号
                else {
                    return trigName + "((" + poly.toString() + "))";
                }
            }
        } 
        // 如果多项式含有多个 mono，直接加双括号
        else {
            return trigName + "((" + poly.toString() + "))";
        }
    }
}
