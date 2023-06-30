package nl.kooi.monads.domain.product;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;


public record PensionProduct(UUID productReference,
                             String productName,
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
