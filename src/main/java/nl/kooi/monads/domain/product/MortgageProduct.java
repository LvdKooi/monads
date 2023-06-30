package nl.kooi.monads.domain.product;

import java.math.BigDecimal;
import java.time.LocalDate;


public record MortgageProduct(String productName,
                              LocalDate startDate,
                              BigDecimal yearlyCommission,
                              BigDecimal monthlyPayment,
                              BigDecimal monthlySavingsAmount,
                              Integer durationInMonths,
                              BigDecimal interest) implements Product {

    @Override
    public ProductType productType() {
        return ProductType.MORTGAGE;
    }
}
