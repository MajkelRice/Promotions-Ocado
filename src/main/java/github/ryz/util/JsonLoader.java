package github.ryz.util;

import github.ryz.model.Order;
import github.ryz.model.PaymentMethod;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.File;
import java.io.IOException;
import java.util.List;

public class JsonLoader {
    private static final ObjectMapper mapper = new ObjectMapper();

    public static List<Order> loadOrders(String filePath) throws IOException {
        return mapper.readValue(new File(filePath), new TypeReference<List<Order>>() {});
    }

    public static List<PaymentMethod> loadPaymentMethods(String filePath) throws IOException {
        return mapper.readValue(new File(filePath), new TypeReference<List<PaymentMethod>>() {});
    }
}