package nl.kooi.monads.domain.product;

import java.math.BigDecimal;
import java.time.LocalDate;


public record PensionProduct(String productName,
                             LocalDate startDate,
                             BigDecimal yearlyCommission,
                             BigDecimal monthlyDeposit,
                             LocalDate endDate,
                             BigDecimal targetCapital) implements Product {

    @Override
    public ProductType productType() {
        return ProductType.PENSION;
    }
}
