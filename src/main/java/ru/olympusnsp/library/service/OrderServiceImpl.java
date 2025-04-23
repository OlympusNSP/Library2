package ru.olympusnsp.library.service;

import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import ru.olympusnsp.library.controller.BookController;
import ru.olympusnsp.library.dto.OrderCreate;
import ru.olympusnsp.library.dto.OrderInfo;
import ru.olympusnsp.library.exeption.*;
import ru.olympusnsp.library.model.Book;
import ru.olympusnsp.library.model.Order;
import ru.olympusnsp.library.model.RentalBook;
import ru.olympusnsp.library.model.User;
import ru.olympusnsp.library.repository.OrderRepository;
import ru.olympusnsp.library.repository.UserRepository;

import java.time.LocalDate;
import java.util.*;

@Service
public class OrderServiceImpl implements OrderService {

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private UserService userService;

    @Autowired
    private BookService bookService;

    @Value("${setting.order.max-books-in-order}")
    private Integer maxBooksInOrder;

    @Value("${setting.order.max-rental-books}")
    private Integer maxRentalBooks;

    Logger logger = LoggerFactory.getLogger(OrderServiceImpl.class);


    /**
     * Получить страничную выдачу всех заказов
     * @param pageable страница
     * @return заказы, страничная выдача
     */
    public Page<Order> findAll(Pageable pageable){
        return orderRepository.findAll(pageable);
    }


    /**
     *  Поиск заказа по инентификтору
     * @param id идентификатор
     * @return заказ
     */
    @Transactional
    public Order findById(Long id){
        return orderRepository.findById(id).orElseThrow(() -> new NotFoundEntity("Order with id " + id.toString() + " not found"));
    }

    /**
     * Создание нового заказа, с резерированием книг
     *
     * @param orderCreate orderDTO
     * @return созданный заказ
     */

    @Transactional
    public Order createNewOrder(OrderCreate orderCreate) {
        logger.info("Create new order {}", orderCreate);
        var user = userService.findById(orderCreate.getUser_id());
        if (user==null){
            throw new NotFoundUser("User not found");
        }
        if (user.getStatusBlock()){
            throw new UserBlockExeption("Пользователь заблокирован");
        }
        var book_ids = orderCreate.getBook_ids();
        if(book_ids.size() > maxBooksInOrder){
            logger.warn("Превышение количества книг в заказе");
            throw new BookCountExcessException("Превышение количества книг в заказе");
        }
        Integer maxAvailableBookForRental = maxRentalBooks - user.getBookRented();
        if(book_ids.size() > maxAvailableBookForRental){
            logger.warn("Превышение доступного количества выдаваемых книг для аккаунта");
            throw new BookCountExcessException("Превышение доступного количества выдаваемых книг для аккаунта");
        }
        var listBook = new ArrayList<Book>();
        for(Integer book_id : book_ids){
            Boolean result = bookService.reserveBookById(book_id);
            var book = bookService.findById(book_id);
            listBook.add(book);
            if(!result){
                logger.warn("Закончилась книга с id={}",book_id);
                throw new BookUnavailableException("Книга с id= "+book_id.toString()+" недоступна");
            }
        }

        Order order = new Order();
        order.setUser(user);
        order.setStatus(Order.OrderStatus.READY);
        order.setReservedBooks(listBook);
        logger.warn("Сохранение заказа");

        return orderRepository.save(order);
    }

    /**
     * Выдача заказа (книг) в библиотеке пользователю
     *
     * @param orderId идентификатор заказа
     * @return
     */
    @Transactional
    public OrderInfo startRentalOrder(Long orderId){
        var oOrder = orderRepository.findById(orderId);
        if(oOrder.isEmpty()){
            logger.warn("Заказ с номером "+orderId.toString()+" не найден");
            throw new NotFoundEntity("Заказ с номером "+orderId.toString()+" не найден");
        }
        var order = oOrder.get();
        var listRental = new HashSet<RentalBook>();
        //order.setManager(userService.getCurrentUser());// TODO не реализовано
        var user = order.getUser();
        for (Book b:order.getReservedBooks()){
            b.setReserve(b.getReserve()-1);
            user.setBookRented(user.getBookRented()+1);
            logger.info("Обработка книги {} в заказе",b.getId());
            b= bookService.save(b);
            var rentalBook = RentalBook.builder().book(b).dateRentedStart(LocalDate.now()).user(user).build();
            listRental.add(rentalBook);
        };
        user.setRentalBooks(listRental);
        logger.warn(listRental.toString());
        userService.save(user);
        order.setStatus(Order.OrderStatus.COMPLETED);
        logger.info("Сохранение заказа с номером {}",orderId);
        orderRepository.save(order);
        return OrderInfo.from(order);
    }
}
