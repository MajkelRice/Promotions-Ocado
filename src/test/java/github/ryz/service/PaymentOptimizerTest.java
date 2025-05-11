package github.ryz.service;

import github.ryz.model.Order;
import github.ryz.model.PaymentMethod;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class PaymentOptimizerTest {

    @Test
    void testSingleOrderWithFullPointsPayment() {
        // Given
        List<Order> orders = List.of(
                new Order("ORDER1", new BigDecimal("100.00"), null)
        );

        List<PaymentMethod> methods = List.of(
                new PaymentMethod("PUNKTY", 15, new BigDecimal("200.00"))
        );

        // When
        Map<String, BigDecimal> result = PaymentOptimizer.optimize(orders, methods);

        // Then
        assertEquals(1, result.size());
        assertEquals(new BigDecimal("85.00"), result.get("PUNKTY")); // 100 - 15%
    }

    @Test
    void testSingleOrderWithCardPromotion() {
        // Given
        List<Order> orders = List.of(
                new Order("ORDER1", new BigDecimal("200.00"), List.of("mZysk"))
        );

        List<PaymentMethod> methods = List.of(
                new PaymentMethod("mZysk", 10, new BigDecimal("200.00")),
                new PaymentMethod("PUNKTY", 15, new BigDecimal("50.00"))
        );

        // When
        Map<String, BigDecimal> result = PaymentOptimizer.optimize(orders, methods);

        // Then
        assertEquals(1, result.size());
        assertEquals(new BigDecimal("180.00"), result.get("mZysk")); // 200 - 10%
    }

    @Test
    void testPartialPointsPayment() {
        // Given
        List<Order> orders = List.of(
                new Order("ORDER1", new BigDecimal("100.00"), null)
        );

        List<PaymentMethod> methods = List.of(
                new PaymentMethod("PUNKTY", 15, new BigDecimal("10.00")), // Only enough for 10%
                new PaymentMethod("VISA", 0, new BigDecimal("1000.00"))
        );

        // When
        Map<String, BigDecimal> result = PaymentOptimizer.optimize(orders, methods);

        // Then
        assertEquals(2, result.size());
        assertEquals(new BigDecimal("10.00"), result.get("PUNKTY")); // 10% of 100
        assertEquals(new BigDecimal("80.00"), result.get("VISA")); // 100 - 10% discount = 90, minus 10 points
    }

    @Test
    void testMultipleOrdersOptimalDistribution() {
        // Given
        List<Order> orders = List.of(
                new Order("ORDER1", new BigDecimal("100.00"), List.of("mZysk")),
                new Order("ORDER2", new BigDecimal("200.00"), List.of("BosBankrut")),
                new Order("ORDER3", new BigDecimal("150.00"), List.of("mZysk", "BosBankrut")),
                new Order("ORDER4", new BigDecimal("50.00"), null)
        );

        List<PaymentMethod> methods = List.of(
                new PaymentMethod("PUNKTY", 15, new BigDecimal("100.00")),
                new PaymentMethod("mZysk", 10, new BigDecimal("180.00")),
                new PaymentMethod("BosBankrut", 5, new BigDecimal("200.00"))
        );

        // When
        Map<String, BigDecimal> result = PaymentOptimizer.optimize(orders, methods);

        // Then
        assertEquals(3, result.size());
        assertEquals(new BigDecimal("100.00"), result.get("PUNKTY")); // Full payment for ORDER1
        assertEquals(new BigDecimal("165.00"), result.get("mZysk")); // ORDER3 (150 - 10% = 135) + ORDER4 partial (30)
        assertEquals(new BigDecimal("190.00"), result.get("BosBankrut")); // ORDER2 (200 - 5% = 190)
    }

    @Test
    void testNoPromotionsAvailable() {
        // Given
        List<Order> orders = List.of(
                new Order("ORDER1", new BigDecimal("100.00"), null)
        );

        List<PaymentMethod> methods = List.of(
                new PaymentMethod("VISA", 0, new BigDecimal("200.00"))
        );

        // When
        Map<String, BigDecimal> result = PaymentOptimizer.optimize(orders, methods);

        // Then
        assertEquals(1, result.size());
        assertEquals(new BigDecimal("100.00"), result.get("VISA"));
    }

    @Test
    void testInsufficientFunds() {
        // Given
        List<Order> orders = List.of(
                new Order("ORDER1", new BigDecimal("100.00"), null)
        );

        List<PaymentMethod> methods = List.of(
                new PaymentMethod("PUNKTY", 15, new BigDecimal("10.00")),
                new PaymentMethod("VISA", 0, new BigDecimal("50.00"))
        );

        // When
        Map<String, BigDecimal> result = PaymentOptimizer.optimize(orders, methods);

        // Then
        assertTrue(result.isEmpty()); // No method can cover the full amount
    }

    @Test
    void testEdgeCaseSmallOrder() {
        // Given
        List<Order> orders = List.of(
                new Order("ORDER1", new BigDecimal("10.00"), null)
        );

        List<PaymentMethod> methods = List.of(
                new PaymentMethod("PUNKTY", 15, new BigDecimal("1.00")), // Only enough for 10%
                new PaymentMethod("VISA", 0, new BigDecimal("100.00"))
        );

        // When
        Map<String, BigDecimal> result = PaymentOptimizer.optimize(orders, methods);

        // Then
        assertEquals(2, result.size());
        assertEquals(new BigDecimal("1.00"), result.get("PUNKTY")); // 10% of 10
        assertEquals(new BigDecimal("8.00"), result.get("VISA")); // 10 - 10% = 9, minus 1 point
    }

    @Test
    void testPreferPointsOverCards() {
        // Given - Both options give same discount, but should prefer points
        List<Order> orders = List.of(
                new Order("ORDER1", new BigDecimal("100.00"), List.of("mZysk"))
        );

        List<PaymentMethod> methods = List.of(
                new PaymentMethod("PUNKTY", 10, new BigDecimal("200.00")),
                new PaymentMethod("mZysk", 10, new BigDecimal("200.00"))
        );

        // When
        Map<String, BigDecimal> result = PaymentOptimizer.optimize(orders, methods);

        // Then
        assertEquals(1, result.size());
        assertEquals(new BigDecimal("90.00"), result.get("PUNKTY")); // Preferred over mZysk
    }
}