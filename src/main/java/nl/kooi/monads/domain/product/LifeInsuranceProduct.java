package nl.kooi.monads.domain.product;

import java.math.BigDecimal;
import java.time.LocalDate;

public record LifeInsuranceProduct(String productName,
                                   LocalDate startDate,
                                   BigDecimal yearlyCommission,
                                   BigDecimal insuredAmount,
                                   LocalDate birthdateInsuredCustomer) implements Product {

    @Override
    public ProductType productType() {
        return ProductType.LIFE_INSURANCE;
    }
}
