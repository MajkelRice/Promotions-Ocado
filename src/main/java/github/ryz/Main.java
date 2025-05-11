package github.ryz;

import github.ryz.model.Order;
import github.ryz.model.PaymentMethod;
import github.ryz.util.JsonLoader;
import github.ryz.service.PaymentOptimizer;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Map;

public class Main {
    public static void main(String[] args) {
        if (args.length != 2) {
            System.err.println("Usage: java -jar app.jar <orders.json> <paymentmethods.json>");
            System.exit(1);
        }

        try {
            String ordersPath = args[0];
            String methodsPath = args[1];

            List<Order> orders = JsonLoader.loadOrders(ordersPath);
            List<PaymentMethod> methods = JsonLoader.loadPaymentMethods(methodsPath);

            Map<String, BigDecimal> result = PaymentOptimizer.optimize(orders, methods);
            result.forEach((method, amount) ->
                    System.out.println(method + " " + amount.setScale(2, RoundingMode.HALF_UP)));

        } catch (IOException e) {
            System.err.println("Error loading JSON files: " + e.getMessage());
            System.exit(1);
        }
    }
}
