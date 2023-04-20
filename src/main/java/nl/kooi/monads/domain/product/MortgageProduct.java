package nl.kooi.monads.domain.product;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;

@Data
@EqualsAndHashCode(callSuper = true)
public class MortgageProduct extends Product {
    private BigDecimal monthlyPayment;

    private BigDecimal monthlySavingsAmount;

    private Integer durationInMonths;

    private BigDecimal interest;

    @Override
    public ProductType getProductType() {
        return ProductType.MORTGAGE;
    }
}
