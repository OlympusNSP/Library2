package ru.olympusnsp.library.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.olympusnsp.library.exeption.NotFoundUser;
import ru.olympusnsp.library.model.User;
import ru.olympusnsp.library.repository.UserRepository;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class UserServiceImplTest {

    @Mock
    private UserRepository userRepository;


    @InjectMocks
    private UserServiceImpl userService;

    private User testUser;
    private final Integer userId = 1;
    private final Integer nonExistentUserId = 99;


    @Test
    @DisplayName("findById должен вернуть пользователя, если он существует")
    void findById_WhenUserExists_ShouldReturnUser() {
        // Arrange (Подготовка)
        testUser = new User(); // Предполагаем, что у User есть конструктор по умолчанию
        testUser.setId(userId);
        testUser.setViolations(0);
        testUser.setStatusBlock(false);

        when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));

        User foundUser = userService.findById(userId);

        assertNotNull(foundUser);
        assertEquals(userId, foundUser.getId());
        assertEquals(testUser, foundUser);

        verify(userRepository, times(1)).findById(userId); // Убедимся, что метод findById был вызван ровно 1 раз с нужным ID
        verifyNoMoreInteractions(userRepository); // Убедимся, что других взаимодействий с репозиторием не было
    }

    @Test
    @DisplayName("findById должен выбросить NotFoundUser, если пользователь не найден")
    void findById_WhenUserNotFound_ShouldThrowNotFoundUserException() {

        when(userRepository.findById(nonExistentUserId)).thenReturn(Optional.empty());

        assertThrows(NotFoundUser.class, () -> {
            userService.findById(nonExistentUserId);
        });

        verify(userRepository, times(1)).findById(nonExistentUserId);
        verifyNoMoreInteractions(userRepository);
    }


    @Test
    @DisplayName("addViolation должен увеличить нарушения и не блокировать пользователя (первое нарушение)")
    void addViolation_WhenUserExistsAndFirstViolation_ShouldIncrementViolationsAndNotBlock() {
        testUser = new User();
        testUser.setId(userId);
        testUser.setViolations(0); // Начальное состояние - 0 нарушений
        testUser.setStatusBlock(false);


        when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));

        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        when(userRepository.save(userCaptor.capture())).thenAnswer(invocation -> invocation.getArgument(0)); // Возвращаем того же юзера, что пришел на сохранение

        User updatedUser = userService.addViolation(userId);


        assertNotNull(updatedUser);
        assertEquals(1, updatedUser.getViolations());
        assertFalse(updatedUser.getStatusBlock());

        User savedUser = userCaptor.getValue(); // Получаем пользователя, который был передан в save
        assertNotNull(savedUser);
        assertEquals(1, savedUser.getViolations() );
        assertFalse(savedUser.getStatusBlock());

        verify(userRepository, times(1)).findById(userId);
        verify(userRepository, times(1)).save(any(User.class)); // Проверяем, что save был вызван 1 раз с любым объектом User
        verifyNoMoreInteractions(userRepository);
    }

    @Test
    @DisplayName("addViolation должен увеличить нарушения и заблокировать пользователя (второе нарушение)")
    void addViolation_WhenUserExistsAndSecondViolation_ShouldIncrementViolationsAndBlock() {
        // Arrange
        testUser = new User();
        testUser.setId(userId);
        testUser.setViolations(1); // Начальное состояние - 1 нарушение
        testUser.setStatusBlock(false); // Еще не заблокирован

        when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));
        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        when(userRepository.save(userCaptor.capture())).thenAnswer(invocation -> invocation.getArgument(0));

        User updatedUser = userService.addViolation(userId);

        assertNotNull(updatedUser);
        assertEquals(2, updatedUser.getViolations());
        assertTrue(updatedUser.getStatusBlock());

        User savedUser = userCaptor.getValue();
        assertNotNull(savedUser);
        assertEquals(2, savedUser.getViolations());
        assertTrue(savedUser.getStatusBlock());


        verify(userRepository, times(1)).findById(userId);
        verify(userRepository, times(1)).save(any(User.class));
        verifyNoMoreInteractions(userRepository);
    }

    @Test
    @DisplayName("addViolation должен выбросить NotFoundUser, если пользователь не найден")
    void addViolation_WhenUserNotFound_ShouldThrowNotFoundUserException() {
        when(userRepository.findById(nonExistentUserId)).thenReturn(Optional.empty());


        assertThrows(NotFoundUser.class, () -> {
            userService.addViolation(nonExistentUserId);
        });

        verify(userRepository, times(1)).findById(nonExistentUserId);
        verify(userRepository, never()).save(any(User.class));
        verifyNoMoreInteractions(userRepository);
    }
}