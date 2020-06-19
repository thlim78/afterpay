package afterpay;

import afterpay.exception.FileNotFoundException;
import afterpay.exception.InvalidFileContentException;
import afterpay.exception.InvalidNumberThresholdException;
import afterpay.model.CreditCardTransaction;
import afterpay.service.CreditCardTransactionPopulateService;
import afterpay.service.FraudulentCreditCardTransactionDetectService;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.io.File;
import java.math.BigDecimal;
import java.util.Collection;
import java.util.Map;

/*
    Why reactive ? Many reasons.
    1. Reactive is non-blocking, asychronous, event driven, requires small number of threads to scale vertically.
    2. It introduces back pressure mechanism, allowing subscriber to tell publisher how much data the subscriber is able to handle and process at one time.
    3. It is already a trend, and is believed will soon be commonly applied in all applications.
    4. Why? Because of 5G, large volume of data stream will be produced at a enourmous speed, users are expecting quicker response,
    current conventional blocking mechanism may not serve well to meet the above requirement. Non-blocking mechanism is the way forward.
    5. To achieve non-blocking, the apps needs to program reactively, from reactive app server (netty) to reactive repositories e.g. MongoDb.
*/

public class App {

    public static void main(String[] args) {
        // validate number of arguments
        if (args.length < 2) {
            System.out.println("Usage: afterpay.App <threshold> <filename.csv>");
            System.exit(-1);
        }

        BigDecimal threshold = parseThreshold(args[0]);
        // Assumption: Detect frauds based on credit card spending, not refund
        // Only accept positive threshold value
        validateThreshold(threshold);

        File inputFile = new File(args[1]);
        validateInputFile(inputFile);

        try {
            CreditCardTransactionPopulateService creditCardTransactionPopulateService = new CreditCardTransactionPopulateService();
            Mono<Map<String, Collection<CreditCardTransaction>>> creditCardTransactions = creditCardTransactionPopulateService.populateTransaction(inputFile);

            FraudulentCreditCardTransactionDetectService fraudulentCreditCardTransactionDetectService = new FraudulentCreditCardTransactionDetectService();
            Flux<String> fraudulentCreditCardDetails = fraudulentCreditCardTransactionDetectService
                    .getFraudulentCreditCardDetails(creditCardTransactions, threshold);

            fraudulentCreditCardDetails
                .defaultIfEmpty("NONE")
                .reduce((s1, s2) -> s1 + ", " + s2)
                .subscribe(cardDetail -> System.out.println("The fraudulent credit card details are: " + cardDetail));
        } catch (Exception e) {
            System.err.println("Exception: " + e.getMessage());
        }
    }

    private static BigDecimal parseThreshold(String value) {
        try {
            return BigDecimal.valueOf(Double.parseDouble(value)) ;
        } catch (NumberFormatException exception) {
            System.err.println("Failed to parse threshold value");
            throw new InvalidNumberThresholdException("Failed to parse threshold value");
        }
    }

    private static void validateThreshold(BigDecimal threshold) {
        if (threshold.compareTo(BigDecimal.valueOf(0L)) != 1 ) {
            System.err.println("Threshold must be positive value");
            throw new InvalidNumberThresholdException("Threshold must be positive value");
        }
    }

    private static void validateInputFile(File inputFile) {
        if (!inputFile.exists()) {
            System.err.println("Credit card transaction input file not found");
            throw new FileNotFoundException("Credit card transaction input file not found");
        }

        if (inputFile.length() == 0) {
            System.err.println("Credit card input file is empty");
            throw new InvalidFileContentException("Credit card input file is empty");
        }
    }
}