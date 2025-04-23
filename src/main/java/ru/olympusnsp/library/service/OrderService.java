package ru.olympusnsp.library.service;

import jakarta.transaction.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import ru.olympusnsp.library.dto.OrderCreate;
import ru.olympusnsp.library.dto.OrderInfo;
import ru.olympusnsp.library.exeption.BookCountExcessException;
import ru.olympusnsp.library.exeption.BookUnavailableException;
import ru.olympusnsp.library.exeption.NotFoundUser;
import ru.olympusnsp.library.model.Order;
import ru.olympusnsp.library.repository.OrderRepository;

import java.util.List;

public interface OrderService {

    Page<Order> findAll(Pageable pageable);
    Order findById(Long id);

    Order createNewOrder(OrderCreate orderCreate);
    OrderInfo startRentalOrder(Long orderId);

}
