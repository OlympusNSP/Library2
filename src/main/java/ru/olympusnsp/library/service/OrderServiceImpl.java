package ru.olympusnsp.library.service;

import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import ru.olympusnsp.library.dto.OrderBookChangeRequest;
import ru.olympusnsp.library.dto.OrderCreate;
import ru.olympusnsp.library.exeption.*;
import ru.olympusnsp.library.model.Order;
import ru.olympusnsp.library.model.OrderBook;
import ru.olympusnsp.library.repository.OrderBookRepository;
import ru.olympusnsp.library.repository.OrderRepository;

import java.time.LocalDate;
import java.util.*;


@Service
public class OrderServiceImpl implements OrderService {

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private OrderBookRepository orderBookRepository;

    @Autowired
    private UserService userService;

    @Autowired
    private BookService bookService;

    @Value("${setting.order.max-books-in-order}")
    private Integer maxBooksInOrder;

    @Value("${setting.order.max-rental-books}")
    private Integer maxRentalBooks;

    @Value("${setting.max-days-rental}")
    private Integer daysRentalBooks;

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
        return orderRepository.findById(id).orElseThrow(() -> new NotFoundEntity("Order with id " + id + " not found"));
    }

    /**
     * Создание нового заказа, уменьшаем количество доступных книг
     *
     * @param orderCreate orderDTO
     * @return созданный заказ
     */

    @Transactional
    public Order createNewOrder(OrderCreate orderCreate) {
        logger.info("Create new order {}", orderCreate);
        var user = userService.findById(orderCreate.getUser_id());
        if (user==null){
            throw new NotFoundUser("Пользователь не найден");
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
        var setOrderBook = new HashSet<OrderBook>();
        Order order = new Order();
        order.setUser(user);
        order.setCreatedData(LocalDate.now());
        order = orderRepository.save(order);

        for(Integer bookId : book_ids){
            var book = bookService.findById(bookId);
            if(book.getAvailable()<=0){
                logger.warn("Закончилась книга с id={}",bookId);
                throw new BookUnavailableException("Книга с id= "+bookId.toString()+" недоступна");
            }
            book.setAvailable(book.getAvailable()-1) ;
            OrderBook orderBook = new OrderBook();
            orderBook.setBook(book);
            orderBook.setOrder(order);
            orderBook.setStatus(OrderBook.OrderBookStatus.CREATED);
            setOrderBook.add(orderBook);
            //orderBookRepository.save(orderBook);
        }
        order.setOrderBooks(setOrderBook);
        return orderRepository.findById(order.getId()).orElse(null);
    }

    /**
     * Изменяем состояние заказанных книг (OrderBook)
     *
     * @param change DTO для изменения заказа (orderBookID и status)
     * @return возвращается состояние заказанной книги
     */
    @Transactional
    public OrderBook changeOrderBook(OrderBookChangeRequest change) {

        var oOrderBook = orderBookRepository.findById(change.getOrderBookId());
        if (oOrderBook.isPresent()) {
            var orderBook = oOrderBook.get();
            logger.debug("Изменение состояния OrderBook с id {} с {} на {}",change.getOrderBookId(),orderBook.getStatus(),change.getStatus());
            var newStatus = change.getStatus();
            var oldStatus = orderBook.getStatus();
            if (newStatus.equals(oldStatus))
                return orderBook;
            if (newStatus == OrderBook.OrderBookStatus.PREPARED && oldStatus == OrderBook.OrderBookStatus.CREATED) {
                logger.debug("Книга найдена в библиотеке и отложена в резерв (для созданного заказа)");
                orderBook.setStatus(OrderBook.OrderBookStatus.PREPARED);
                var book = orderBook.getBook();
                book.setReserve(book.getReserve() + 1);
                orderBook.setBook(book);
                return orderBookRepository.save(orderBook);
            }
            else if (newStatus == OrderBook.OrderBookStatus.LOSSLIBRARY && oldStatus == OrderBook.OrderBookStatus.CREATED) {
                logger.debug("Книга НЕ найдена в библиотеке, объявляется потерянной (для созданного заказа)");
                orderBook.setStatus(OrderBook.OrderBookStatus.LOSSLIBRARY);
                var book = orderBook.getBook();
                book.setAvailable(book.getCount() - 1);
                orderBook.setBook(book);
                return orderBookRepository.save(orderBook);
            }
            else if (newStatus == OrderBook.OrderBookStatus.RENTED && oldStatus == OrderBook.OrderBookStatus.PREPARED) {
                logger.debug("Выдача заказа для подготовленных (зарезервированных книг)");
                orderBook.setStatus(OrderBook.OrderBookStatus.RENTED);
                orderBook.setDateStartRentedBook(LocalDate.now());
                var returnUpTo = LocalDate.now().plusDays(daysRentalBooks);
                orderBook.setDateReturnUpto(returnUpTo);
                var book = orderBook.getBook();
                logger.debug("Выдача книги id {} до {}",book.getId(), returnUpTo);
                book.setReserve(book.getReserve() - 1);
                orderBook.setBook(book);
                return orderBookRepository.save(orderBook);
            }
            else if (newStatus == OrderBook.OrderBookStatus.RETURNED && oldStatus == OrderBook.OrderBookStatus.RENTED) {
                logger.debug("Возврат арендованной книги");
                orderBook.setStatus(OrderBook.OrderBookStatus.RETURNED);
                var now = LocalDate.now();
                orderBook.setDateReturnedBook(now);
                if (now.isAfter(orderBook.getDateReturnUpto())) {
                    var user_id = orderBook.getOrder().getUser().getId();
                    logger.info("Пользователь id = {} просрочил возврат книги",user_id);
                    userService.addViolation(user_id);
                }
                var book = orderBook.getBook();
                book.setAvailable(book.getAvailable() + 1);
                orderBook.setBook(book);
                return orderBookRepository.save(orderBook);
            }
            //
            else if (newStatus == OrderBook.OrderBookStatus.LOSSUSER && oldStatus == OrderBook.OrderBookStatus.RENTED){
                logger.debug("Пользователь потерял книгу (для арендованных книг)");
                orderBook.setStatus(OrderBook.OrderBookStatus.LOSSUSER);
                var user = orderBook.getOrder().getUser();
                userService.addViolation(user.getId());
                var book = orderBook.getBook();
                book.setAvailable(book.getCount() - 1);
                orderBook.setBook(book);
                return orderBookRepository.save(orderBook);
            }
            // Отмена
            else if (newStatus == OrderBook.OrderBookStatus.CANCELLED) {
                switch (oldStatus) {
                    case PREPARED:
                        logger.debug("Отмена из подготовленного состояния, возврат книги из резерва в доступные");
                        orderBook.setStatus(OrderBook.OrderBookStatus.CANCELLED);
                        var book = orderBook.getBook();
                        book.setReserve(book.getReserve() - 1);
                        book.setAvailable(book.getAvailable() + 1);
                        orderBook.setBook(book);
                        return orderBookRepository.save(orderBook);
                    case RENTED:
                        logger.info("Запрещено отменять ареднованный заказ, используйте возврат или потерю");
                        throw new OrderBookStatusException("Используйте статус RETURNED или LOSSUSER");
                    case CREATED:
                        logger.debug("Отмена только созданного заказа, книга добавляется в доступные");
                        var book2 = orderBook.getBook();
                        book2.setAvailable(book2.getAvailable() + 1);
                        orderBook.setBook(book2);
                        return orderBookRepository.save(orderBook);
                    default:
                        logger.info("Отмена из данного состояния невозмозжна");
                        throw new OrderBookStatusException("Отмена из данного состояния невозможна");
                }
            }
            throw new OrderBookStatusException("Неподдерживаемое изменение статуса с "+change.getStatus()+" на "+orderBook.getStatus()+")");
        }
        else{
            throw new NotFoundEntity("OrderBook с id = "+change.getOrderBookId()+" не найден");
        }
    }
}
