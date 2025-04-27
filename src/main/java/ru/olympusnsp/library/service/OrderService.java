package ru.olympusnsp.library.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import ru.olympusnsp.library.dto.OrderBookChangeRequest;
import ru.olympusnsp.library.dto.OrderCreate;
import ru.olympusnsp.library.model.Order;
import ru.olympusnsp.library.model.OrderBook;

public interface OrderService {

    Page<Order> findAll(Pageable pageable);
    Order findById(Long id);

    Order createNewOrder(OrderCreate orderCreate);
     OrderBook changeOrderBook(OrderBookChangeRequest change);
}
