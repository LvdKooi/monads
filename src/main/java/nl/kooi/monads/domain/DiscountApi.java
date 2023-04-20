package nl.kooi.monads.domain;

import nl.kooi.monads.domain.product.Product;

import java.math.BigDecimal;
import java.util.List;

public interface DiscountApi {

    BigDecimal determineDiscount(List<Product> products);
}
