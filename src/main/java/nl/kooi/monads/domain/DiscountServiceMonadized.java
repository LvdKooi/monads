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
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Predicate;

import static nl.kooi.monads.util.BigDecimalUtils.*;
import static nl.kooi.monads.util.ProductUtils.*;

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
        return addAmounts(products, Product::yearlyCommission);
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
        return switch (product.productType()) {
            case PENSION -> PensionDiscountService.determinePensionDiscountPercentage(product);
            case MORTGAGE -> MortgageDiscountService.determineMortgageDiscountPercentage(product);
            case LIFE_INSURANCE -> LifeInsuranceDiscountService.determineLifeInsuranceDiscountPercentage(product);
            case NON_LIFE_INSURANCE -> BigDecimal.ZERO;
        };
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

    private static class PensionDiscountService {
        public static BigDecimal determinePensionDiscountPercentage(Product product) {
            return withProductAsType(product, PensionProduct.class)
                    .map(pensionProduct -> determineEndDateRelatedDiscount(pensionProduct)
                            .add(determineMonthlyDepositRelatedDiscount(pensionProduct)))
                    .orElse(BigDecimal.ZERO);
        }

        private static BigDecimal determineEndDateRelatedDiscount(PensionProduct product) {
            return Optional.ofNullable(product)
                    .filter(hasDate(PensionProduct::endDate).negate().or(isEndDateMoreThan20YearsAfterStartDate()))
                    .map(isEligibleForDiscount -> BigDecimal.valueOf(2))
                    .orElse(BigDecimal.ZERO);
        }

        private static BigDecimal determineMonthlyDepositRelatedDiscount(PensionProduct product) {
            return Optional.ofNullable(product)
                    .filter(isAmountAtLeast(PensionProduct::monthlyDeposit, 300))
                    .map(isEligibleForDiscount -> BigDecimal.ONE)
                    .orElse(BigDecimal.ZERO);
        }

        private static Predicate<PensionProduct> isEndDateMoreThan20YearsAfterStartDate() {
            return pension -> Optional.ofNullable(pension)
                    .filter(hasDate(PensionProduct::startDate))
                    .filter(hasDate(PensionProduct::endDate))
                    .filter(isEndDateMoreThanGivenYearsAfterStartDate(PensionProduct::startDate, PensionProduct::endDate, 20))
                    .isPresent();
        }
    }

    private static class MortgageDiscountService {
        public static BigDecimal determineMortgageDiscountPercentage(Product product) {
            return withProductAsType(product, MortgageProduct.class)
                    .filter(isAnnuity().and(hasDurationOf360Months()))
                    .map(MortgageProduct::durationInMonths)
                    .map(BigDecimal::valueOf)
                    .map(BigDecimal.valueOf(0.01)::multiply)
                    .orElse(BigDecimal.ZERO);
        }

        private static Predicate<MortgageProduct> isAnnuity() {
            return mortgage -> Optional.ofNullable(mortgage)
                    .map(MortgageProduct::productName)
                    .filter("ANNUITY"::equals)
                    .isPresent();
        }

        private static Predicate<MortgageProduct> hasDurationOf360Months() {
            return mortgageProduct -> Optional.ofNullable(mortgageProduct)
                    .map(MortgageProduct::durationInMonths)
                    .filter(Integer.valueOf(360)::equals)
                    .isPresent();
        }
    }

    private static class LifeInsuranceDiscountService {
        public static BigDecimal determineLifeInsuranceDiscountPercentage(Product product) {
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
                    .filter(isCustomerAtLeast21Years())
                    .map(isEligibleForDiscount -> BigDecimal.valueOf(2))
                    .orElse(BigDecimal.ZERO);
        }

        private static Predicate<LifeInsuranceProduct> isCustomerAtLeast21Years() {
            return isEndDateMoreThanGivenYearsAfterStartDate(LifeInsuranceProduct::birthdateInsuredCustomer,
                    p -> LocalDate.now(),
                    20);
        }

        private static Optional<LifeInsuranceProduct> withLifeInsuranceEligibleForDiscounts(Product product) {
            return withProductAsType(product, LifeInsuranceProduct.class)
                    .filter(lifeInsurance -> Objects.nonNull(lifeInsurance.insuredAmount()))
                    .filter(isAmountAtLeast(LifeInsuranceProduct::insuredAmount, 100000));
        }
    }
}

