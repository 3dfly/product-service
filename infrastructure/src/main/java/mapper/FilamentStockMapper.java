package mapper;

import dto.FilamentStockRequest;
import dto.FilamentStockResponse;
import entity.FilamentStock;
import entity.Supplier;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import repository.projection.ClosetSupplierProjection;

@Component
public class FilamentStockMapper {

    private final ModelMapper modelMapper;

    @Autowired
    public FilamentStockMapper(ModelMapper modelMapper) {
        this.modelMapper = modelMapper;
    }

    public FilamentStockResponse toResponse(FilamentStock filamentStock) {
        if (filamentStock == null) {
            return null;
        }

        FilamentStockResponse response = modelMapper.map(filamentStock, FilamentStockResponse.class);
        // Custom logic for supplier-related fields
        if (filamentStock.getSupplier() != null) {
            response.setSupplierId(filamentStock.getSupplier().getId());
            response.setSupplierName(filamentStock.getSupplier().getName());
        }
        
        return response;
    }

    public FilamentStock toEntity(FilamentStockRequest request) {
        if (request == null) {
            return null;
        }

        FilamentStock filamentStock = FilamentStock.builder()
                .materialType(request.getMaterialType())
                .color(request.getColor())
                .quantityKg(request.getQuantityKg())
                .reservedKg(request.getReservedKg())
                .available(request.isAvailable())
                .lastRestocked(request.getLastRestocked())
                .expiryDate(request.getExpiryDate())
                .build();

        // Supplier will be set by the service layer
        return filamentStock;
    }

    public void updateEntityFromRequest(FilamentStock filamentStock, FilamentStockRequest request) {
        if (filamentStock == null || request == null) {
            return;
        }

        modelMapper.map(request, filamentStock);
        // Supplier will be set by the service layer
    }

    /**
     * Converts a SupplierWithStockProjection to a FilamentStock entity.
     * Used with optimized queries that fetch both supplier and stock data.
     */
    public FilamentStock fromStockProjection(ClosetSupplierProjection projection, Supplier supplier) {
        if (projection == null) {
            return null;
        }

        return FilamentStock.builder()
                .id(projection.getStockId())
                .supplier(supplier)
                .materialType(projection.getMaterialType())
                .color(projection.getColor())
                .quantityKg(projection.getQuantityKg())
                .reservedKg(projection.getReservedKg())
                .available(projection.getAvailable())
                .build();
    }
}