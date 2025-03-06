import java.util.HashMap;
import java.math.BigInteger;
import java.util.ArrayList;

public class Poly {
    private HashMap<BigInteger, BigInteger> monosX = new HashMap<BigInteger, BigInteger>();

    public void addMono(Mono mono) {
        BigInteger exp = mono.getExponent();
        BigInteger coe = mono.getCoefficient();
        if (mono.getVariable().equals("x")) { //不能使用==比较字符串
            if (exp.compareTo(BigInteger.ZERO) < 0) {
                System.out.println("Error: invalid exponent " + exp);
            } else if (monosX.containsKey(exp)) {
                coe = coe.add(monosX.get(exp));
                if (coe.equals(BigInteger.ZERO)) {
                    monosX.remove(exp);
                } else {
                    monosX.put(exp, coe);
                }
            } else {
                monosX.put(exp, coe);
            }
        } else {
            System.out.println("Error: invalid variable " + mono.getVariable());
        }
    }

    public Poly addPoly(Poly poly1, Poly poly2) {
        Poly result = new Poly();
        result.monosX.putAll(poly1.monosX);
        for (BigInteger exp : poly2.monosX.keySet()) {
            if (result.monosX.containsKey(exp)) {
                BigInteger coe = result.monosX.get(exp).add(poly2.monosX.get(exp));
                if (coe.equals(BigInteger.ZERO) && result.monosX.size() > 1) {
                    // 如果大于两个元素，则删除
                    result.monosX.remove(exp);
                } else {
                    result.monosX.put(exp, coe);
                }
            } else {
                result.monosX.put(exp, poly2.monosX.get(exp));
            }
        }
        return result;
    }

    public Poly multiplyPoly(Poly poly1, Poly poly2) {
        Poly result = new Poly();
        if (poly1.monosX.isEmpty()) {
            return poly2;
        } else if (poly2.monosX.isEmpty()) {
            return poly1;
        } else {
            for (BigInteger exp1 : poly1.monosX.keySet()) {
                for (BigInteger exp2 : poly2.monosX.keySet()) {
                    BigInteger exp = exp1.add(exp2);
                    BigInteger coe = poly1.monosX.get(exp1).multiply(poly2.monosX.get(exp2));
                    Mono mono = new Mono(coe, exp);
                    result.addMono(mono);
                }
            }
            return result;
        }
        
    }

    public Poly negPoly(Poly poly) {
        Poly result = new Poly();
        for (BigInteger exp : poly.monosX.keySet()) {
            BigInteger coe = poly.monosX.get(exp).negate();
            Mono mono = new Mono(coe, exp);
            result.addMono(mono);
        }
        return result;
    }

    public String toString() {
        ArrayList<String> positiveTerms = new ArrayList<>();
        ArrayList<String> negativeTerms = new ArrayList<>();
        StringBuilder sb = new StringBuilder();
        for (BigInteger exp : monosX.keySet()) {
            Mono mono = new Mono(monosX.get(exp), exp);
            String monoStr = mono.toString();
            if (monoStr.equals("0")) {
                continue;
            } else if (monoStr.charAt(0) == '-') {
                negativeTerms.add(monoStr);
            } else {
                positiveTerms.add(monoStr);
            }
        }
        // 处理正项
        if (!positiveTerms.isEmpty()) {
            sb.append(positiveTerms.get(0));
            for (int i = 1; i < positiveTerms.size(); i++) {
                sb.append("+").append(positiveTerms.get(i));
            }
        }

        // 处理负项，直接添加，因为它们以'-'开头
        for (String term : negativeTerms) {
            sb.append(term);
        }
        //关于多个单项式，第一个为0的情况
        if (sb.length() == 0) {
            return "0";
        }

        return sb.toString();
    }
}
