package nl.kooi.monads.domain.product;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public interface Product {

    String productName();

    LocalDate startDate();

    BigDecimal yearlyCommission();

    ProductType productType();
}
