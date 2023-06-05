package nl.kooi.monads.domain;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import nl.kooi.monads.domain.product.LifeInsuranceProduct;
import nl.kooi.monads.domain.product.MortgageProduct;
import nl.kooi.monads.domain.product.PensionProduct;
import nl.kooi.monads.domain.product.Product;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.Period;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.UnaryOperator;

import static java.math.RoundingMode.HALF_UP;

@Service
@RequiredArgsConstructor
@Slf4j
@Profile("monad")
public class DiscountServiceMonadized implements DiscountApi {

    @Override
    public BigDecimal determineDiscount(List<Product> products) {
        return calculateDiscount(
                determineCommission(products),
                determineDiscountPercentage(products));
    }

    private static BigDecimal determineDiscountPercentage(List<Product> products) {
        return addAmounts(products, DiscountServiceMonadized::determineDiscountPercentage);
    }

    private static BigDecimal determineCommission(List<Product> products) {
        return addAmounts(products, Product::getYearlyCommission);
    }

    private static BigDecimal addAmounts(List<Product> products, Function<Product, BigDecimal> amountFunction) {
        return Optional.ofNullable(products)
                .orElseGet(Collections::emptyList)
                .stream()
                .map(amountFunction)
                .filter(Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private static BigDecimal determineDiscountPercentage(Product product) {
        return switch (product.getProductType()) {
            case PENSION -> determinePensionDiscountPercentage(product);
            case MORTGAGE -> determineMortgageDiscountPercentage(product);
            case LIFE_INSURANCE -> determineLifeInsuranceDiscountPercentage(product);
            case NON_LIFE_INSURANCE -> BigDecimal.ZERO;
        };
    }

    private static BigDecimal determinePensionDiscountPercentage(Product product) {
        return Optional.ofNullable(product)
                .filter(PensionProduct.class::isInstance)
                .map(PensionProduct.class::cast)
                .map(pensionProduct -> determineEndDateRelatedDiscount(pensionProduct)
                        .add(determineMonthlyDepositRelatedDiscount(pensionProduct)))
                .orElse(BigDecimal.ZERO);
    }

    private static BigDecimal determineEndDateRelatedDiscount(PensionProduct product) {
        return Optional.ofNullable(product)
                .filter(hasDate(PensionProduct::getEndDate).negate().or(isEndDateMoreThan20YearsAfterStartDate()))
                .map(isEligibleForDiscount -> BigDecimal.valueOf(2))
                .orElse(BigDecimal.ZERO);
    }

    private static BigDecimal determineMonthlyDepositRelatedDiscount(PensionProduct product) {
        return Optional.ofNullable(product)
                .filter(isAmountAtLeast(PensionProduct::getMonthlyDeposit, 300))
                .map(isEligibleForDiscount -> BigDecimal.ONE)
                .orElse(BigDecimal.ZERO);
    }

    private static <T extends Product> Predicate<T> hasDate(Function<T, LocalDate> dateFunction) {
        return pension -> Optional.ofNullable(pension).map(dateFunction).isPresent();
    }

    private static Predicate<PensionProduct> isEndDateMoreThan20YearsAfterStartDate() {
        return pension -> Optional.ofNullable(pension)
                .filter(hasDate(PensionProduct::getStartDate))
                .filter(hasDate(PensionProduct::getEndDate))
                .filter(isEndDateMoreThanGivenYearsAfterStartDate(PensionProduct::getStartDate, PensionProduct::getEndDate, 20))
                .isPresent();
    }

    private static <T extends Product> Predicate<T> isEndDateMoreThanGivenYearsAfterStartDate(Function<T, LocalDate> firstDate, Function<T, LocalDate> secondDate, int moreThan) {
        return product -> Optional.ofNullable(product)
                .filter(hasDate(firstDate))
                .filter(hasDate(secondDate))
                .filter(pr -> Period.between(firstDate.apply(pr), secondDate.apply(pr)).getYears() > moreThan)
                .isPresent();
    }

    private static <T extends Product> Predicate<T> isAmountAtLeast(Function<T, BigDecimal> amountFunction,
                                                                    int atLeast) {
        return product -> Optional.ofNullable(product)
                .map(amountFunction)
                .filter(amount -> amount.compareTo(BigDecimal.valueOf(atLeast)) >= 0)
                .isPresent();
    }

    private static BigDecimal determineLifeInsuranceDiscountPercentage(Product product) {
        return determineLifeInsuranceBaseDiscount(product)
                .add(determineAgeBaseLifeInsuranceDiscount(product));
    }

    private static BigDecimal determineLifeInsuranceBaseDiscount(Product product) {
        return withLifeInsuranceEligibleForDiscounts(product)
                .map(isEligibleForDiscount -> BigDecimal.ONE)
                .orElse(BigDecimal.ZERO);
    }

    private static BigDecimal determineAgeBaseLifeInsuranceDiscount(Product product) {
        return withLifeInsuranceEligibleForDiscounts(product)
                .filter(isCustomerAtLeast20Years())
                .map(isEligibleForDiscount -> BigDecimal.valueOf(3))
                .orElse(BigDecimal.ZERO);
    }

    private static Predicate<LifeInsuranceProduct> isCustomerAtLeast20Years() {
        return isEndDateMoreThanGivenYearsAfterStartDate(LifeInsuranceProduct::getBirthdateInsuredCustomer,
                p -> LocalDate.now(),
                20);
    }

    private static Optional<LifeInsuranceProduct> withLifeInsuranceEligibleForDiscounts(Product product) {
        return Optional.ofNullable(product)
                .filter(LifeInsuranceProduct.class::isInstance)
                .map(LifeInsuranceProduct.class::cast)
                .filter(lifeInsurance -> Objects.nonNull(lifeInsurance.getInsuredAmount()))
                .filter(isAmountAtLeast(LifeInsuranceProduct::getInsuredAmount, 300000));
    }

    private static BigDecimal determineMortgageDiscountPercentage(Product product) {
        return Optional.ofNullable(product)
                .filter(MortgageProduct.class::isInstance)
                .map(MortgageProduct.class::cast)
                .filter(isAnnuity().and(hasDurationOf360Months()))
                .map(MortgageProduct::getDurationInMonths)
                .map(BigDecimal::valueOf)
                .map(BigDecimal.valueOf(0.01)::multiply)
                .orElse(BigDecimal.ZERO);
    }

    private static Predicate<MortgageProduct> isAnnuity() {
        return mortgage -> Optional.ofNullable(mortgage)
                .map(MortgageProduct::getProductName)
                .map("ANNUITY"::equals)
                .orElse(false);
    }

    private static Predicate<MortgageProduct> hasDurationOf360Months() {
        return mortgageProduct -> Optional.ofNullable(mortgageProduct)
                .map(MortgageProduct::getDurationInMonths)
                .map(Integer.valueOf(360)::equals)
                .orElse(false);
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
}

