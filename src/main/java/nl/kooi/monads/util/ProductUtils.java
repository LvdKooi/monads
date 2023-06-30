package nl.kooi.monads.util;

import lombok.experimental.UtilityClass;
import nl.kooi.monads.domain.product.Product;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.Period;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Predicate;

@UtilityClass
public class ProductUtils {

    public static <T extends Product> Predicate<T> isAmountAtLeast(Function<T, BigDecimal> amountFunction,
                                                                   int atLeast) {
        return product -> Optional.ofNullable(product)
                .map(amountFunction)
                .filter(amount -> amount.compareTo(BigDecimal.valueOf(atLeast)) >= 0)
                .isPresent();
    }

    public static <T extends Product> Predicate<T> isEndDateMoreThanGivenYearsAfterStartDate(Function<T, LocalDate> firstDate, Function<T, LocalDate> secondDate, int moreThan) {
        return product -> Optional.ofNullable(product)
                .filter(hasDate(firstDate))
                .filter(hasDate(secondDate))
                .filter(pr -> Period.between(firstDate.apply(pr), secondDate.apply(pr)).getYears() > moreThan)
                .isPresent();
    }

    public static <T extends Product> Predicate<T> hasDate(Function<T, LocalDate> dateFunction) {
        return pension -> Optional.ofNullable(pension).map(dateFunction).isPresent();
    }

    public static <T extends Product> Optional<T> withProductAsType(Product product, Class<T> type) {
        return Optional.ofNullable(product)
                .filter(type::isInstance)
                .map(type::cast);
    }
}
