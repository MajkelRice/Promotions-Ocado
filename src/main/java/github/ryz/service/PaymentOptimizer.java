package github.ryz.service;

import github.ryz.model.Order;
import github.ryz.model.PaymentMethod;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;
import java.util.stream.Collectors;
public class PaymentOptimizer {

    public static Map<String, BigDecimal> optimize(List<Order> orders, List<PaymentMethod> methods) {
        Map<String, BigDecimal> usage = new HashMap<>();
        Map<String, PaymentMethod> methodMap = methods.stream()
                .collect(Collectors.toMap(PaymentMethod::getId,
                        m -> new PaymentMethod(m.getId(), m.getDiscount(), m.getLimit())));

        PaymentMethod points = methodMap.get("PUNKTY");

        for (Order order : orders) {
            BigDecimal value = order.getValue();
            String bestMethod = null;
            boolean fullPoints = false;
            boolean partialPoints = false;
            BigDecimal bestCost = value;
            BigDecimal pointsUsed = BigDecimal.ZERO;
            BigDecimal cardCharge = BigDecimal.ZERO;

            // 1. Full points (preferred when discount is equal)
            if (points != null) {
                BigDecimal discountFactor = BigDecimal.valueOf(100 - points.getDiscount()).divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
                BigDecimal cost = value.multiply(discountFactor).setScale(2, RoundingMode.HALF_UP);

                if (points.getLimit().compareTo(cost) >= 0 && cost.compareTo(bestCost) <= 0) {
                    bestCost = cost;
                    bestMethod = "PUNKTY";
                    fullPoints = true;
                    pointsUsed = cost;
                }
            }

            // 2. Full card promos
            if (order.getPromotions() != null) {
                for (String promo : order.getPromotions()) {
                    PaymentMethod pm = methodMap.get(promo);
                    if (pm == null) continue;

                    BigDecimal discountFactor = BigDecimal.valueOf(100 - pm.getDiscount()).divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
                    BigDecimal cost = value.multiply(discountFactor).setScale(2, RoundingMode.HALF_UP);

                    if (pm.getLimit().compareTo(cost) >= 0 && cost.compareTo(bestCost) < 0) {
                        bestCost = cost;
                        bestMethod = promo;
                        fullPoints = false;
                    }
                }
            }

            // 3. Partial points (at least 10% of original value)
            if (points != null) {
                BigDecimal discountedTotal = value.multiply(BigDecimal.valueOf(0.9)).setScale(2, RoundingMode.HALF_UP);
                BigDecimal minPoints = value.multiply(BigDecimal.valueOf(0.1)).setScale(2, RoundingMode.HALF_UP);
                BigDecimal maxPossiblePoints = points.getLimit().min(discountedTotal);

                if (maxPossiblePoints.compareTo(minPoints) >= 0) {
                    BigDecimal cCharge = discountedTotal.subtract(maxPossiblePoints);

                    Optional<PaymentMethod> fallback = methodMap.values().stream()
                            .filter(m -> !m.getId().equals("PUNKTY") && m.getLimit().compareTo(cCharge) >= 0)
                            .findFirst();

                    if (fallback.isPresent() && discountedTotal.compareTo(bestCost) < 0) {
                        bestCost = discountedTotal;
                        bestMethod = fallback.get().getId();
                        fullPoints = false;
                        partialPoints = true;
                        pointsUsed = maxPossiblePoints;
                        cardCharge = cCharge;
                    }
                }
            }

            //apply the best method
            if (bestMethod != null) {
                if (fullPoints) {
                    usage.merge("PUNKTY", pointsUsed, BigDecimal::add);
                    points.setLimit(points.getLimit().subtract(pointsUsed));
                } else if (partialPoints) {
                    usage.merge("PUNKTY", pointsUsed, BigDecimal::add);
                    points.setLimit(points.getLimit().subtract(pointsUsed));

                    usage.merge(bestMethod, cardCharge, BigDecimal::add);
                    PaymentMethod card = methodMap.get(bestMethod);
                    card.setLimit(card.getLimit().subtract(cardCharge));
                } else {
                    usage.merge(bestMethod, bestCost, BigDecimal::add);
                    PaymentMethod card = methodMap.get(bestMethod);
                    card.setLimit(card.getLimit().subtract(bestCost));
                }
            } else {
                // no promotions, pay full, with any available method
                if (points != null && points.getLimit().compareTo(value) >= 0) {
                    usage.merge("PUNKTY", value, BigDecimal::add);
                    points.setLimit(points.getLimit().subtract(value));
                } else {
                    Optional<PaymentMethod> fallback = methodMap.values().stream()
                            .filter(m -> !m.getId().equals("PUNKTY") && m.getLimit().compareTo(value) >= 0)
                            .findFirst();
                    if (fallback.isPresent()) {
                        usage.merge(fallback.get().getId(), value, BigDecimal::add);
                        fallback.get().setLimit(fallback.get().getLimit().subtract(value));
                    }
                }
            }
        }

        return usage;
    }
}