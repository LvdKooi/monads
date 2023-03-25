package nl.kooi.monads.domain.product;

import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
public class PensionProduct extends Product {

    private BigDecimal monthlyDeposit;

    private LocalDate endDate;

    private BigDecimal targetCapital;
}
