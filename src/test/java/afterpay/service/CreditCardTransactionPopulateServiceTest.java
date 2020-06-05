package afterpay.service;

import afterpay.exception.InvalidDateFormatException;
import afterpay.exception.InvalidNumberFormatException;
import afterpay.model.CreditCardTransaction;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;
import java.io.File;
import java.util.*;

@RunWith(JUnit4.class)
public class CreditCardTransactionPopulateServiceTest {

    @Test
    public void transactionsPopulatedFromValidInputFile() {
        File validInputFile = new File(getClass().getClassLoader().getResource("validInputFile.csv").getFile());
        CreditCardTransactionPopulateService service = new CreditCardTransactionPopulateService();
        Mono<Map<String, Collection<CreditCardTransaction>>> creditCardTransactions = service.populateTransaction(validInputFile);

        List<String> expectedHashedCreditCardNumbers = Arrays.asList("10d7ce2f43e35fa57d1bbf8b1e2","10d7ce2f43e35fa57d1bbf8b1e3","10d7ce2f43e35fa57d1bbf8b1e4");

        StepVerifier.create(creditCardTransactions)
                .expectNextMatches(map -> map.keySet().size() == expectedHashedCreditCardNumbers.size() &&
                        map.keySet().containsAll(expectedHashedCreditCardNumbers) &&
                        map.get("10d7ce2f43e35fa57d1bbf8b1e2").size() == 4 &&
                        map.get("10d7ce2f43e35fa57d1bbf8b1e3").size() == 2 &&
                        map.get("10d7ce2f43e35fa57d1bbf8b1e4").size() == 1)
                .verifyComplete();
    }

    @Test
    public void zeroTransactionsFromEmptyInputFile() {
        File emptyInputFile = new File(getClass().getClassLoader().getResource("emptyInputFile.csv").getFile());
        CreditCardTransactionPopulateService service = new CreditCardTransactionPopulateService();

        StepVerifier.create(service.populateTransaction(emptyInputFile))
                .expectNextMatches(map -> map.size() == 0)
                .verifyComplete();
    }

    @Test
    public void raiseInvalidDateFormatException() {
        File invalidDateFormatInputFile = new File(getClass().getClassLoader().getResource("invalidDateFormatInputFile.csv").getFile());
        CreditCardTransactionPopulateService service = new CreditCardTransactionPopulateService();

        StepVerifier.create(service.populateTransaction(invalidDateFormatInputFile))
                .expectErrorMatches(throwable -> throwable instanceof InvalidDateFormatException
                        && throwable.getMessage().startsWith("Date must be in yyyy-MM-dd HH:mm:ss"))
                .verify();
    }

    @Test
    public void raiseInvalidNumberFormatException() {
        File invalidNumberFormatInputFile = new File(getClass().getClassLoader().getResource("invalidNumberFormatInputFile.csv").getFile());
        CreditCardTransactionPopulateService service = new CreditCardTransactionPopulateService();

        StepVerifier.create(service.populateTransaction(invalidNumberFormatInputFile))
                .expectErrorMatches(throwable -> throwable instanceof InvalidNumberFormatException
                        && throwable.getMessage().startsWith("Invalid number"))
                .verify();
    }
}
