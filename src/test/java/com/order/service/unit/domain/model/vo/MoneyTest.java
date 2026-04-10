package com.order.service.unit.domain.model.vo;

import com.order.service.domain.exception.InvalidOrderException;
import com.order.service.domain.model.vo.Money;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

class MoneyTest {

    @Test
    @DisplayName("Should create Money with valid positive amount")
    void shouldCreateMoneyWithValidPositiveAmount() {
        // Act
        var money = Money.of(100.00);

        // Assert
        assertEquals(0, money.getAmount().compareTo(BigDecimal.valueOf(100.00)));
    }

    @Test
    @DisplayName("Should throw exception when creating Money with negative amount")
    void shouldThrowExceptionWhenCreatingMoneyWithNegativeAmount() {
        // Act & Assert
        assertThrows(InvalidOrderException.class, () -> Money.of(-100.00));
    }

    @Test
    @DisplayName("Should add two Money values correctly")
    void shouldAddTwoMoneyValuesCorrectly() {
        // Arrange
        var money1 = Money.of(100.00);
        var money2 = Money.of(50.00);

        // Act
        var result = money1.add(money2);

        // Assert
        assertEquals(0, result.getAmount().compareTo(BigDecimal.valueOf(150.00)));
    }

    @Test
    @DisplayName("Should multiply Money by quantity correctly")
    void shouldMultiplyMoneyByQuantityCorrectly() {
        // Arrange
        var money = Money.of(50.00);

        // Act
        var result = money.multiply(3);

        // Assert
        assertEquals(0, result.getAmount().compareTo(BigDecimal.valueOf(150.00)));
    }

    @Test
    @DisplayName("Should create zero Money")
    void shouldCreateZeroMoney() {
        // Act
        var money = Money.zero();

        // Assert
        assertEquals(0, money.getAmount().compareTo(BigDecimal.ZERO));
    }

    @Test
    @DisplayName("Should compare two equal Money values")
    void shouldCompareTwoEqualMoneyValues() {
        // Arrange
        var money1 = Money.of(100.00);
        var money2 = Money.of(100.00);

        // Assert
        assertEquals(money1, money2);
    }
}
