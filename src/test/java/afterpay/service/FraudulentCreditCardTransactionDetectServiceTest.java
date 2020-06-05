package afterpay.service;

import afterpay.model.CreditCardTransaction;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;

@RunWith(JUnit4.class)
public class FraudulentCreditCardTransactionDetectServiceTest {

    @Test
    public void shouldReturnEmptyFraudulentTransactions() {
        FraudulentCreditCardTransactionDetectService service = new FraudulentCreditCardTransactionDetectService();

        StepVerifier.create(service.getFraudulentCreditCardDetails(Mono.empty(), BigDecimal.valueOf(100.00)))
                .expectNextCount(0l) // expect NO hashed credit card details
                .verifyComplete();
    }

    @Test
    public void shouldReturnFraudulentCreditCardDetailsOver24HoursSlidingWindowPeriodExceedsPriceThreshold() {
        // Given - preparing test data
        Map<String, Collection<CreditCardTransaction>> creditCardTransactionsMap = new HashMap<>();

        // only 2nd and 3rd transactions within 24 hours between them, and the sum of them exceeds 100.00 thresholds
        List list1 = Arrays.asList(
                new CreditCardTransaction(LocalDateTime.parse("2003-08-04T10:11:30"), BigDecimal.valueOf(20.00)),
                new CreditCardTransaction(LocalDateTime.parse("2004-04-29T13:15:55"), BigDecimal.valueOf(70.00)),
                new CreditCardTransaction(LocalDateTime.parse("2004-04-29T13:15:56"), BigDecimal.valueOf(90.00)));

        creditCardTransactionsMap.put("10d7ce2f43e35fa57d1bbf8b1e2", list1);

        // none of the transactions are within 24 hours between them
        List list2 = Arrays.asList(
                new CreditCardTransaction(LocalDateTime.parse("2009-04-29T13:15:58"), BigDecimal.valueOf(20.00)),
                new CreditCardTransaction(LocalDateTime.parse("2009-05-29T13:15:58"), BigDecimal.valueOf(10.00)),
                new CreditCardTransaction(LocalDateTime.parse("2009-06-29T13:15:58"), BigDecimal.valueOf(90.00)));

        creditCardTransactionsMap.put("10d7ce2f43e35fa57d1bbf8b1e3", list2);

        // only 1st and 3rd transactions not in chronological order are within 24 hours between them,
        // and the sum of them exceeds 100.00 thresholds
        List list3 = Arrays.asList(
                new CreditCardTransaction(LocalDateTime.parse("2009-04-29T13:15:58"), BigDecimal.valueOf(20.00)),
                new CreditCardTransaction(LocalDateTime.parse("2009-05-29T13:15:58"), BigDecimal.valueOf(10.00)),
                new CreditCardTransaction(LocalDateTime.parse("2009-04-29T14:15:58"), BigDecimal.valueOf(90.00)));

        creditCardTransactionsMap.put("10d7ce2f43e35fa57d1bbf8b1e4", list3);

        Mono<Map<String, Collection<CreditCardTransaction>>> creditCardTransactions = Mono.just(creditCardTransactionsMap);

        // When
        FraudulentCreditCardTransactionDetectService service = new FraudulentCreditCardTransactionDetectService();
        Flux<String> fraudulentCreditCardDetails = service.getFraudulentCreditCardDetails(creditCardTransactions, BigDecimal.valueOf(100.00));

        // Then
        StepVerifier.create(fraudulentCreditCardDetails)
                .expectNext("10d7ce2f43e35fa57d1bbf8b1e2")
                .expectNext("10d7ce2f43e35fa57d1bbf8b1e4")
                .verifyComplete();
    }
}
