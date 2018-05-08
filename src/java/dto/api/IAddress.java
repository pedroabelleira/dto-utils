package dto.api;

public interface IAddress {
    String getStreet();
    String getNumber();

    default String getFullAddress() {
        return getStreet() + " "  + getNumber();
    }
}

