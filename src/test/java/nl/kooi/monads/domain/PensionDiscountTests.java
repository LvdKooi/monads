package nl.kooi.monads.domain;

import nl.kooi.monads.domain.product.PensionProduct;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;

public class PensionDiscountTests {

    private final DiscountService discountService = new DiscountService();

    @Test
    @DisplayName("When duration is more than 20 years, and monthly deposit is at least 300 then the discountpercentage is 3%")
    void pensionWithDiscountOf3Percent_withEndDate() {
        var pension = createPensionProduct(21, 300);

        assertThat(discountService.determineDiscount(List.of(pension))).isEqualTo(BigDecimal.valueOf(30));
    }

    @Test
    @DisplayName("When there is no endDate and monthly deposit is at least 300 then the discountpercentage is 3%")
    void pensionWithDiscountOf3Percent_withoutEndDate() {
        var pension = createPensionProduct(null, 300);

        assertThat(discountService.determineDiscount(List.of(pension))).isEqualTo(BigDecimal.valueOf(30));
    }

    @Test
    @DisplayName("When there is no endDate and monthly deposit is less than 300 then the discountpercentage is 2%")
    void pensionWithDiscountOf2Percent_monthlyDepositLessThan300() {
        var pension = createPensionProduct(null, 299);

        assertThat(discountService.determineDiscount(List.of(pension))).isEqualTo(BigDecimal.valueOf(20));
    }

    @Test
    @DisplayName("When the duration is less than 20 years and monthly deposit is at least 300 then the discountpercentage is 1%")
    void pensionWithDiscountOf1Percent_monthlyDepositAtLeast300EndDateLessThan20YearAfterStartDate() {
        var pension = createPensionProduct(19, 300);

        assertThat(discountService.determineDiscount(List.of(pension))).isEqualTo(BigDecimal.valueOf(10));
    }


    @Test
    @DisplayName("When the duration is less than 20 years and monthly deposit is less than 299 then the discountpercentage is 0%")
    void pensionWithDiscountOf0Percent_monthlyDepositLessThan300EndDateLessThan20YearAfterStartDate() {
        var pension = createPensionProduct(19, 299);

        assertThat(discountService.determineDiscount(List.of(pension))).isEqualTo(BigDecimal.ZERO);
    }

    private static PensionProduct createPensionProduct(Integer durationInYears, Integer monthlyDeposit) {
        return new PensionProduct("pension",
                LocalDate.now(),
                BigDecimal.valueOf(1000),
                BigDecimal.valueOf(monthlyDeposit),
                Optional.ofNullable(durationInYears)
                        .map(LocalDate.now()::plusYears)
                        .orElse(null),
                BigDecimal.valueOf(500000));
    }
}
