package nl.kooi.monads.domain;

import nl.kooi.monads.domain.product.LifeInsuranceProduct;
import nl.kooi.monads.domain.product.MortgageProduct;
import nl.kooi.monads.domain.product.NonLifeInsurance;
import nl.kooi.monads.domain.product.PensionProduct;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringJUnitConfig(DiscountServiceMonadized.class)
@ActiveProfiles("monad")
public class DiscountServiceTest {

    @Autowired
    private DiscountApi discountService;


    @Test
    public void testDetermineDiscount_withPensionProduct() {
        // Arrange
        var pensionProduct = new PensionProduct();
        pensionProduct.setStartDate(LocalDate.of(2000, 1, 1));
        pensionProduct.setEndDate(LocalDate.of(2022, 1, 1));
        pensionProduct.setMonthlyDeposit(BigDecimal.valueOf(400));
        pensionProduct.setYearlyCommission(BigDecimal.valueOf(100));

        // Act
        var discount = discountService.determineDiscount(List.of(pensionProduct));

        // Assert
        assertEquals(BigDecimal.valueOf(3), discount);
    }

    @Test
    public void testDetermineDiscount_withMortgageProduct() {
        // Arrange
        var mortgageProduct = new MortgageProduct();
        mortgageProduct.setProductName("ANNUITY");
        mortgageProduct.setDurationInMonths(360);
        mortgageProduct.setYearlyCommission(BigDecimal.valueOf(100));

        // Act
        var discount = discountService.determineDiscount(List.of(mortgageProduct));

        // Assert
        assertEquals(BigDecimal.valueOf(3.6), discount.setScale(1, RoundingMode.HALF_UP));
    }

    @Test
    public void testDetermineDiscount_withLifeInsuranceProduct() {
        // Arrange
        var lifeInsuranceProduct = new LifeInsuranceProduct();
        lifeInsuranceProduct.setInsuredAmount(BigDecimal.valueOf(200_000L));
        lifeInsuranceProduct.setBirthdateInsuredCustomer(LocalDate.of(1970, 1, 1));
        lifeInsuranceProduct.setYearlyCommission(BigDecimal.valueOf(100));

        // Act
        var discount = discountService.determineDiscount(List.of(lifeInsuranceProduct));

        // Assert
        assertEquals(BigDecimal.valueOf(3), discount);
    }

    @Test
    public void testDetermineDiscount_withNonLifeInsuranceProduct() {
        // Arrange
        var nonLifeInsuranceProduct = new NonLifeInsurance();

        // Act
        var discount = discountService.determineDiscount(List.of(nonLifeInsuranceProduct));

        // Assert
        assertEquals(BigDecimal.ZERO, discount);
    }

}
