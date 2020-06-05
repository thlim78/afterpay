package afterpay.model;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
public class CreditCardTransaction implements Comparable<CreditCardTransaction> {
    LocalDateTime dateTime;
    BigDecimal amount;

    @Override
    public int compareTo(CreditCardTransaction transaction) {
        return this.getDateTime().compareTo(transaction.getDateTime());
    }
}
