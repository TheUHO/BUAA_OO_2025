import java.util.HashMap;
import java.util.Map;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Objects;

public class Poly {
    private HashMap<Mono, BigInteger> monos;

    public Poly() {
        this.monos = new HashMap<>();
    }
    
    public HashMap<Mono, BigInteger> getMonos() {
        return monos;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof Poly) {
            Poly other = (Poly) obj;
            return monos.equals(other.monos);
        } else {
            return false;
        }
    }

    @Override
    public int hashCode() {
        return Objects.hash(monos);
    }

    public void addMono(Mono mono) {
        if (!mono.getVariable().equals("x")) {
            throw new IllegalArgumentException("Error: invalid variable " + mono.getVariable());
        }
        // 新建一个Mono对象，作为key，避免直接使用mono对象
        Mono key = mono.normalized();
        BigInteger coe = monos.getOrDefault(key, BigInteger.ZERO);
        BigInteger sum = coe.add(mono.getCoefficient());
        if (sum.equals(BigInteger.ZERO) && monos.size() > 1) {
            monos.remove(key);
        } else {
            monos.put(key, sum);
        }
    }

    public Poly addPoly(Poly poly1, Poly poly2) {
        Poly result = new Poly();
        for (Map.Entry<Mono, BigInteger> entry : poly1.monos.entrySet()) {
            Mono key = entry.getKey();
            key.setCoefficient(entry.getValue());
            result.addMono(key);
        }
        // 遍历poly2，将其加入result
        for (Map.Entry<Mono, BigInteger> entry : poly2.monos.entrySet()) {
            Mono key = entry.getKey();
            BigInteger coe = entry.getValue();
            key.setCoefficient(coe);
            result.addMono(key);
        }
        return result;
    }

    public Poly multiplyPoly(Poly poly1, Poly poly2) {
        Poly result = new Poly();
        if (poly1.monos.isEmpty()) {
            return poly2;
        } else if (poly2.monos.isEmpty()) {
            return poly1;
        }
        for (Map.Entry<Mono, BigInteger> entry1 : poly1.monos.entrySet()) {
            for (Map.Entry<Mono, BigInteger> entry2 : poly2.monos.entrySet()) {
                Mono mono1 = entry1.getKey();
                Mono mono2 = entry2.getKey();
                BigInteger coe = entry1.getValue().multiply(entry2.getValue());
                Mono mono = mono1.multiply(mono2).normalized();
                mono.setCoefficient(coe);
                result.addMono(mono);
            }
        }
        return result;
    }

    public Poly negPoly(Poly poly) {
        Poly result = new Poly();
        for (Map.Entry<Mono, BigInteger> entry : poly.monos.entrySet()) {
            Mono key = entry.getKey();
            BigInteger coe = entry.getValue().negate();
            Mono mono = new Mono(coe, key.getExponent());
            mono.getSinMap().putAll(key.getSinMap());
            mono.getCosMap().putAll(key.getCosMap());
            result.addMono(mono);
        }
        return result;
    }

    public String toString() {
        if (monos.isEmpty()) {
            return "0";
        }
        ArrayList<String> positiveTerms = new ArrayList<>();
        ArrayList<String> negativeTerms = new ArrayList<>();
        // 遍历每一项，构造对应的单项式字符串，并分别归类
        for (Map.Entry<Mono, BigInteger> entry : monos.entrySet()) {
            Mono key = entry.getKey();
            BigInteger coeff = entry.getValue();
            // 生成实际的单项式，注意将三角函数因子也复制到新的 Mono 中
            Mono m = new Mono(coeff, key.getExponent());
            m.getSinMap().putAll(key.getSinMap());
            m.getCosMap().putAll(key.getCosMap());
            String s = m.toString();
            if (s.equals("0")) {
                continue;
            }
            // 按照首字符判断正负
            if (s.charAt(0) == '-') {
                negativeTerms.add(s);
            } else {
                positiveTerms.add(s);
            }
        }
        StringBuilder sb = new StringBuilder();
        boolean first = true;
        // 先输出所有正项，正项之间添加"+"号
        for (String s : positiveTerms) {
            if (first) {
                sb.append(s);
                first = false;
            } else {
                sb.append("+").append(s);
            }
        }
        // 然后输出所有负项，负项自带"-"，直接拼接
        for (String s : negativeTerms) {
            sb.append(s);
        }
        
        if (sb.length() == 0) {
            return "0";
        }
        return sb.toString();
    }

    public boolean negateTriFactor() {
        Mono maxMono = null;
        for (Map.Entry<Mono, BigInteger> entry : monos.entrySet()) {
            Mono key = entry.getKey();
            BigInteger coeff = entry.getValue();
            if (maxMono == null || (key.getExponent().compareTo(maxMono.getExponent()) > 0 
                && (!coeff.equals(BigInteger.ZERO)))) {
                maxMono = key;
            } else if (key.getExponent().equals(maxMono.getExponent()) 
                && (!coeff.equals(BigInteger.ZERO))) {
                if (key.getSinMap().size() + key.getCosMap().size() 
                    > maxMono.getSinMap().size() + maxMono.getCosMap().size()) {
                    maxMono = key;
                }
            }
        }
        if (maxMono != null && monos.get(maxMono).compareTo(BigInteger.ZERO) < 0) {
            return true;
        } else {
            return false;
        }
    }
}
