package exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;

/**
 * Custom exception for when no supplier is found with sufficient stock.
 */
@Slf4j
public class SupplierNotFoundException extends BaseException {
    private SupplierNotFoundException(String message) {
        super(message, HttpStatus.NOT_FOUND);
    }

    /**
     * Creates a SupplierNotFoundException with a formatted message for material requirements.
     */
    public static SupplierNotFoundException forMaterialRequirement(String materialType, String color, Double requiredQuantity) {
        String message = String.format("No supplier found with sufficient stock for %s %s (required: %.1f kg)", 
                materialType, color, requiredQuantity);
        return new SupplierNotFoundException(message);
    }

    @Override
    protected void logException() {
        log.warn("Supplier not found: {}", getMessage());
    }
}