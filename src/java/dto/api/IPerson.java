package dto.api;

public interface IPerson {
    String getName();
    String getSurName();
    IAddress getAddress();
    IAddress [] getOtherAddresses();
    String [] getAliases();
}

