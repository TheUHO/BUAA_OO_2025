import java.math.BigInteger;

public class SinFactor implements Factor {
    private BigInteger sign;
    private Factor factor;
    private BigInteger exponent;

    public SinFactor(BigInteger sign) {
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
    }

    public Factor getFactor() {
        return factor;
    }

    public void setFactor(Factor factor) {
        this.factor = factor;
    }

    // 重写toPoly()方法
    @Override public Poly toPoly() {
        Mono mono = new Mono(sign, BigInteger.ZERO); // 创建一个单项式
        mono.addSinFactor(factor.toPoly(), exponent);
        Poly result = new Poly(); // 创建一个多项式
        result.addMono(mono); // 将单项式加入多项式
        return result; // 返回多项式
    }
    
}
