package nl.kooi.monads.domain;

import lombok.RequiredArgsConstructor;
import nl.kooi.monads.domain.product.LifeInsuranceProduct;
import nl.kooi.monads.domain.product.MortgageProduct;
import nl.kooi.monads.domain.product.PensionProduct;
import nl.kooi.monads.domain.product.Product;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.Period;
import java.util.List;

import static java.math.RoundingMode.HALF_UP;

@Service
@RequiredArgsConstructor
@Profile("monadless")
public class DiscountService implements DiscountApi {

    @Override
    public BigDecimal determineDiscount(List<Product> products) {
        var discountPercentage = BigDecimal.ZERO;
        var commission = BigDecimal.ZERO;

        for (var product : products) {
            discountPercentage = discountPercentage.add(determineDiscountPercentage(product));
            commission = commission.add(product.yearlyCommission() == null ? BigDecimal.ZERO : product.yearlyCommission());
        }

        return calculateDiscount(commission, discountPercentage);
    }

    private BigDecimal determineDiscountPercentage(Product product) {
        switch (product.productType()) {
            case PENSION:
                return determinePensionDiscountPercentage((PensionProduct) product);

            case MORTGAGE:
                return determineMortgageDiscountPercentage((MortgageProduct) product);

            case LIFE_INSURANCE:
                return determineLifeInsuranceDiscountPercentage((LifeInsuranceProduct) product);

            case NON_LIFE_INSURANCE:
                return BigDecimal.ZERO;
        }

        return null;
    }

    private static BigDecimal determinePensionDiscountPercentage(PensionProduct product) {
        var discountPercentage = BigDecimal.ZERO;

        if (product != null) {
            if (product.endDate() == null || (Period.between(product.startDate(), product.endDate()).getYears() > 20)) {
                discountPercentage = discountPercentage.add(BigDecimal.valueOf(2));
            }

            if (product.monthlyDeposit().compareTo(BigDecimal.valueOf(300)) >= 0) {
                discountPercentage = discountPercentage.add(BigDecimal.valueOf(1));
            }
        }

        return discountPercentage;
    }

    private static BigDecimal determineMortgageDiscountPercentage(MortgageProduct product) {
        var discountPercentage = BigDecimal.ZERO;

        if (product != null) {
            if (product.productName().equals("ANNUITY") && product.durationInMonths() == 360) {
                discountPercentage = discountPercentage.add(BigDecimal.valueOf(0.01)
                        .multiply(BigDecimal.valueOf(product.durationInMonths())));
            }
        }

        return discountPercentage;
    }

    private static BigDecimal determineLifeInsuranceDiscountPercentage(LifeInsuranceProduct product) {
        if (product != null && product.insuredAmount() != null && product.insuredAmount().compareTo(BigDecimal.valueOf(100_000L)) >= 0) {

            if (Period.between(product.birthdateInsuredCustomer(), LocalDate.now()).getYears() > 20) {
                return BigDecimal.valueOf(3);
            }

            return BigDecimal.ONE;
        }

        return BigDecimal.ZERO;
    }

    private static BigDecimal calculateDiscount(BigDecimal amount, BigDecimal discountPercentage) {
        if (amount != null && amount.compareTo(BigDecimal.valueOf(50)) >= 0) {

            var baseAmount = amount;

            if (baseAmount.compareTo(BigDecimal.valueOf(1000)) > 0) {
                baseAmount = BigDecimal.valueOf(1000);
            }

            if (discountPercentage != null) {
                return baseAmount.setScale(0, HALF_UP)
                        .divide(BigDecimal.valueOf(100), 0, HALF_UP)
                        .multiply(discountPercentage);
            }
        }

        return BigDecimal.ZERO;
    }
}
