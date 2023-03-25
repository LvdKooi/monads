package nl.kooi.monads.domain.product;

import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Getter
public abstract class Product {
    private UUID productReference;
    private ProductType productType;
    private String productName;
    private LocalDate startDate;

    private BigDecimal yearlyCommission;
}
