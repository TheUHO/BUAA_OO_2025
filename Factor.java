import java.math.BigInteger;

public interface Factor {
    BigInteger getSign();
    
    Poly toPoly();
}
