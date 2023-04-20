package nl.kooi.monads.domain.product;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@EqualsAndHashCode(callSuper = true)
public class PensionProduct extends Product {

    private BigDecimal monthlyDeposit;

    private LocalDate endDate;

    private BigDecimal targetCapital;

    @Override
    public ProductType getProductType() {
        return ProductType.PENSION;
    }
}
