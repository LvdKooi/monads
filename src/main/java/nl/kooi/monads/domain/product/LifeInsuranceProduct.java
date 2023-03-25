package nl.kooi.monads.domain.product;

import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
public class LifeInsuranceProduct extends Product {

    private BigDecimal insuredAmount;

    private LocalDate birthdateInsuredCustomer;
}
