package ru.olympusnsp.library.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.test.util.ReflectionTestUtils;
import ru.olympusnsp.library.dto.OrderCreate;
import ru.olympusnsp.library.exeption.BookCountExcessException;
import ru.olympusnsp.library.exeption.BookUnavailableException;
import ru.olympusnsp.library.exeption.NotFoundEntity;
import ru.olympusnsp.library.exeption.NotFoundUser;
import ru.olympusnsp.library.model.Book;
import ru.olympusnsp.library.model.Order;
import ru.olympusnsp.library.model.User;
import ru.olympusnsp.library.repository.OrderRepository;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class) // Интеграция Mockito с JUnit 5
class OrderServiceImplTest {

    @Mock // Создаем мок для OrderRepository
    private OrderRepository orderRepository;

    @Mock // Создаем мок для UserService
    private UserService userService;

    @Mock // Создаем мок для BookService
    private BookService bookService;

    @InjectMocks // Внедряем моки в тестируемый сервис
    private OrderServiceImpl orderService;

    @BeforeEach
    void setUp() {
        // Устанавливаем значения для @Value полей с помощью рефлексии
        ReflectionTestUtils.setField(orderService, "maxBooksInOrder", 3);
        ReflectionTestUtils.setField(orderService, "maxRentalBooks", 5);

    }

    @Test
    @DisplayName("findAll должен возвращать страницу заказов")
    void findAll_shouldReturnPageOfOrders() {
        Pageable pageable = PageRequest.of(0, 10);
        List<Order> orderList = List.of(new Order());
        Page<Order> orderPage = new PageImpl<>(orderList, pageable, orderList.size());
        when(orderRepository.findAll(pageable)).thenReturn(orderPage);
        Page<Order> result = orderService.findAll(pageable);

        assertEquals(orderPage, result);
        verify(orderRepository).findAll(pageable); // Убеждаемся, что метод репозитория был вызван
    }

    @Test
    @DisplayName("findById должен возвращать заказ, если он найден")
    void findById_whenOrderExists_shouldReturnOrder() {
        // Arrange
        Long orderId = 1L;
        Order o = new Order();
        when(orderRepository.findById(orderId)).thenReturn(Optional.of(o));
        Order foundOrder = orderService.findById(orderId);
        // Assert
        assertNotNull(foundOrder);
        assertEquals(o, foundOrder);
        verify(orderRepository).findById(orderId);
    }

