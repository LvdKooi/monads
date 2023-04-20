package nl.kooi.monads.domain.product;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;

@Data
@EqualsAndHashCode(callSuper = true)
public class NonLifeInsurance extends Product {

    private BigDecimal monthlyPremium;

    @Override
    public ProductType getProductType() {
        return ProductType.NON_LIFE_INSURANCE;
    }
}
