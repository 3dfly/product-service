package repository.projection;

/**
 * Projection interface for supplier data with calculated distance.
 * Spring Data JPA will automatically map query results to this interface.
 */
public interface SupplierWithDistanceProjection {
    Long getId();
    Long getUserId();
    String getName();
    String getEmail();
    String getPhone();
    String getAddress();
    String getCity();
    String getState();
    String getCountry();
    String getPostalCode();
    Double getLatitude();
    Double getLongitude();
    String getBusinessLicense();
    String getDescription();
    Boolean getVerified();
    Boolean getActive();
    Double getDistanceKm();
}