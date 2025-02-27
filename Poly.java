import java.util.HashMap;
import java.math.BigInteger;

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
                if (coe.equals(BigInteger.ZERO)) {
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
        StringBuilder sb = new StringBuilder();
        for (BigInteger exp : monosX.keySet()) {
            Mono mono = new Mono(monosX.get(exp), exp);
            sb.append(mono.toString());
            sb.append("+");
        }
        sb.deleteCharAt(sb.length() - 1); // delete the last "+"

        //关于多个单项式，第一个为0的情况
        //关于多个单项式，第一个为负的情况
        //关于多个单项式，中间出现+-单项式的情况

        return sb.toString();
    }
}
