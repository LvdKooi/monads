package nl.kooi.monads.domain;

import nl.kooi.monads.domain.product.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

@SpringJUnitConfig(DiscountService.class)
class DiscountServiceTests {

    @Autowired
    private DiscountService discountService;


    @Test
    void allProductsAreEligibleForMaximumDiscount() {
        var productsList = List.of(createPensionProduct(21, 300),
                createMortgageProduct("ANNUITY", 360),
                createLifeInsurance(21, 500, 150000),
                createNonLifeInsurance());

        assertThat(discountService.determineDiscount(productsList)).isEqualTo(BigDecimal.valueOf(96).setScale(2, RoundingMode.HALF_UP));
    }

    @Nested
    @DisplayName("Pension Tests")
    class PensionDiscountTests {

        @Test
        @DisplayName("When duration is more than 20 years, and monthly deposit is at least 300 then the discount percentage is 3%")
        void pensionWithDiscountOf3Percent_withEndDate() {
            var pension = createPensionProduct(21, 300);

            assertThat(discountService.determineDiscount(List.of(pension))).isEqualTo(BigDecimal.valueOf(30));
        }

        @Test
        @DisplayName("When there is no endDate and monthly deposit is at least 300 then the discount percentage is 3%")
        void pensionWithDiscountOf3Percent_withoutEndDate() {
            var pension = createPensionProduct(null, 300);

            assertThat(discountService.determineDiscount(List.of(pension))).isEqualTo(BigDecimal.valueOf(30));
        }

        @Test
        @DisplayName("When there is no endDate and monthly deposit is less than 300 then the discount percentage is 2%")
        void pensionWithDiscountOf2Percent_monthlyDepositLessThan300() {
            var pension = createPensionProduct(null, 299);

            assertThat(discountService.determineDiscount(List.of(pension))).isEqualTo(BigDecimal.valueOf(20));
        }

        @Test
        @DisplayName("When the duration is less than 20 years and monthly deposit is at least 300 then the discount percentage is 1%")
        void pensionWithDiscountOf1Percent_monthlyDepositAtLeast300EndDateLessThan20YearAfterStartDate() {
            var pension = createPensionProduct(19, 300);

            assertThat(discountService.determineDiscount(List.of(pension))).isEqualTo(BigDecimal.valueOf(10));
        }

        @Test
        @DisplayName("When the duration is less than 20 years and monthly deposit is less than 299 then the discount percentage is 0%")
        void pensionWithDiscountOf0Percent_monthlyDepositLessThan300EndDateLessThan20YearAfterStartDate() {
            var pension = createPensionProduct(19, 299);

            assertThat(discountService.determineDiscount(List.of(pension))).isEqualTo(BigDecimal.ZERO);
        }

    }

    @Nested
    @DisplayName("Mortgage Tests")
    class MortgageDiscountTests {

        @Test
        @DisplayName("When productName is  ANNUITY and duration in months is 360, then product is eligible for discount")
        void mortgageWithDiscount() {

            var mortgage = createMortgageProduct("ANNUITY", 360);

            // // discount percentage (0.01 % 360 = 3.6%) * yearly commission (500) = 18
            assertThat(discountService.determineDiscount(List.of(mortgage))).isEqualTo(BigDecimal.valueOf(18).setScale(2, RoundingMode.HALF_UP));
        }

        @Test
        @DisplayName("When productName is not ANNUITY but duration in months is 360, then there is no discount")
        void mortgageWithDiscountOf0_notAnnuity() {

            var mortgage = createMortgageProduct("SAVINGS", 360);

            // // discount percentage (0%) * yearly commission (500) = 0
            assertThat(discountService.determineDiscount(List.of(mortgage))).isEqualTo(BigDecimal.ZERO);
        }

        @Test
        @DisplayName("When productName is not ANNUITY but duration in months is 360, then there is no discount")
        void mortgageWithDiscountOf0_annuityButDurationLessThan360Months() {

            var mortgage = createMortgageProduct("ANNUITY", 359);

            // // discount percentage (0%) * yearly commission (500) = 0
            assertThat(discountService.determineDiscount(List.of(mortgage))).isEqualTo(BigDecimal.ZERO);
        }
    }

    @Nested
    @DisplayName("Life Insurance tests")
    class LifeInsuranceDiscountTests {

