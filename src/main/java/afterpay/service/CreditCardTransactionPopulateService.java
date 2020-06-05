package afterpay.service;

import afterpay.exception.InvalidDateFormatException;
import afterpay.exception.InvalidNumberFormatException;
import afterpay.model.CreditCardTransaction;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.io.File;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Collection;
import java.util.Map;
import java.util.stream.BaseStream;

public class CreditCardTransactionPopulateService {
    private static final String commarDelimiter = ",";
    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");

    public Mono<Map<String, Collection<CreditCardTransaction>>> populateTransaction(File inputFile) {
        return readCreditCardTransactionFile(Paths.get(inputFile.getAbsolutePath()))
                .collectMultimap(
                        record -> {
                            String key = record.split(commarDelimiter)[0].trim();
                            return key;
                        },
                        record -> {
                            LocalDateTime dateTime = parseStringToDate(record.split(commarDelimiter)[1].trim());
                            BigDecimal amount = parseStringToBigDecimal(record.split(commarDelimiter)[2].trim());
                            return new CreditCardTransaction(dateTime, amount);
                        }
                );
    }

    private Flux<String> readCreditCardTransactionFile(Path path) {
        return Flux.using(() -> Files.lines(path),
                Flux::fromStream,
                BaseStream::close
        );
    }

    private LocalDateTime parseStringToDate(String dateTime) {
        try {
            return LocalDateTime.parse(dateTime);
        } catch (DateTimeParseException exception) {
            throw new InvalidDateFormatException("Date must be in yyyy-MM-dd HH:mm:ss - " + dateTime);
        }
    }

    private BigDecimal parseStringToBigDecimal(String number) {
        try {
            return BigDecimal.valueOf(Double.parseDouble(number));
        } catch (NumberFormatException exception) {
            throw new InvalidNumberFormatException("Invalid number - " + number);
        }
    }

}
