package nl.kooi.monads.domain.product;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public record LifeInsuranceProduct(UUID productReference,
                                   String productName,
                                   LocalDate startDate,
                                   BigDecimal yearlyCommission,
                                   BigDecimal insuredAmount,
                                   LocalDate birthdateInsuredCustomer) implements Product {

    @Override
    public ProductType productType() {
        return ProductType.LIFE_INSURANCE;
    }
}
