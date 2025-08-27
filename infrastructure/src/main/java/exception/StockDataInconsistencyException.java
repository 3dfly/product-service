package exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;

/**
 * Custom exception for stock data inconsistency issues.
 * Thrown when stock information is expected but not found due to data integrity problems.
 */
@Slf4j
public class StockDataInconsistencyException extends BaseException {
    private StockDataInconsistencyException(String message) {
        super(message, HttpStatus.PRECONDITION_FAILED);
    }

    /**
     * Creates a StockDataInconsistencyException for supplier stock availability issues.
     */
    public static StockDataInconsistencyException forSupplier(Long supplierId) {
        String message = String.format("Stock information unavailable for supplier %d - data consistency issue", supplierId);
        return new StockDataInconsistencyException(message);
    }

    @Override
    protected void logException() {
        log.error("Stock data inconsistency: {}", getMessage());
    }
}