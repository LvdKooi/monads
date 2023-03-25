package nl.kooi.monads.domain.product;

import lombok.Getter;

import java.math.BigDecimal;

@Getter
public class NonLifeInsurance extends Product {

    private BigDecimal monthlyPremium;

}
