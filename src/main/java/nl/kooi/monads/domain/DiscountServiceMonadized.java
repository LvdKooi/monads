package nl.kooi.monads.domain;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import nl.kooi.monads.domain.product.LifeInsuranceProduct;
import nl.kooi.monads.domain.product.MortgageProduct;
import nl.kooi.monads.domain.product.PensionProduct;
import nl.kooi.monads.domain.product.Product;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.Period;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.function.UnaryOperator;

import static java.math.RoundingMode.HALF_UP;

@Service
@RequiredArgsConstructor
@Slf4j
public class DiscountServiceMonadized {

    public BigDecimal determineDiscount(List<Product> products) {
        var discountPercentage = products.stream()
                .map(this::determineDiscountPercentage)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        var commission = products.stream()
                .map(Product::getYearlyCommission)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return calculateDiscount(commission, discountPercentage);
    }

    private BigDecimal determineDiscountPercentage(Product product) {
        switch (product.getProductType()) {
            case PENSION -> {
                return determinePensionDiscountPercentage((PensionProduct) product);
            }
            case MORTGAGE -> {
                return determineMortgageDiscountPercentage((MortgageProduct) product);
            }
            case LIFE_INSURANCE -> {
                return determineLifeInsuranceDiscountPercentage((LifeInsuranceProduct) product);
            }
            case NON_LIFE_INSURANCE -> {
                return BigDecimal.ZERO;
            }
        }

        return null;
    }

    private static BigDecimal determinePensionDiscountPercentage(Product product) {
        return Optional.ofNullable(product)
                .map(DiscountServiceMonadized::calculatePeriodRelatedDiscount)
                .map(discount -> calculateMonthylDepositRelatedDiscount(discount, product))
                .orElse(BigDecimal.ZERO);
    }

    private static BigDecimal calculateMonthylDepositRelatedDiscount(BigDecimal discount, Product product) {
        return getPensionProductOptional(product)
                .map(PensionProduct::getMonthlyDeposit)
                .filter(isAtLeast(300))
                .map(p -> BigDecimal.ONE)
                .orElse(BigDecimal.ZERO)
                .add(discount);
    }

    private static BigDecimal calculatePeriodRelatedDiscount(Product product) {
        return getPensionProductOptional(product)
                .filter(noEndDateOrOver20Years())
                .map(p -> BigDecimal.valueOf(2))
                .orElse(BigDecimal.ZERO);
    }

    private static Optional<PensionProduct> getPensionProductOptional(Product product) {
        return Optional.ofNullable(product)
                .filter(PensionProduct.class::isInstance)
                .map(PensionProduct.class::cast);
    }

    private static BigDecimal determineMortgageDiscountPercentage(MortgageProduct product) {
        return Optional.ofNullable(product)
                .filter(p -> p.getProductName().equals("ANNUITY") && p.getDurationInMonths() == 360)
                .map(p -> BigDecimal.valueOf(0.01).multiply(BigDecimal.valueOf(p.getDurationInMonths())))
                .orElse(BigDecimal.ZERO);
    }

    private static BigDecimal determineLifeInsuranceDiscountPercentage(LifeInsuranceProduct product) {
        return Optional.ofNullable(product)
                .filter(p -> Objects.nonNull(p.getInsuredAmount()))
                .filter(p -> p.getInsuredAmount().compareTo(BigDecimal.valueOf(100_000L)) >= 1)
                .filter(p -> Period.between(p.getBirthdateInsuredCustomer(), LocalDate.now()).getYears() > 20)
                .map(p -> BigDecimal.valueOf(3))
                .orElse(BigDecimal.ZERO);
    }

    private static BigDecimal calculateDiscount(BigDecimal amount, BigDecimal discountPercentage) {
        return Optional.ofNullable(amount)
                .filter(isAtLeast(50))
                .map(maximizeAt(1000))
                .map(roundUp())
                .map(divideBy(100))
                .map(multiplyBy(discountPercentage))
                .orElse(BigDecimal.ZERO);
    }

    private static Predicate<BigDecimal> isAtLeast(long amount) {
        return bd -> bd.compareTo(BigDecimal.valueOf(amount)) >= 0;
    }

    private static UnaryOperator<BigDecimal> maximizeAt(long amount) {
        return bd -> bd.min(BigDecimal.valueOf(amount));
    }

    private static UnaryOperator<BigDecimal> roundUp() {
        return bd -> bd.setScale(0, HALF_UP);
    }

    private static UnaryOperator<BigDecimal> divideBy(long amount) {
        return bd -> bd.divide(BigDecimal.valueOf(amount), 0, HALF_UP);
    }

    private static UnaryOperator<BigDecimal> multiplyBy(BigDecimal amount) {
        return bd -> Optional.ofNullable(amount).orElse(BigDecimal.ONE).multiply(bd);
    }

    private static Predicate<PensionProduct> noEndDateOrOver20Years() {
        return p -> p.getEndDate() == null || (Period.between(p.getStartDate(), p.getEndDate()).getYears() > 20);
    }
}

