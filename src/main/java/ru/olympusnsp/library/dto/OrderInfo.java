package ru.olympusnsp.library.dto;

import lombok.*;
import ru.olympusnsp.library.model.Order;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderInfo {
    private Long orderId;
    private Order.OrderStatus status;

    public static OrderInfo from(Order order) {
        OrderInfo info = new OrderInfo();
        info.orderId = order.getId();
        info.status = order.getStatus();
        return info;
    }
}
