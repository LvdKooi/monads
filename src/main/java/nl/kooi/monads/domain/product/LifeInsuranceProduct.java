package nl.kooi.monads.domain.product;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@EqualsAndHashCode(callSuper = true)
public class LifeInsuranceProduct extends Product {

    private BigDecimal insuredAmount;

    private LocalDate birthdateInsuredCustomer;

    @Override
    public ProductType getProductType() {
        return ProductType.LIFE_INSURANCE;
    }
}