        @Test
        @DisplayName("When customer was 21 year at startdate of product, and has an insured amount larger than 100K then the discount percentage is 3%.")
        void lifeInsuranceWithDiscountOf3Percent() {
            var lifeInsurance = createLifeInsurance(21, 500, 150000);

            // discount percentage (3%) * yearly commission (500) = 15
            assertThat(discountService.determineDiscount(List.of(lifeInsurance))).isEqualTo(BigDecimal.valueOf(15));
        }

        @Test
        @DisplayName("When customer was 20 year at startdate of product, and has an insured amount larger than 100K then the discount percentage is 1%.")
        void lifeInsuranceWithDiscountOf1Percent() {
            var lifeInsurance = createLifeInsurance(20, 500, 150000);

            // discount percentage (1%) * yearly commission (500) = 5
            assertThat(discountService.determineDiscount(List.of(lifeInsurance))).isEqualTo(BigDecimal.valueOf(5));
        }

        @Test
        @DisplayName("When customer was 20 year at startdate of product, has an insured amount larger than 100K and commission over 1000 then the discount percentage is 1% and commission is maximized at 1000.")
        void lifeInsuranceWithDiscountOf1Percent_andCommissionOver1000() {
            var lifeInsurance = createLifeInsurance(20, 1500, 150000);

            // discount percentage (1%) * yearly commission (1000) = 10
            assertThat(discountService.determineDiscount(List.of(lifeInsurance))).isEqualTo(BigDecimal.valueOf(10));
        }

        @Test
        @DisplayName("When customer was 21 year at startdate of product, and has an insured amount smaller than 100K then the discount percentage is 0%.")
        void lifeInsuranceWithDiscountOf0Percent_insuredAmountSmallerThan100K() {
            var lifeInsurance = createLifeInsurance(21, 500, 99000);

            // discount percentage (0%) * yearly commission (500) = 0
            assertThat(discountService.determineDiscount(List.of(lifeInsurance))).isEqualTo(BigDecimal.valueOf(0));
        }

        @Test
        @DisplayName("When customer was 21 year at startdate of product, and has no insured amount then the discount percentage is 0%.")
        void lifeInsuranceWithDiscountOf0Percent_noInsuredAmount() {
            var lifeInsurance = createLifeInsurance(21, 500, null);

            // discount percentage (0%) * yearly commission (500) = 0
            assertThat(discountService.determineDiscount(List.of(lifeInsurance))).isEqualTo(BigDecimal.valueOf(0));
        }
    }

    @Nested
    @DisplayName("Non Life Insurance Tests")
    class NonLifeInsuranceDiscountTests {

        @Test
        @DisplayName("Non Life Insurances are never eligible for a discount")
        void nonLifeInsuranceIsNeverEligibleForADiscount() {
            var nonLifeProduct = createNonLifeInsurance();

            assertThat(discountService.determineDiscount(List.of(nonLifeProduct))).isEqualTo(BigDecimal.ZERO);
        }
    }

    private static Product createPensionProduct(Integer durationInYears, Integer monthlyDeposit) {
        return new PensionProduct("pension", LocalDate.now(), BigDecimal.valueOf(1000), BigDecimal.valueOf(monthlyDeposit), Optional.ofNullable(durationInYears).map(LocalDate.now()::plusYears).orElse(null), BigDecimal.valueOf(500000));
    }

    private static Product createMortgageProduct(String productName, int durationInMonths) {
        return new MortgageProduct(productName, LocalDate.now(), BigDecimal.valueOf(500), BigDecimal.valueOf(100), BigDecimal.valueOf(150), durationInMonths, BigDecimal.ONE);
    }

    private static Product createLifeInsurance(int ageCustomerAtStartDate, Integer yearlyCommission, Integer insuredAmount) {
        return new LifeInsuranceProduct("lifeInsurance", LocalDate.now(), Optional.ofNullable(yearlyCommission).map(BigDecimal::valueOf).orElse(null), Optional.ofNullable(insuredAmount).map(BigDecimal::valueOf).orElse(null), LocalDate.now().minusYears(ageCustomerAtStartDate));
    }

    private static Product createNonLifeInsurance() {
        return new NonLifeInsurance("NL", LocalDate.now(), BigDecimal.valueOf(500), BigDecimal.valueOf(130));
    }
}