    @Test
    @DisplayName("findById должен выбрасывать NotFoundEntity, если заказ не найден")
    void findById_whenOrderNotFound_shouldThrowNotFoundEntity() {
        // Arrange
        Long orderId = 99L;
        when(orderRepository.findById(orderId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(NotFoundEntity.class, () -> {
            orderService.findById(orderId);
        });
        verify(orderRepository).findById(orderId);
    }

    @Test
    @DisplayName("createNewOrder должен успешно создавать заказ")
    void createNewOrder_whenValid_shouldCreateOrder() {
        // Arrange
        Integer userId =4;
        List<Integer> bookIds = List.of(101,102);
        User user = User.builder().bookRented(0).id(userId).build();

        OrderCreate orderCreate = OrderCreate.builder().user_id(userId).book_ids(bookIds).build();
        when(userService.findById(userId)).thenReturn(user);
        when(bookService.reserveBookById(101)).thenReturn(true);
        when(bookService.reserveBookById(102)).thenReturn(true);
        // Настраиваем мок save так, чтобы он возвращал переданный ему объект с некоторыми изменениями
        when(orderRepository.save(any(Order.class))).thenAnswer(invocation -> {
            Order orderToSave = invocation.getArgument(0);
            orderToSave.setId(2L); // Имитируем присвоение ID при сохранении
            return orderToSave;
        });

        // Act
        Order createdOrder = orderService.createNewOrder(orderCreate);

        // Assert
        assertNotNull(createdOrder);
        assertEquals(userId, createdOrder.getUser().getId());
        assertEquals(Order.OrderStatus.READY, createdOrder.getStatus());
        assertNotNull(createdOrder.getId()); // Убеждаемся, что ID был присвоен (имитация save)

        verify(userService).findById(userId);
        verify(bookService).reserveBookById(101);
        verify(bookService).reserveBookById(102);
        verify(orderRepository).save(any(Order.class));
    }

    @Test
    @DisplayName("createNewOrder должен выбрасывать BookUnavailableExeption, когда одна из книг закончилась")
    void createNewOrder_whenBookAvailableZero_shouldCreateOrder() {

        Integer userId = 3;
        List<Integer> bookIds = List.of(101,102);
        User user =  User.builder().bookRented(0).build();
        OrderCreate orderCreate = OrderCreate.builder().user_id(userId).book_ids(bookIds).build();

        when(userService.findById(userId)).thenReturn(user);
        when(bookService.reserveBookById(101)).thenReturn(true);
        when(bookService.reserveBookById(102)).thenReturn(false); //Нет доступной книги

        assertThrows(BookUnavailableException.class, () -> {
                    orderService.createNewOrder(orderCreate);
        });
        verify(userService).findById(userId);
        verify(orderRepository, never()).save(any(Order.class));
    }

    @Test
    @DisplayName("createNewOrder должен выбрасывать NotFoundUser, если пользователь не найден")
    void createNewOrder_whenUserNotFound_shouldThrowNotFoundUser() {

        Integer userId = 99;
        List<Integer> bookIds = List.of(101,102);
        OrderCreate orderCreate = OrderCreate.builder().user_id(userId).book_ids(bookIds).build();

        when(userService.findById(userId)).thenThrow(new NotFoundUser("Пользователь не найден"));


        assertThrows(NotFoundUser.class, () -> {
            orderService.createNewOrder(orderCreate);
        });
        verify(userService).findById(userId);
        verify(bookService, never()).reserveBookById(anyInt()); // Резервирование не должно вызываться
        verify(orderRepository, never()).save(any(Order.class)); // Сохранение не должно вызываться
    }

    @Test
    @DisplayName("createNewOrder должен выбрасывать BookCountExcessException, если книг больше maxBooksInOrder")
    void createNewOrder_whenTooManyBooksInOrder_shouldThrowBookCountExcessException() {

        Integer userId = 4;
        User user = new User();
        user.setBookRented(0);
        List<Integer> bookIds = List.of(101, 102, 103, 104); // Больше 3 (maxBooksInOrder)
        OrderCreate orderCreate = OrderCreate.builder().user_id(userId).book_ids(bookIds).build();

        when(userService.findById(userId)).thenReturn(user);

        assertThrows(BookCountExcessException.class, () -> {
            orderService.createNewOrder(orderCreate);
        });
        verify(userService).findById(userId);
        verify(bookService, never()).reserveBookById(anyInt());
        verify(orderRepository, never()).save(any(Order.class));
    }

    @Test
    @DisplayName("createNewOrder должен выбрасывать BookCountExcessException, если превышен лимит книг пользователем (с новыми лимит по количеству книг на руках будет превышен)")
    void createNewOrder_whenUserRentalLimitExceeded_shouldThrowBookCountExcessException() {
        // Arrange
        Integer userId = 4;
        List<Integer> bookIds = List.of(101, 102);

        OrderCreate orderCreate = OrderCreate.builder().user_id(userId).book_ids(bookIds).build();
        User user =  User.builder().bookRented(4).build();
        // У пользователя уже 4 книги (лимит 5)
        // 4 (уже есть) + 2 (в заказе) > 5 (maxRentalBooks)

        when(userService.findById(userId)).thenReturn(user);


        assertThrows(BookCountExcessException.class, () -> {
            orderService.createNewOrder(orderCreate);
        });
        verify(userService).findById(userId);
        verify(bookService, never()).reserveBookById(anyInt());
        verify(orderRepository, never()).save(any(Order.class));
    }



    @Test
    @DisplayName("createNewOrder должен выбрасывать BookUnavailableException, если книга недоступна")
    void createNewOrder_whenBookUnavailable_shouldThrowBookUnavailableException() {

        Integer userId =4;

        Integer availableBookId = 101;
        Integer unavailableBookId = 102;
        List<Integer> bookIds = List.of(availableBookId,unavailableBookId);
        OrderCreate orderCreate = OrderCreate.builder().user_id(userId).book_ids(bookIds).build();

        User user = new User();
        user.setBookRented(0);

        when(userService.findById(userId)).thenReturn(user);
        when(bookService.reserveBookById(availableBookId)).thenReturn(true); // Первая книга доступна
        when(bookService.reserveBookById(unavailableBookId)).thenReturn(false); // Вторая недоступна


        assertThrows(BookUnavailableException.class, () -> {
            orderService.createNewOrder(orderCreate);
        });
        verify(userService).findById(userId);
        verify(bookService).reserveBookById(unavailableBookId);
        verify(orderRepository, never()).save(any(Order.class)); // Заказ не должен сохраняться
    }

    @Test
    @DisplayName("startRentalOrder должен обновить резерв книг, если заказ найден")
    void startRentalOrder_whenOrderFound_shouldUpdateBookReserve() {
        // Arrange
        Long orderId = 10L;
        // Устанавливаем начальный резерв в 1, чтобы проверить уменьшение до 0
        Book testBook1 =  mock(Book.class);
        Book testBook2 =  mock(Book.class);
        when(testBook1.getReserve()).thenReturn(1);
        when(testBook2.getReserve()).thenReturn(2);
        User user =  User.builder().bookRented(4).build();
        Order testOrder = Order.builder().reservedBooks(List.of(testBook1,testBook2)).user(user).build();

        when(orderRepository.findById(orderId)).thenReturn(Optional.of(testOrder));

        // Act
        orderService.startRentalOrder(orderId);

        // Assert
        verify(orderRepository).findById(orderId);
        verify(testBook1).getReserve();
        verify(testBook2).getReserve();
        verify(testBook1).setReserve(0);
        verify(testBook2).setReserve(1);

    }

    @Test
    @DisplayName("startRentalOrder должен выбрасывать NotFoundEntity, если заказ не найден")
    void startRentalOrder_whenOrderNotFound_shouldThrowNotFoundEntity() {

        Long orderId = 99L;
        when(orderRepository.findById(orderId)).thenReturn(Optional.empty());


        assertThrows(NotFoundEntity.class, () -> {
            orderService.startRentalOrder(orderId);
        });verify(orderRepository).findById(orderId);
    }
}