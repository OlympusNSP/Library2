
package ru.olympusnsp.library.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.web.bind.annotation.ExceptionHandler;
import ru.olympusnsp.library.dto.OrderBookChangeRequest;
import ru.olympusnsp.library.dto.OrderBookStatusDTO;
import ru.olympusnsp.library.dto.OrderCreate;
import ru.olympusnsp.library.exeption.NotFoundEntity;
import ru.olympusnsp.library.exeption.UserIdInRequestAndUserDetailDifferentException;
import ru.olympusnsp.library.model.Book;
import ru.olympusnsp.library.model.Order;
import ru.olympusnsp.library.model.OrderBook;
import ru.olympusnsp.library.model.User; // Предполагаем наличие енама для статуса
import ru.olympusnsp.library.service.OrderService;
import ru.olympusnsp.library.service.UserService;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Date; // Используйте java.time если возможно
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf; // Если CSRF включен
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user; // Альтернативный способ задать пользователя
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
        import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
        import static org.hamcrest.Matchers.is; // Для jsonPath проверок
@WebMvcTest(OrderController.class)
@DisplayName("Тесты OrderController с MockMvc")
class OrderControllerMockMvcTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private OrderService orderService;

    @MockBean
    private UserService userService;

    private User testUser;
    private OrderCreate orderCreateRequest;
    private Order expectedOrder;
    private OrderBookStatusDTO orderBookStatusDTO;
    private OrderBook expectedOrderBook;
    private Book sampleBook;

    private final String testUsername = "testuser";
    private final Integer testUserId = 1;
    private final Long testOrderId = 10L;
    private final Long testOrderBookId = 20L;
    private final Long nonExistentId = 99L;


    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setId(testUserId);
        testUser.setUsername(testUsername);

        sampleBook = new Book();
        sampleBook.setId(3);
        sampleBook.setTitle("Test Book");
        sampleBook.setCount(3);
        sampleBook.setYear((short)2025);
        sampleBook.setDescription("Test Book Description");
        sampleBook.setReserve(1);
        sampleBook.setAvailable(1);

        orderCreateRequest = new OrderCreate();
        orderCreateRequest.setUser_id(testUserId);
        orderCreateRequest.setBook_ids(List.of(3));



        orderBookStatusDTO = new OrderBookStatusDTO();
        orderBookStatusDTO.setStatus(OrderBook.OrderBookStatus.PREPARED); // Используем предполагаемый Enum

        expectedOrderBook = new OrderBook();
        expectedOrderBook.setId(testOrderBookId);
        expectedOrderBook.setStatus(OrderBook.OrderBookStatus.CREATED);
        expectedOrderBook.setBook(sampleBook);

        expectedOrder = new Order();
        expectedOrder.setId(testOrderId);
        expectedOrder.setUser(testUser); // Связь с пользователем
        expectedOrder.setCreatedData(LocalDate.now());
        expectedOrder.setOrderBooks(Set.of(expectedOrderBook));
    }


    @Test
    @WithMockUser(username = testUsername) // Имитирует аутентифицированного пользователя
    @DisplayName("POST /order - Успешное создание заказа")
    void newOrder_WhenValidRequestAndUserMatch_ShouldReturnCreatedOrder() throws Exception {

        given(userService.getByUsername(testUsername)).willReturn(testUser);
        given(orderService.createNewOrder(any(OrderCreate.class))).willReturn(expectedOrder);

        ResultActions resultActions = mockMvc.perform(post("/order")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(orderCreateRequest))
                .with(csrf())); // Добавляем CSRF токен, если он используется

        // Assert
        resultActions
                .andExpect(status().isOk()) // Ожидаем статус 200 OK (или 201 Created)
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("id", is(expectedOrder.getId().intValue()))) // Проверяем поля в JSON ответе.andExpect(jsonPath("$.user.id", is(testUserId)));
                .andExpect(jsonPath("orderBooks[0].status",is("CREATED")))
                .andExpect(jsonPath("orderBooks[0].book.id",is(sampleBook.getId()))); // Если user сериализуется

        // Verify
        verify(userService).getByUsername(testUsername);
        verify(orderService).createNewOrder(any(OrderCreate.class)); // Проверяем, что сервисы были вызваны
    }

    @Test
    @WithMockUser(username = testUsername)
    @ExceptionHandler(UserIdInRequestAndUserDetailDifferentException.class)
    @DisplayName("POST /order - Ошибка: ID пользователя в запросе не совпадает с аутентифицированным")
    void newOrder_WhenUserIdMismatch_ShouldThrowExceptionAndReturnBadRequest() throws Exception {
        OrderCreate mismatchRequest = new OrderCreate();
        mismatchRequest.setUser_id(testUserId + 1); // Неправильный ID
        mismatchRequest.setBook_ids(List.of(3));

        given(userService.getByUsername(testUsername)).willReturn(testUser);

        ResultActions resultActions = mockMvc.perform(post("/order")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(mismatchRequest))
                .with(csrf()));

        // Assert
        resultActions
                .andExpect(status().isBadRequest())
                .andExpect(result -> assertInstanceOf(UserIdInRequestAndUserDetailDifferentException.class, result.getResolvedException()))
                ;

        // Verify
        verify(userService).getByUsername(testUsername);
        verify(orderService, never()).createNewOrder(any(OrderCreate.class)); // Убедимся, что метод создания заказа не вызывался
    }

    @Test
    @DisplayName("POST /order - Ошибка: Не аутентифицированный пользователь")
    void newOrder_WhenUnauthenticated_ShouldReturnUnauthorized() throws Exception {

        ResultActions resultActions = mockMvc.perform(post("/order")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(orderCreateRequest)));

        resultActions.andExpect(status().is4xxClientError()); // или isForbidden()
    }

    @Test
    @WithMockUser(username = testUsername)
    @DisplayName("POST /order - Ошибка валидации DTO")
    void newOrder_WhenInvalidDto_ShouldReturnBadRequest() throws Exception {
        OrderCreate invalidRequest = new OrderCreate();

        ResultActions resultActions = mockMvc.perform(post("/order")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidRequest))
                .with(csrf()));


        resultActions.andExpect(status().isBadRequest());

        // Verify
        verify(userService, never()).getByUsername(any());
        verify(orderService, never()).createNewOrder(any(OrderCreate.class));
    }


    @Test
    @DisplayName("GET /order/{id} - Успешный поиск существующего заказа")
    @WithMockUser // Некоторые GET запросы тоже могут требовать аутентификации
    void findById_WhenOrderExists_ShouldReturnOrder() throws Exception {
        given(orderService.findById(testOrderId)).willReturn(expectedOrder);

        ResultActions resultActions = mockMvc.perform(get("/order/{id}", testOrderId)
                .accept(MediaType.APPLICATION_JSON));

        // Assert
        resultActions
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id", is(testOrderId.intValue())))
                .andExpect(jsonPath("orderBooks[0].status",is("CREATED")))
                .andExpect(jsonPath("orderBooks[0].book.id",is(sampleBook.getId()))); // Если user сериализуется


        // Verify
        verify(orderService).findById(testOrderId);
    }

    @Test
    @DisplayName("GET /order/{id} - Ошибка: Заказ не найден")
    @WithMockUser
    void findById_WhenOrderNotFound_ShouldReturnNotFound() throws Exception {

        given(orderService.findById(nonExistentId)).willThrow(new NotFoundEntity("Order not found"));

        ResultActions resultActions = mockMvc.perform(get("/order/{id}", nonExistentId)
                .accept(MediaType.APPLICATION_JSON));

        resultActions.andExpect(status().isNotFound()); // Ожидаем 404 Not Found

        // Verify
        verify(orderService).findById(nonExistentId);
    }

    // --- Тесты для POST /order/orderbook/{id} ---

    @Test
    @DisplayName("PUT /order/orderbook/{id} - Успешное изменение статуса OrderBook")
    @WithMockUser(roles = "MANAGER")
    void changeOrder_WhenValidRequest_ShouldReturnUpdatedOrderBook() throws Exception {

        OrderBookChangeRequest expectedRequest = new OrderBookChangeRequest(testOrderBookId, orderBookStatusDTO.getStatus());

        given(orderService.changeOrderBook(any(OrderBookChangeRequest.class))).willReturn(expectedOrderBook);


        // Act
        ResultActions resultActions = mockMvc.perform(put("/order/orderbook/{id}", testOrderBookId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(orderBookStatusDTO))
                .with(csrf())); // Добавляем CSRF


        resultActions
                .andExpect(status().isOk())
                .andExpect(jsonPath("id", is(testOrderBookId.intValue()))); // Сравниваем строковое представление Enum

    }

}