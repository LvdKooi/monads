package nl.kooi.monads.util;

import lombok.experimental.UtilityClass;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.function.UnaryOperator;

import static java.math.RoundingMode.HALF_UP;

@UtilityClass
public class BigDecimalUtils {
    public static Predicate<BigDecimal> isAtLeast(long amount) {
        return bd -> bd.compareTo(BigDecimal.valueOf(amount)) >= 0;
    }

    public static UnaryOperator<BigDecimal> maximizeAt(long amount) {
        return bd -> bd.min(BigDecimal.valueOf(amount));
    }

    public static UnaryOperator<BigDecimal> roundUp() {
        return bd -> bd.setScale(0, HALF_UP);
    }

    public static UnaryOperator<BigDecimal> divideBy(long amount) {
        return bd -> bd.divide(BigDecimal.valueOf(amount), 0, HALF_UP);
    }

    public static UnaryOperator<BigDecimal> multiplyBy(BigDecimal amount) {
        return bd -> Optional.ofNullable(amount).orElse(BigDecimal.ONE).multiply(bd);
    }
}
