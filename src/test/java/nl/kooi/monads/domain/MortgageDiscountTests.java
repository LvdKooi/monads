package nl.kooi.monads.domain;

import nl.kooi.monads.domain.product.MortgageProduct;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;

public class MortgageDiscountTests {

    private final DiscountService discountService = new DiscountService();

    @Test
    @DisplayName("When productName is  ANNUITY and duration in months is 360, then product is eligible for discount")
    void mortgageWithDiscount() {

        var mortgage = createMortgageProduct("ANNUITY", 360);

        // // discount percentage (0.01 % 360 = 3.6%) * yearly commission (500) = 18
        assertThat(discountService.determineDiscount(List.of(mortgage)))
                .isEqualTo(BigDecimal.valueOf(18).setScale(2, RoundingMode.HALF_UP));
    }

    @Test
    @DisplayName("When productName is not ANNUITY but duration in months is 360, then there is no discount")
    void mortgageWithDiscountOf0_notAnnuity() {

        var mortgage = createMortgageProduct("SAVINGS", 360);

        // // discount percentage (0%) * yearly commission (500) = 0
        assertThat(discountService.determineDiscount(List.of(mortgage)))
                .isEqualTo(BigDecimal.ZERO);
    }

    @Test
    @DisplayName("When productName is not ANNUITY but duration in months is 360, then there is no discount")
    void mortgageWithDiscountOf0_annuityButDurationLessThan360Months() {

        var mortgage = createMortgageProduct("ANNUITY", 359);

        // // discount percentage (0%) * yearly commission (500) = 0
        assertThat(discountService.determineDiscount(List.of(mortgage)))
                .isEqualTo(BigDecimal.ZERO);
    }

    private static MortgageProduct createMortgageProduct(String productName, int durationInMonths) {
        return new MortgageProduct(productName, LocalDate.now(), BigDecimal.valueOf(500), BigDecimal.valueOf(100), BigDecimal.valueOf(150), durationInMonths, BigDecimal.ONE);
    }
}
