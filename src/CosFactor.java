import java.math.BigInteger;

public class CosFactor implements Factor {
    private BigInteger sign;
    private Factor factor;
    private BigInteger exponent;
    private Poly polyCache;
    private boolean polyDirty = true;

    public CosFactor(BigInteger sign) {
        this.sign = sign;
    }

    public BigInteger getSign() {
        return sign;
    }

    public BigInteger getExponent() {
        return exponent;
    }

    public void setExponent(BigInteger exponent) {
        this.exponent = exponent;
        this.polyDirty = true;
    }

    public Factor getFactor() {
        return factor;
    }

    public void setFactor(Factor factor) {
        this.factor = factor;
        this.polyDirty = true;
    }

    // 重写toPoly()方法
    @Override public Poly toPoly() {
        if (!polyDirty && polyCache != null) {
            return polyCache;
        }
        Mono mono = new Mono(sign, BigInteger.ZERO); // 创建一个单项式
        Poly result = new Poly(); // 创建一个多项式
        if (exponent.equals(BigInteger.ZERO)) { 
            // 如果指数为0
        } else {
            mono.addCosFactor(factor.toPoly(), exponent);
        }
        result.addMono(mono); // 将单项式加入多项式
        polyCache = result; // 更新缓存
        polyDirty = false;
        return result; // 返回多项式
    }

    // f(x)=sign * cos(factor(x))^exponent
    // f'(x) = -exponent * sign * cos(factor(x))^(exponent-1) * factor'(x) * sin(factor(x))
    @Override
    public Poly toDerivative() {
        Poly result = new Poly();
        if (exponent.equals(BigInteger.ZERO)) {
            Mono mono = new Mono(BigInteger.ZERO, BigInteger.ZERO);
            result.addMono(mono);
            return result;
        }
        BigInteger coe = sign.multiply(exponent).negate(); // 求导后的系数
        Mono mono = new Mono(coe, BigInteger.ZERO);
        mono.addSinFactor(factor.toPoly(), BigInteger.ONE);
        if (exponent.compareTo(BigInteger.ONE) > 0) { // 如果指数大于1，继续添加余弦因子
            mono.addCosFactor(factor.toPoly(), exponent.subtract(BigInteger.ONE)); 
        }
        result.addMono(mono);
        Poly innerPoly = factor.toDerivative(); // 对内部表达式求导
        result = result.multiplyPoly(result, innerPoly); // 乘上内部表达式的导数
        return result;
    }
}
