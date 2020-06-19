package afterpay.service;

import afterpay.model.CreditCardTransaction;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

public class FraudulentCreditCardTransactionDetectService {

    public Flux<String> getFraudulentCreditCardDetails(Mono<Map<String, Collection<CreditCardTransaction>>> creditCardTransactions,
                                           BigDecimal threshold) {
        List<String> fraudulentCreditCards = new ArrayList<>();

        creditCardTransactions
            .subscribe(map -> {
                map.forEach((key, value) -> {
                    Mono<Boolean> anyFraudulentTransaction = Flux.fromIterable(value)
                        // sort the collection of credit card transactions based on local date time
                        .sort()
                        // determine any fraudulent transactions belonged to the hashed credit card number
                        .any(transaction -> isTransactionFraudulent(transaction, value, threshold));

                    anyFraudulentTransaction.subscribe(outcome -> {
                        // report the fraudulent transactions belonged to the hashed credit card number
                        if (outcome) {
                            fraudulentCreditCards.add(key);
                        }
                    });
                });
            });

        return Flux.fromIterable(fraudulentCreditCards).sort();
    }

    private Boolean isTransactionFraudulent(CreditCardTransaction startTransaction, Collection<CreditCardTransaction> transactions, BigDecimal threshold ) {
        LocalDateTime startDateTime = startTransaction.getDateTime();
        // compute end datetime from start datetime + 24 hours
        LocalDateTime endDateTime = startTransaction.getDateTime().plusHours(24);

        //System.out.println("Start date time: " + startDateTime);
        //System.out.println("End date time: " + endDateTime);

        BigDecimal sum = transactions.stream()
                .filter(transaction -> isTransactionDateBetweenStartAndEndDateTime(transaction.getDateTime(), startDateTime, endDateTime))
                .map(transaction -> transaction.getAmount())
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        //System.out.println("Sum: " + sum);

        return isThresholdExceeded(sum, threshold);
    }

    private Boolean isThresholdExceeded (BigDecimal sum, BigDecimal threshold) {
        return sum.compareTo(threshold) == 1;
    }

    private Boolean isTransactionDateBetweenStartAndEndDateTime(LocalDateTime dateTime, LocalDateTime startDateTime, LocalDateTime endDateTime) {
        return startDateTime.compareTo(dateTime) <= 0 && endDateTime.compareTo(dateTime) >= 0;
    }
}
