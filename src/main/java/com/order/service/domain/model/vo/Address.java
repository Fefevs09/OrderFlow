package com.order.service.domain.model.vo;

import com.order.service.domain.exception.InvalidOrderException;

import java.util.Objects;

public final class Address {
    private final String street;
    private final String city;
    private final String state;
    private final String zipCode;
    private final String country;

    private Address(String street, String city, String state, String zipCode, String country) {
        if (street == null || street.isBlank()) {
            throw new InvalidOrderException("Street cannot be null or blank");
        }
        if (city == null || city.isBlank()) {
            throw new InvalidOrderException("City cannot be null or blank");
        }
        if (zipCode == null || zipCode.isBlank()) {
            throw new InvalidOrderException("Zip code cannot be null or blank");
        }
        this.street = street;
        this.city = city;
        this.state = state;
        this.zipCode = zipCode;
        this.country = country != null ? country : "BR";
    }

    public static Address of(String street, String city, String state, String zipCode, String country) {
        return new Address(street, city, state, zipCode, country);
    }

    public static Address of(String street, String city, String state, String zipCode) {
        return new Address(street, city, state, zipCode, "BR");
    }

    public String getStreet() {
        return street;
    }

    public String getCity() {
        return city;
    }

    public String getState() {
        return state;
    }

    public String getZipCode() {
        return zipCode;
    }

    public String getCountry() {
        return country;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Address address = (Address) o;
        return Objects.equals(street, address.street) &&
                Objects.equals(city, address.city) &&
                Objects.equals(state, address.state) &&
                Objects.equals(zipCode, address.zipCode) &&
                Objects.equals(country, address.country);
    }

    @Override
    public int hashCode() {
        return Objects.hash(street, city, state, zipCode, country);
    }

    @Override
    public String toString() {
        return String.format("%s, %s, %s %s, %s", street, city, state, zipCode, country);
    }
}
