package ru.olympusnsp.library.controller;

import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import ru.olympusnsp.library.dto.OrderBookChangeRequest;
import ru.olympusnsp.library.dto.OrderBookStatusDTO;
import ru.olympusnsp.library.dto.OrderCreate;
import ru.olympusnsp.library.exeption.UserIdInRequestAndUserDetailDifferentException;
import ru.olympusnsp.library.model.Order;
import ru.olympusnsp.library.model.OrderBook;
import ru.olympusnsp.library.model.User;
import ru.olympusnsp.library.service.OrderService;
import ru.olympusnsp.library.service.UserService;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/order")
@Tag(name="Заказы")
public class OrderController {

    @Autowired
    private OrderService orderService;

    @Autowired
    private UserService userService;

    Logger logger = LoggerFactory.getLogger(OrderController.class);

    @PostMapping("")
    Order newOrder(@RequestBody @Valid OrderCreate orderCreate, @AuthenticationPrincipal UserDetails userDetails){
        User user = userService.getByUsername(userDetails.getUsername());
        if (!orderCreate.getUser_id().equals(user.getId()))
        {
            throw new UserIdInRequestAndUserDetailDifferentException("Пользователь в запросе не совпадает с аутентифицированным");
        }
       return orderService.createNewOrder(orderCreate);
    }

    @GetMapping("/{id}")
    Order findById(@PathVariable Long id){
        return orderService.findById(id);
    }


    //@PreAuthorize("hasRole('MANAGER')")
    @PutMapping(value = "/orderbook/{id}")
    OrderBook changeOrder(@PathVariable Long id, @RequestBody @Valid OrderBookStatusDTO orderBookStatus) {
        var request = new OrderBookChangeRequest(id,orderBookStatus.getStatus());
        return orderService.changeOrderBook(request);

    }

}
