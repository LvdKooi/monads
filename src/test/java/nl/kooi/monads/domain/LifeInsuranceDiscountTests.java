package nl.kooi.monads.domain;

import nl.kooi.monads.domain.product.LifeInsuranceProduct;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;

public class LifeInsuranceDiscountTests {

    private final DiscountService discountService = new DiscountService();


    @Test
    @DisplayName("When customer was 21 year at startdate of product, and has an insured amount larger than 100K then the discountperentage is 3%.")
    void lifeInsuranceWithDiscountOf3Percent() {
        var lifeInsurance = createLifeInsurance(21, 500, 150000);

        // discount percentage (3%) * yearly commission (500) = 15
        assertThat(discountService.determineDiscount(List.of(lifeInsurance)))
                .isEqualTo(BigDecimal.valueOf(15));
    }

    @Test
    @DisplayName("When customer was 20 year at startdate of product, and has an insured amount larger than 100K then the discountperentage is 1%.")
    void lifeInsuranceWithDiscountOf1Percent() {
        var lifeInsurance = createLifeInsurance(20, 500, 150000);

        // discount percentage (1%) * yearly commission (500) = 5
        assertThat(discountService.determineDiscount(List.of(lifeInsurance)))
                .isEqualTo(BigDecimal.valueOf(5));
    }

    @Test
    @DisplayName("When customer was 20 year at startdate of product, has an insured amount larger than 100K and commission over 1000 then the discountperentage is 1% and commission is maximized at 1000.")
    void lifeInsuranceWithDiscountOf1Percent_andCommissionOver1000() {
        var lifeInsurance = createLifeInsurance(20, 1500, 150000);

        // discount percentage (1%) * yearly commission (1000) = 10
        assertThat(discountService.determineDiscount(List.of(lifeInsurance)))
                .isEqualTo(BigDecimal.valueOf(10));
    }

    @Test
    @DisplayName("When customer was 21 year at startdate of product, and has an insured amount smaller than 100K then the discountperentage is 0%.")
    void lifeInsuranceWithDiscountOf0Percent_insuredAmountSmallerThan100K() {
        var lifeInsurance = createLifeInsurance(21, 500, 99000);

        // discount percentage (0%) * yearly commission (500) = 0
        assertThat(discountService.determineDiscount(List.of(lifeInsurance)))
                .isEqualTo(BigDecimal.valueOf(0));
    }

    @Test
    @DisplayName("When customer was 21 year at startdate of product, and has no insured amount then the discountperentage is 0%.")
    void lifeInsuranceWithDiscountOf0Percent_noInsuredAmount() {
        var lifeInsurance = createLifeInsurance(21, 500, null);

        // discount percentage (0%) * yearly commission (500) = 0
        assertThat(discountService.determineDiscount(List.of(lifeInsurance)))
                .isEqualTo(BigDecimal.valueOf(0));
    }

    private static LifeInsuranceProduct createLifeInsurance(int ageCustomerAtStartDate, Integer yearlyCommission, Integer insuredAmount) {
        return new LifeInsuranceProduct("lifeInsurance",
                LocalDate.now(),
                Optional.ofNullable(yearlyCommission).map(BigDecimal::valueOf).orElse(null),
                Optional.ofNullable(insuredAmount).map(BigDecimal::valueOf).orElse(null),
                LocalDate.now().minusYears(ageCustomerAtStartDate)
        );
    }
}
