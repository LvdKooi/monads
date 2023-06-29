package nl.kooi.monads.domain.product;

import io.vavr.control.Either;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Optional;
import java.util.Random;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;
import java.util.function.Predicate;

class CalculationException extends RuntimeException {

    public CalculationException(String msg) {
        super(msg);
    }

    public static CalculationException oddNumberNotAllowed() {
        return new CalculationException("Invalid input: odd number provided.");
    }
}

public class CalculationService {
    public static void callMe() {

//        var outcome = doCalculation(5);
//        var outcome2 = doAnotherCalculation(5);

        var outcome3 = doSeriesOfCalculations(5);

        System.out.println(outcome3);
    }

    public static int doCalculation(int number) {
        return CompletableFuture.supplyAsync(() -> doCalculation(number))
                .exceptionally(CalculationService::handleArithmeticException)
                .thenApply(CalculationService::doSomewhatDifficultCalculation)
                .exceptionally(CalculationService::handleNumberFormatException)
                .thenApply(CalculationService::doIncrediblyDifficultCalculation)
                .exceptionally(CalculationService::handleDifficultCalcException)
                .join();
    }

    private static Predicate<Integer> isEven() {
        return number -> number % 2 == 0;
    }

    private static Either<? super CalculationException, Integer> doEvenCalculation(int number) {
        return Optional.of(number)
                .filter(isEven())
                .map(CalculationService::doCalculation)
                .map(Either::right)
                .orElseGet(() -> Either.left(CalculationException.oddNumberNotAllowed()));
    }

    private static BigDecimal applyPercentage(int percentage, int baseAmount) {
        return BigDecimal.valueOf(percentage)
                .divide(BigDecimal.valueOf(100), RoundingMode.HALF_UP)
                .multiply(BigDecimal.valueOf(baseAmount));
    }

    public static BigDecimal doSeriesOfCalculations(int number) {
        return doEvenCalculation(number)
                .map(calculatedNumber -> applyPercentage(85, calculatedNumber))
                .getOrElseThrow(Function.identity().andThen(CalculationException.class::cast));
    }

//        Try.of(() -> doCalculation(number))
//            .recover(x -> Match(x).of(
//            Case($(instanceOf(ArithmeticException.class)), CalcService::handleArithmeticException),
//    Case($(instanceOf(NumberFormatException.class)), CalcService::handleNumberFormatException)))
//            .getOrElse(0);


    private static Integer handleDifficultCalcException(Throwable e) {
        System.out.println(String.format("Got exception:: %s", e.getMessage()));
        return 556;
    }


    private static int doEasyCalculation(int number) {
        var i = Math.random() > 0.5 ? 1 : 0;
        return number / i;
    }

    private static int handleArithmeticException(Throwable e) {
        System.out.println(String.format("Got exception:: %s", e.getMessage()));
        return 3;
    }

    private static int handleNumberFormatException(Throwable e) {
        System.out.println(String.format("Got exception:: %s", e.getMessage()));
        return 300;
    }

    private static int doSomewhatDifficultCalculation(int number) {
        var i = Math.random() > 0.5 ? Integer.valueOf(number).toString() : "10_00";
        return Integer.parseInt(i);
    }

    private static int doIncrediblyDifficultCalculation(int number) {
        var newNumber = new Random().nextInt(10);

        return switch (newNumber) {
            case 1, 2, 3 -> throw new NullPointerException();
            default -> 37 * number;
        };
    }
}
