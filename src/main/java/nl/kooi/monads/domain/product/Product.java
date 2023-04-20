package nl.kooi.monads.domain.product;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Data
public abstract class Product {
    private UUID productReference;
    private String productName;
    private LocalDate startDate;

    private BigDecimal yearlyCommission;

    public abstract ProductType getProductType();
}
