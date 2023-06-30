package nl.kooi.monads.domain.product;

import java.math.BigDecimal;
import java.time.LocalDate;


public record NonLifeInsurance(String productName,
                               LocalDate startDate,
                               BigDecimal yearlyCommission,
                               BigDecimal monthlyPremium) implements Product {

    @Override
    public ProductType productType() {
        return ProductType.NON_LIFE_INSURANCE;
    }
}
