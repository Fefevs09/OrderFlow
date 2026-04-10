package com.order.service.domain.model;

import com.order.service.domain.model.vo.Address;
import com.order.service.domain.model.vo.Email;
import lombok.Getter;

import java.util.Objects;
import java.util.UUID;

@Getter
public class Customer {
    private final String id;
    private final String name;
    private final Email email;
    private final Address address;

    public Customer(String id, String name, Email email, Address address) {
        this.id = id != null ? id : UUID.randomUUID().toString();
        this.name = Objects.requireNonNull(name, "Customer name cannot be null");
        this.email = Objects.requireNonNull(email, "Customer email cannot be null");
        this.address = Objects.requireNonNull(address, "Customer address cannot be null");
    }

    public static Customer create(String name, Email email, Address address) {
        return new Customer(null, name, email, address);
    }

    public static Customer of(String id, String name, Email email, Address address) {
        return new Customer(id, name, email, address);
    }

    public String getEmailValue() {
        return email.getValue();
    }
}
