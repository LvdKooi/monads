package nl.kooi.monads.domain.product;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;


public record NonLifeInsurance(UUID productReference,
                               String productName,
                               LocalDate startDate,
                               BigDecimal yearlyCommission,
                               BigDecimal monthlyPremium) implements Product {

    @Override
    public ProductType productType() {
        return ProductType.NON_LIFE_INSURANCE;
    }
}
