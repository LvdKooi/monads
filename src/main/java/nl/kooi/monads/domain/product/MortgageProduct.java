package nl.kooi.monads.domain.product;

import lombok.Getter;

import java.math.BigDecimal;

@Getter
public class MortgageProduct extends Product {
    private BigDecimal monthlyPayment;

    private BigDecimal monthlySavingsAmount;

    private Integer durationInMonths;

    private BigDecimal interest;
}
