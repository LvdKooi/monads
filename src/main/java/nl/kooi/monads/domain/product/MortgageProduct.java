package nl.kooi.monads.domain.product;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;


public record MortgageProduct(UUID productReference,
                              String productName,
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
