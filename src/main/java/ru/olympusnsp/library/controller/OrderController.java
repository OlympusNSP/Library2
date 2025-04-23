package ru.olympusnsp.library.controller;

import io.swagger.v3.oas.annotations.Operation;
import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.http.RequestEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import ru.olympusnsp.library.dto.OrderCreate;
import ru.olympusnsp.library.dto.OrderInfo;
import ru.olympusnsp.library.model.Author;
import ru.olympusnsp.library.model.Order;
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
    Order newOrder(@RequestBody OrderCreate orderCreate, @AuthenticationPrincipal UserDetails userDetails){
        logger.info("Create new order");
        User user = userService.getByUsername(userDetails.getUsername());
        if (!orderCreate.getUser_id().equals(user.getId()))
        {
            throw new RuntimeException("User id orderCreate  does not match with userdetail");
        }
       return orderService.createNewOrder(orderCreate);
    }

    @GetMapping("/{id}")
    Order findById(@PathVariable Long id){
        logger.info("Find order by id={}",id);
        return orderService.findById(id);
    }


    @PostMapping(value = "/{id}/start")
    OrderInfo startRentalOrder(@PathVariable Long id) {
        return orderService.startRentalOrder(id);

    }

}
