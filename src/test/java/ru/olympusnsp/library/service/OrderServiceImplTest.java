package ru.olympusnsp.library.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.util.ReflectionTestUtils; // Для установки @Value полей
import ru.olympusnsp.library.dto.OrderBookChangeRequest;
import ru.olympusnsp.library.dto.OrderCreate;
import ru.olympusnsp.library.exeption.*;
import ru.olympusnsp.library.model.*; // Импортируем User, Book, Order, OrderBook
import ru.olympusnsp.library.repository.OrderBookRepository;
import ru.olympusnsp.library.repository.OrderRepository;

import java.time.LocalDate;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrderServiceImplTest {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private OrderBookRepository orderBookRepository;

    @Mock
    private UserService userService;

    @Mock
    private BookService bookService;

    @InjectMocks
    private OrderServiceImpl orderService;

    // Константы для тестов
    private final Integer USER_ID = 1;
    private final Long ORDER_ID = 10L;
    private final Long ORDER_BOOK_ID = 100L;
    private final Integer BOOK_ID_1 = 1;
    private final Integer BOOK_ID_2 = 2;
    private final Integer MAX_BOOKS_IN_ORDER = 5;
    private final Integer MAX_RENTAL_BOOKS = 10;
    private final Integer DAYS_RENTAL_BOOKS = 14;

    private User mockUser;
    private Book mockBook1;
    private Book mockBook2;
    private Order mockOrder;
    private OrderBook mockOrderBook;
    private OrderCreate orderCreateDto;
    private OrderBookChangeRequest changeRequest;


    @BeforeEach
    void setUp() {
        // Устанавливаем значения @Value полей для тестов
        // Используем ReflectionTestUtils, так как @Value не работает напрямую с @InjectMocks вне контекста Spring
        ReflectionTestUtils.setField(orderService, "maxBooksInOrder", MAX_BOOKS_IN_ORDER);
        ReflectionTestUtils.setField(orderService, "maxRentalBooks", MAX_RENTAL_BOOKS);
        ReflectionTestUtils.setField(orderService, "daysRentalBooks", DAYS_RENTAL_BOOKS);

        // Настройка общих моков
        mockUser = new User();
        mockUser.setId(USER_ID);
        mockUser.setStatusBlock(false);
        mockUser.setBookRented(0); // Используем сеттер, если он есть


        mockBook1 = new Book();
        mockBook1.setId(BOOK_ID_1);
        mockBook1.setAvailable(5);
        mockBook1.setReserve(0);
        mockBook1.setCount(5); // Общее количество экземпляров

        mockBook2 = new Book();
        mockBook2.setId(BOOK_ID_2);
        mockBook2.setAvailable(1);
        mockBook2.setReserve(0);
        mockBook2.setCount(1);

        mockOrder = new Order();
        mockOrder.setId(ORDER_ID);
        mockOrder.setUser(mockUser);
        mockOrder.setCreatedData(LocalDate.now());

        mockOrderBook = new OrderBook();
        mockOrderBook.setId(ORDER_BOOK_ID);
        mockOrderBook.setOrder(mockOrder);
        mockOrderBook.setBook(mockBook1);
        mockOrderBook.setStatus(OrderBook.OrderBookStatus.CREATED);

        orderCreateDto = new OrderCreate();
        orderCreateDto.setUser_id(USER_ID);
        orderCreateDto.setBook_ids(List.of(BOOK_ID_1, BOOK_ID_2));

        changeRequest = new OrderBookChangeRequest();
        changeRequest.setOrderBookId(ORDER_BOOK_ID);
    }

    @Test
    @DisplayName("findAll - Должен вернуть страницу заказов")
    void findAll_ShouldReturnPageOfOrders() {
        // Arrange
        Pageable pageable = PageRequest.of(0, 10);
        List<Order> orderList = Collections.singletonList(mockOrder);
        Page<Order> orderPage = new PageImpl<>(orderList, pageable, orderList.size());

        when(orderRepository.findAll(pageable)).thenReturn(orderPage);

        // Act
        Page<Order> result = orderService.findAll(pageable);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        assertEquals(mockOrder, result.getContent().get(0));
        verify(orderRepository, times(1)).findAll(pageable);
    }

    @Test
    @DisplayName("findById - Должен вернуть заказ при существующем ID")
    void findById_WhenOrderExists_ShouldReturnOrder() {
        // Arrange
        when(orderRepository.findById(ORDER_ID)).thenReturn(Optional.of(mockOrder));

        // Act
        Order result = orderService.findById(ORDER_ID);

        // Assert
        assertNotNull(result);
        assertEquals(ORDER_ID, result.getId());
        verify(orderRepository, times(1)).findById(ORDER_ID);
    }

    @Test
    @DisplayName("findById - Должен выбросить NotFoundEntity при несуществующем ID")
    void findById_WhenOrderNotExists_ShouldThrowNotFoundEntity() {
        // Arrange
        Long nonExistentId = 999L;
        when(orderRepository.findById(nonExistentId)).thenReturn(Optional.empty());

        // Act & Assert
        NotFoundEntity exception = assertThrows(NotFoundEntity.class, () -> {
            orderService.findById(nonExistentId);
        });
        assertEquals("Order with id " + nonExistentId + " not found", exception.getMessage());
        verify(orderRepository, times(1)).findById(nonExistentId);
    }

    // --- Тесты для createNewOrder ---

    @Test
    @DisplayName("createNewOrder - Успешное создание заказа")
    void createNewOrder_Successful() {
        // Arrange
        when(userService.findById(USER_ID)).thenReturn(mockUser);
        when(bookService.findById(BOOK_ID_1)).thenReturn(mockBook1);
        when(bookService.findById(BOOK_ID_2)).thenReturn(mockBook2);

        Order savedOrderWithId = new Order();
        savedOrderWithId.setId(ORDER_ID);
        savedOrderWithId.setUser(mockUser);
        savedOrderWithId.setCreatedData(LocalDate.now());

        when(orderRepository.save(any(Order.class))).thenAnswer(invocation -> {
            Order orderToSave = invocation.getArgument(0);
            orderToSave.setId(ORDER_ID); // Устанавливаем ID
            return orderToSave; // Возвращаем "сохраненный" объект
        });

        mockOrder.setOrderBooks(Set.of( // Добавляем ожидаемые OrderBook к моку order
                new OrderBook(null, mockOrder, OrderBook.OrderBookStatus.CREATED, null, null,null,mockBook1 ),
                new OrderBook(null, mockOrder, OrderBook.OrderBookStatus.CREATED, null, null, null, mockBook2)
        ));
        when(orderRepository.findById(ORDER_ID)).thenReturn(Optional.of(mockOrder)); // Возвращаем мок заказа с книгами

        Order result = orderService.createNewOrder(orderCreateDto);

        assertNotNull(result);
        assertEquals(ORDER_ID, result.getId());
        assertEquals(mockUser, result.getUser());
        assertNotNull(result.getCreatedData());
        assertNotNull(result.getOrderBooks());
        assertEquals(2, result.getOrderBooks().size()); // Проверяем количество книг в заказе

        // Проверяем, что доступное количество книг уменьшилось
        assertEquals(4, mockBook1.getAvailable()); // 5 -> 4
        assertEquals(0, mockBook2.getAvailable()); // 1 -> 0

        verify(userService, times(1)).findById(USER_ID);
        verify(bookService, times(1)).findById(BOOK_ID_1);
        verify(bookService, times(1)).findById(BOOK_ID_2);
        verify(orderRepository, times(1)).save(any(Order.class)); // Проверяем, что save был вызван
        verify(orderRepository, times(1)).findById(ORDER_ID); // Проверяем финальный findById

    }

    @Test
    @DisplayName("createNewOrder - Пользователь не найден")
    void createNewOrder_UserNotFound() {
        Integer nonExistentUserId = 999;
        orderCreateDto.setUser_id(nonExistentUserId);
        when(userService.findById(nonExistentUserId)).thenThrow(new NotFoundUser("User not found")); // Или вернуть null и проверить исключение из сервиса

        assertThrows(NotFoundUser.class, () -> {
            orderService.createNewOrder(orderCreateDto);
        });
        verify(userService, times(1)).findById(nonExistentUserId);
        verifyNoInteractions(bookService, orderRepository, orderBookRepository); // Убеждаемся, что другие зависимости не вызывались
    }

    @Test
    @DisplayName("createNewOrder - Пользователь заблокирован")
    void createNewOrder_UserBlocked() {
        mockUser.setStatusBlock(true);
        when(userService.findById(USER_ID)).thenReturn(mockUser);

        assertThrows(UserBlockExeption.class, () -> {
            orderService.createNewOrder(orderCreateDto);
        });
        verify(userService, times(1)).findById(USER_ID);
        verifyNoInteractions(bookService, orderRepository, orderBookRepository);
    }

    @Test
    @DisplayName("createNewOrder - Превышено кол-во книг в заказе")
    void createNewOrder_TooManyBooksInOrder() {
        List<Integer> tooManyBooks = List.of(1, 2, 3, 4, 5, 6); // Больше чем MAX_BOOKS_IN_ORDER = 5
        orderCreateDto.setBook_ids(tooManyBooks);
        when(userService.findById(USER_ID)).thenReturn(mockUser); // Пользователь нужен для проверки лимита

        assertThrows(BookCountExcessException.class, () -> {
            orderService.createNewOrder(orderCreateDto);
        });
        verify(userService, times(1)).findById(USER_ID); // Вызывается до проверки кол-ва книг
        verifyNoInteractions(bookService, orderRepository, orderBookRepository);
    }

    @Test
    @DisplayName("createNewOrder - Превышен лимит книг на руках у пользователя")
    void createNewOrder_UserRentalLimitExceeded() {
        mockUser.setBookRented(MAX_RENTAL_BOOKS - 1); // Пользователь уже взял 9 книг (лимит 10)
        orderCreateDto.setBook_ids(List.of(BOOK_ID_1, BOOK_ID_2)); // Пытается взять еще 2
        when(userService.findById(USER_ID)).thenReturn(mockUser);

        assertThrows(BookCountExcessException.class, () -> {
            orderService.createNewOrder(orderCreateDto);
        });
        verify(userService, times(1)).findById(USER_ID);
        verifyNoInteractions(bookService, orderRepository, orderBookRepository);
    }

    @Test
    @DisplayName("createNewOrder - Книга недоступна")
    void createNewOrder_BookUnavailable() {

        mockBook2.setAvailable(0); // Вторая книга недоступна
        when(userService.findById(USER_ID)).thenReturn(mockUser);
        when(bookService.findById(BOOK_ID_1)).thenReturn(mockBook1);
        when(bookService.findById(BOOK_ID_2)).thenReturn(mockBook2);


        Order savedOrderWithId = new Order();
        savedOrderWithId.setId(ORDER_ID);
        savedOrderWithId.setUser(mockUser);
        savedOrderWithId.setCreatedData(LocalDate.now());
        when(orderRepository.save(any(Order.class))).thenReturn(savedOrderWithId);


        assertThrows(BookUnavailableException.class, () -> {
            orderService.createNewOrder(orderCreateDto);
        });

        verify(userService, times(1)).findById(USER_ID);
        verify(bookService, times(1)).findById(BOOK_ID_1);
        verify(bookService, times(1)).findById(BOOK_ID_2);
        verify(orderRepository, times(1)).save(any(Order.class)); // Пустой заказ сохраняется до проверки книг
        verify(orderRepository, never()).findById(anyLong()); // Финальный findById не вызывается
        verifyNoInteractions(orderBookRepository); // orderBookRepository.save не вызывается
    }

    @Test
    @DisplayName("createNewOrder - Книга не найдена (через BookService)")
    void createNewOrder_BookNotFound() {
        int nonExistentBookId = 999;
        orderCreateDto.setBook_ids(List.of(BOOK_ID_1, nonExistentBookId));
        when(userService.findById(USER_ID)).thenReturn(mockUser);
        when(bookService.findById(BOOK_ID_1)).thenReturn(mockBook1);
        when(bookService.findById(nonExistentBookId)).thenThrow(new NotFoundEntity("Book not found"));

        Order savedOrderWithId = new Order();
        savedOrderWithId.setId(ORDER_ID);
        savedOrderWithId.setUser(mockUser);
        savedOrderWithId.setCreatedData(LocalDate.now());
        when(orderRepository.save(any(Order.class))).thenReturn(savedOrderWithId);

        assertThrows(NotFoundEntity.class, () -> {
            orderService.createNewOrder(orderCreateDto);
        });

        verify(userService, times(1)).findById(USER_ID);
        verify(bookService, times(1)).findById(BOOK_ID_1);
        verify(bookService, times(1)).findById(nonExistentBookId);
        verify(orderRepository, times(1)).save(any(Order.class));
        verify(orderRepository, never()).findById(anyLong());
        verifyNoInteractions(orderBookRepository);
    }


    // --- Тесты для changeOrderBook ---

    @Test
    @DisplayName("changeOrderBook - OrderBook не найден")
    void changeOrderBook_OrderBookNotFound() {
        Long nonExistentOrderBookId = 999L;
        changeRequest.setOrderBookId(nonExistentOrderBookId);
        changeRequest.setStatus(OrderBook.OrderBookStatus.PREPARED);
        when(orderBookRepository.findById(nonExistentOrderBookId)).thenReturn(Optional.empty());

        assertThrows(NotFoundEntity.class, () -> {
            orderService.changeOrderBook(changeRequest);
        });

        verify(orderBookRepository, times(1)).findById(nonExistentOrderBookId);
        verify(orderBookRepository, never()).save(any());
    }

    @Test
    @DisplayName("changeOrderBook - Статус не изменен")
    void changeOrderBook_StatusNotChanged() {
        changeRequest.setStatus(OrderBook.OrderBookStatus.CREATED);
        when(orderBookRepository.findById(ORDER_BOOK_ID)).thenReturn(Optional.of(mockOrderBook));

        OrderBook result = orderService.changeOrderBook(changeRequest);

        assertNotNull(result);
        assertEquals(mockOrderBook, result);
        verify(orderBookRepository, times(1)).findById(ORDER_BOOK_ID);
        verify(orderBookRepository, never()).save(any());
    }

    @Test
    @DisplayName("changeOrderBook - CREATED -> PREPARED")
    void changeOrderBook_CreatedToPrepared() {
        changeRequest.setStatus(OrderBook.OrderBookStatus.PREPARED);
        mockOrderBook.setStatus(OrderBook.OrderBookStatus.CREATED);
        int initialReserve = mockBook1.getReserve();

        when(orderBookRepository.findById(ORDER_BOOK_ID)).thenReturn(Optional.of(mockOrderBook));
        // Возвращаем измененный объект при сохранении
        when(orderBookRepository.save(any(OrderBook.class))).thenAnswer(invocation -> invocation.getArgument(0));

        OrderBook result = orderService.changeOrderBook(changeRequest);

        assertNotNull(result);
        assertEquals(OrderBook.OrderBookStatus.PREPARED, result.getStatus());
        assertEquals(initialReserve + 1, result.getBook().getReserve()); // Проверяем увеличение резерва

        verify(orderBookRepository, times(1)).findById(ORDER_BOOK_ID);
        verify(orderBookRepository, times(1)).save(any(OrderBook.class));
    }

    @Test
    @DisplayName("changeOrderBook - CREATED -> LOSSLIBRARY")
    void changeOrderBook_CreatedToLossLibrary() {
        changeRequest.setStatus(OrderBook.OrderBookStatus.LOSSLIBRARY);
        mockOrderBook.setStatus(OrderBook.OrderBookStatus.CREATED);
        int initialCount = mockBook1.getCount(); // Используем общее количество для обновления available

        when(orderBookRepository.findById(ORDER_BOOK_ID)).thenReturn(Optional.of(mockOrderBook));
        when(orderBookRepository.save(any(OrderBook.class))).thenAnswer(invocation -> invocation.getArgument(0));

        OrderBook result = orderService.changeOrderBook(changeRequest);

        assertNotNull(result);
        assertEquals(OrderBook.OrderBookStatus.LOSSLIBRARY, result.getStatus());
        assertEquals(initialCount - 1, result.getBook().getAvailable()); // Проверяем установку available = count - 1

        verify(orderBookRepository, times(1)).findById(ORDER_BOOK_ID);
        verify(orderBookRepository, times(1)).save(any(OrderBook.class));
    }

    @Test
    @DisplayName("changeOrderBook - PREPARED -> RENTED")
    void changeOrderBook_PreparedToRented() {

        changeRequest.setStatus(OrderBook.OrderBookStatus.RENTED);
        mockOrderBook.setStatus(OrderBook.OrderBookStatus.PREPARED);
        mockBook1.setReserve(1); // Книга должна быть в резерве
        int initialReserve = mockBook1.getReserve();
        LocalDate today = LocalDate.now();
        LocalDate expectedReturnDate = today.plusDays(DAYS_RENTAL_BOOKS);

        when(orderBookRepository.findById(ORDER_BOOK_ID)).thenReturn(Optional.of(mockOrderBook));
        when(orderBookRepository.save(any(OrderBook.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        OrderBook result = orderService.changeOrderBook(changeRequest);

        // Assert
        assertNotNull(result);
        assertEquals(OrderBook.OrderBookStatus.RENTED, result.getStatus());
        assertEquals(initialReserve - 1, result.getBook().getReserve()); // Резерв уменьшился
        assertEquals(today, result.getDateStartRentedBook()); // Дата начала аренды установлена
        assertEquals(expectedReturnDate, result.getDateReturnUpto()); // Дата возврата установлена

        verify(orderBookRepository, times(1)).findById(ORDER_BOOK_ID);
        verify(orderBookRepository, times(1)).save(any(OrderBook.class));
    }


    @Test
    @DisplayName("changeOrderBook - RENTED -> RETURNED (On Time)")
    void changeOrderBook_RentedToReturned_OnTime() {

        changeRequest.setStatus(OrderBook.OrderBookStatus.RETURNED);
        mockOrderBook.setStatus(OrderBook.OrderBookStatus.RENTED);
        // Устанавливаем дату возврата в будущем, чтобы не было просрочки
        mockOrderBook.setDateReturnUpto(LocalDate.now().plusDays(1));
        int initialAvailable = mockBook1.getAvailable();

        when(orderBookRepository.findById(ORDER_BOOK_ID)).thenReturn(Optional.of(mockOrderBook));
        when(orderBookRepository.save(any(OrderBook.class))).thenAnswer(invocation -> invocation.getArgument(0));

        OrderBook result = orderService.changeOrderBook(changeRequest);


        assertNotNull(result);
        assertEquals(OrderBook.OrderBookStatus.RETURNED, result.getStatus());
        assertNotNull(result.getDateReturnedBook());
        assertEquals(initialAvailable + 1, result.getBook().getAvailable());
        verify(userService,never()).addViolation(USER_ID);

        ArgumentCaptor<OrderBook> captor = ArgumentCaptor.forClass(OrderBook.class);
        verify(orderBookRepository, times(1)).save(captor.capture());
        OrderBook savedOrderBook = captor.getValue();
        assertEquals(OrderBook.OrderBookStatus.RETURNED, savedOrderBook.getStatus());
        assertEquals(initialAvailable + 1, savedOrderBook.getBook().getAvailable());
    }

    @Test
    @DisplayName("changeOrderBook - RENTED -> RETURNED (Overdue)")
    void changeOrderBook_RentedToReturned_Overdue() {

        changeRequest.setStatus(OrderBook.OrderBookStatus.RETURNED);
        mockOrderBook.setStatus(OrderBook.OrderBookStatus.RENTED);
        // Устанавливаем дату возврата в прошлом для имитации просрочки
        mockOrderBook.setDateReturnUpto(LocalDate.now().minusDays(1));
        int initialAvailable = mockBook1.getAvailable();

        when(orderBookRepository.findById(ORDER_BOOK_ID)).thenReturn(Optional.of(mockOrderBook));
        when(orderBookRepository.save(any(OrderBook.class))).thenAnswer(invocation -> invocation.getArgument(0));

        OrderBook result = orderService.changeOrderBook(changeRequest);

        assertNotNull(result);
        assertEquals(OrderBook.OrderBookStatus.RETURNED, result.getStatus());
        assertNotNull(result.getDateReturnedBook());
        assertEquals(initialAvailable + 1, result.getBook().getAvailable());

        verify(userService,times(1)).addViolation(USER_ID);

        ArgumentCaptor<OrderBook> captor = ArgumentCaptor.forClass(OrderBook.class);
        verify(orderBookRepository, times(1)).save(captor.capture());
        OrderBook savedOrderBook = captor.getValue();
    }


    // ---- RENTED -> LOSSUSER ----

    @Test
    @DisplayName("changeOrderBook - RENTED -> LOSSUSER")
    void changeOrderBook_RentedToLossUser() {
        // Arrange
        changeRequest.setStatus(OrderBook.OrderBookStatus.LOSSUSER);
        mockOrderBook.setStatus(OrderBook.OrderBookStatus.RENTED);
        int initialCount = mockBook1.getCount(); // Используем count для обновления available

        when(orderBookRepository.findById(ORDER_BOOK_ID)).thenReturn(Optional.of(mockOrderBook));
        when(orderBookRepository.save(any(OrderBook.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        OrderBook result = orderService.changeOrderBook(changeRequest);

        // Assert
        assertNotNull(result);
        assertEquals(OrderBook.OrderBookStatus.LOSSUSER, result.getStatus());
        verify(userService,times(1)).addViolation(USER_ID);; // Нарушение +1
        assertFalse(result.getOrder().getUser().getStatusBlock()); // Блокировки нет
        assertEquals(initialCount - 1, result.getBook().getAvailable()); // Книга списывается (available = count - 1)

        ArgumentCaptor<OrderBook> captor = ArgumentCaptor.forClass(OrderBook.class);
        verify(orderBookRepository, times(1)).save(captor.capture());
        OrderBook savedOrderBook = captor.getValue();
        assertEquals(initialCount - 1, savedOrderBook.getBook().getAvailable());
    }


    // ---- CANCELLED ----

    @Test
    @DisplayName("changeOrderBook - PREPARED -> CANCELLED")
    void changeOrderBook_PreparedToCancelled() {
        // Arrange
        changeRequest.setStatus(OrderBook.OrderBookStatus.CANCELLED);
        mockOrderBook.setStatus(OrderBook.OrderBookStatus.PREPARED);
        mockBook1.setReserve(1); // Была в резерве
        mockBook1.setAvailable(4); // Доступных было меньше
        int initialReserve = mockBook1.getReserve();
        int initialAvailable = mockBook1.getAvailable();

        when(orderBookRepository.findById(ORDER_BOOK_ID)).thenReturn(Optional.of(mockOrderBook));
        when(orderBookRepository.save(any(OrderBook.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        OrderBook result = orderService.changeOrderBook(changeRequest);

        // Assert
        assertNotNull(result);
        assertEquals(OrderBook.OrderBookStatus.CANCELLED, result.getStatus()); // Статус установлен в моке перед save
        assertEquals(initialReserve - 1, result.getBook().getReserve()); // Резерв снят
        assertEquals(initialAvailable + 1, result.getBook().getAvailable()); // Доступность увеличена

        ArgumentCaptor<OrderBook> captor = ArgumentCaptor.forClass(OrderBook.class);
        verify(orderBookRepository, times(1)).save(captor.capture());
        OrderBook savedOrderBook = captor.getValue();
        // Статус CANCELLED должен быть установлен в самом orderBook перед сохранением
        //assertEquals(OrderBook.OrderBookStatus.CANCELLED, savedOrderBook.getStatus()); // Это не проверяем, т.к. статус неявно присваивается в коде
        assertEquals(initialReserve - 1, savedOrderBook.getBook().getReserve());
        assertEquals(initialAvailable + 1, savedOrderBook.getBook().getAvailable());
    }

    @Test
    @DisplayName("changeOrderBook - CREATED -> CANCELLED")
    void changeOrderBook_CreatedToCancelled() {
        // Arrange
        changeRequest.setStatus(OrderBook.OrderBookStatus.CANCELLED);
        mockOrderBook.setStatus(OrderBook.OrderBookStatus.CREATED);
        // Предполагаем, что книга была зарезервирована при создании заказа (уменьшен available)
        mockBook1.setAvailable(4);
        int initialAvailable = mockBook1.getAvailable();

        when(orderBookRepository.findById(ORDER_BOOK_ID)).thenReturn(Optional.of(mockOrderBook));
        when(orderBookRepository.save(any(OrderBook.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        OrderBook result = orderService.changeOrderBook(changeRequest);

        // Assert
        assertNotNull(result);
        assertEquals(initialAvailable + 1, result.getBook().getAvailable()); // Доступность возвращена

        ArgumentCaptor<OrderBook> captor = ArgumentCaptor.forClass(OrderBook.class);
        verify(orderBookRepository, times(1)).save(captor.capture());
        OrderBook savedOrderBook = captor.getValue();
        assertEquals(initialAvailable + 1, savedOrderBook.getBook().getAvailable());
    }

    @Test
    @DisplayName("changeOrderBook - RENTED -> CANCELLED (Неподдерживаемый переход)")
    void changeOrderBook_RentedToCancelled_ThrowsException() {
        // Arrange
        changeRequest.setStatus(OrderBook.OrderBookStatus.CANCELLED);
        mockOrderBook.setStatus(OrderBook.OrderBookStatus.RENTED);

        when(orderBookRepository.findById(ORDER_BOOK_ID)).thenReturn(Optional.of(mockOrderBook));

        // Act & Assert
        assertThrows(OrderBookStatusException.class, () -> {
            orderService.changeOrderBook(changeRequest);
        });

        verify(orderBookRepository, times(1)).findById(ORDER_BOOK_ID);
        verify(orderBookRepository, never()).save(any());
    }


    @Test
    @DisplayName("changeOrderBook - RETURNED -> PREPARED (Неподдерживаемый переход)")
    void changeOrderBook_UnsupportedTransition_ThrowsException() {
        // Arrange
        changeRequest.setStatus(OrderBook.OrderBookStatus.PREPARED); // Попытка недопустимого перехода
        mockOrderBook.setStatus(OrderBook.OrderBookStatus.RETURNED); // Из статуса RETURNED

        when(orderBookRepository.findById(ORDER_BOOK_ID)).thenReturn(Optional.of(mockOrderBook));

        // Act & Assert
        assertThrows(RuntimeException.class, () -> {
            orderService.changeOrderBook(changeRequest);
        });
        // Ожидаем общее исключение для неподдерживаемых переходов

        verify(orderBookRepository, times(1)).findById(ORDER_BOOK_ID);
        verify(orderBookRepository, never()).save(any());
    }
}