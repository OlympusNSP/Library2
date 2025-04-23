
package ru.olympusnsp.library.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import ru.olympusnsp.library.dto.BookSaving;
import ru.olympusnsp.library.exeption.NotFoundEntity;
import ru.olympusnsp.library.model.Author;
import ru.olympusnsp.library.model.Book;
import ru.olympusnsp.library.model.Genre;
import ru.olympusnsp.library.repository.BookRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class) // Инициализация Mockito
class BookServiceImplTest {

    // Моки зависимостей
    @Mock
    private BookRepository bookRepository;
    @Mock
    private AuthorService authorService;
    @Mock
    private GenreService genreService;

    // Инжектируем моки в тестируемый сервис
    @InjectMocks
    private BookServiceImpl bookService;

    // ArgumentCaptor для захвата аргументов, передаваемых в моки
    @Captor
    private ArgumentCaptor<Book> bookArgumentCaptor;
    @Captor
    private ArgumentCaptor<Integer> integerArgumentCaptor;

    private Book testBook1;
    private Book testBook2;
    private Author testAuthor1;
    private Genre testGenre1;
    private Pageable pageable;

    @BeforeEach
    void setUp() {
        // Настройка общих тестовых данных
        testAuthor1 = Author.builder().fullname("Author Name").id(2).build();
  ;
        testGenre1 = new Genre();
        testGenre1.setId(1);
        testGenre1.setText("Genre Name");

        testBook1 = Book.builder().id(1).title("Test Book 1").description("Description 1").count(5).available(5).reserve(0).build();
        testBook2 = Book.builder().id(2).title("Another Book").description("Description 2").count(3).available(2).reserve(1).build();
        pageable = PageRequest.of(0, 10); // Стандартная пагинация для тестов
    }

    @Test
    @DisplayName("findAll - должен вернуть страницу книг")
    void findAll_ShouldReturnPageOfBooks() {
        // Arrange (Подготовка)
        List<Book> books = List.of(testBook1, testBook2);
        Page<Book> bookPage = new PageImpl<>(books, pageable, books.size());
        when(bookRepository.findAll(pageable)).thenReturn(bookPage); // Мокируем ответ репозитория

        // Act (Действие)
        Page<Book> result = bookService.findAll(pageable);

        // Assert (Проверка)
        assertNotNull(result);
        assertEquals(2, result.getTotalElements());
        assertEquals(2, result.getContent().size());
        assertEquals(testBook1.getTitle(), result.getContent().get(0).getTitle());
        verify(bookRepository, times(1)).findAll(pageable); // Проверяем, что метод репозитория был вызван 1 раз
    }

    @Test
    @DisplayName("findAllByTitleContains - должен вернуть страницу книг по части названия")
    void findAllByTitleContains_ShouldReturnPageOfBooksMatchingTitle() {
        // Arrange
        String titleQuery = "Test";
        List<Book> books = List.of(testBook1);
        Page<Book> bookPage = new PageImpl<>(books, pageable, books.size());
        when(bookRepository.findAllByTitleContaining(titleQuery, pageable)).thenReturn(bookPage);

        // Act
        Page<Book> result = bookService.findAllByTitleContains(titleQuery, pageable);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        assertEquals(testBook1.getTitle(), result.getContent().get(0).getTitle());
        verify(bookRepository, times(1)).findAllByTitleContaining(titleQuery, pageable);
    }

    @Test
    @DisplayName("findById - должен вернуть книгу, если она найдена")
    void findById_ShouldReturnBook_WhenFound() {
        // Arrange
        int bookId = 1;
        when(bookRepository.findById(bookId)).thenReturn(Optional.of(testBook1));

        // Act
        Book result = bookService.findById(bookId);

        // Assert
        assertNotNull(result);
        assertEquals(bookId, result.getId());
        assertEquals(testBook1.getTitle(), result.getTitle());
        verify(bookRepository, times(1)).findById(bookId);
    }

    @Test
    @DisplayName("findById - должен выбросить NotFoundEntity, если книга не найдена")
    void findById_ShouldThrowNotFoundEntity_WhenNotFound() {
        // Arrange
        int bookId = 99;
        when(bookRepository.findById(bookId)).thenReturn(Optional.empty());

        // Act & Assert
        NotFoundEntity exception = assertThrows(NotFoundEntity.class, () -> {
            bookService.findById(bookId);
        });

        assertEquals("Book with id " + bookId + " not found", exception.getMessage());
        verify(bookRepository, times(1)).findById(bookId);
    }

    @Test
    @DisplayName("save - должен сохранить новую книгу со связями")
    void save_ShouldSaveNewBookWithRelations() {
        // Arrange
        BookSaving bookDTO = new BookSaving();
        bookDTO.setTitle("New Saved Book");
        bookDTO.setDescription("New Description");
        bookDTO.setYear((short)2023);
        bookDTO.setCount(10);
        bookDTO.setAuthorsId(List.of(1));
        bookDTO.setGenresId(List.of(1)); // Обратите внимание на баг в коде: getAuthorsId используется дважды


        when(authorService.findById(1)).thenReturn(testAuthor1);
        when(genreService.findById(1)).thenReturn(testGenre1); // Используем ID автора, как в исходном коде


        when(bookRepository.save(any(Book.class))).thenAnswer(invocation -> {
            Book bookToSave = invocation.getArgument(0);
            if (bookToSave.getId() == null) { // Первый вызов save
                bookToSave.setId(3); // Имитируем присвоение ID
                return bookToSave;
            } else { // Второй вызов save (со связями)
                return bookToSave;
            }
        });

        Book savedBook = bookService.save(bookDTO);

        assertNotNull(savedBook);
        assertNotNull(savedBook.getId()); // Убедимся, что ID присвоен
        assertEquals(bookDTO.getTitle(), savedBook.getTitle());
        assertEquals(bookDTO.getCount(), savedBook.getCount());
        assertEquals(bookDTO.getCount(), savedBook.getAvailable()); // Available = Count
        assertEquals(0, savedBook.getReserve());
        assertNotNull(savedBook.getAuthors());
        assertFalse(savedBook.getAuthors().isEmpty());
        assertEquals(testAuthor1.getFullname(), savedBook.getAuthors().get(0).getFullname());

        assertNotNull(savedBook.getGenres());
        assertFalse(savedBook.getGenres().isEmpty());
        assertEquals(testGenre1.getText(), savedBook.getGenres().get(0).getText()); // Проверка на основе текущей логики

        verify(authorService, times(1)).findById(1);
        verify(genreService, times(1)).findById(1); // Проверяем, что вызывается с ID автора

        verify(bookRepository, times(2)).save(bookArgumentCaptor.capture());
        List<Book> capturedBooks = bookArgumentCaptor.getAllValues();

        // Проверка состояния книги при втором вызове save
        Book secondSaveArg = capturedBooks.get(1);
        assertNotNull(secondSaveArg.getId()); // ID уже есть
        assertNotNull(secondSaveArg.getAuthors()); // Связи добавлены
        assertNotNull(secondSaveArg.getGenres());
        assertEquals(testAuthor1, secondSaveArg.getAuthors().get(0));
        assertEquals(testGenre1, secondSaveArg.getGenres().get(0));
    }

    @Test
    @DisplayName("save - должен сохранить новую книгу без жанров")
    void save_ShouldSaveNewBookWithoutGenres() {

        Integer bookId = 4;
        BookSaving bookDTO = new BookSaving();
        bookDTO.setTitle("Book Without Genres");
        bookDTO.setDescription("Desc");
        bookDTO.setYear((short)2024);
        bookDTO.setCount(5);
        bookDTO.setAuthorsId(List.of(1));
        bookDTO.setGenresId(null); // Нет жанров

        when(authorService.findById(1)).thenReturn(testAuthor1);

        // Мокируем вызовы save
        when(bookRepository.save(any(Book.class))).thenAnswer(invocation -> {
            Book bookToSave = invocation.getArgument(0);
            if (bookToSave.getId() == null) {
                bookToSave.setId(bookId);
                return bookToSave;
            } else {
                return bookToSave;
            }
        });

        // Act
        Book savedBook = bookService.save(bookDTO);

        // Assert
        assertNotNull(savedBook);
        assertEquals("Book Without Genres", savedBook.getTitle());
        assertNotNull(savedBook.getAuthors());
        assertFalse(savedBook.getAuthors().isEmpty());
        assertEquals(testAuthor1, savedBook.getAuthors().get(0));
        assertEquals(bookId, savedBook.getId());
        assertNull(savedBook.getGenres());

        verify(authorService, times(1)).findById(1);
        verify(genreService, never()).findById(anyInt()); // Сервис жанров не должен вызываться
        verify(bookRepository, times(2)).save(any(Book.class));
    }

    @Test
    @DisplayName("reserveBookById - должен успешно зарезервировать доступную книгу")
    void reserveBookById_ShouldReturnTrue_WhenBookAvailable() {
        int bookId = 1;
        int initialAvailable = testBook1.getAvailable();
        int initialReserve = testBook1.getReserve();
        when(bookRepository.findById(bookId)).thenReturn(Optional.of(testBook1));
        // Мокировать save не обязательно, но можно для проверки аргумента
        when(bookRepository.save(any(Book.class))).thenReturn(testBook1);

        Boolean result = bookService.reserveBookById(bookId);

        assertTrue(result);
        verify(bookRepository, times(1)).findById(bookId);
        verify(bookRepository, times(1)).save(bookArgumentCaptor.capture());

        Book savedBook = bookArgumentCaptor.getValue();
        assertEquals(initialAvailable - 1, savedBook.getAvailable());
        assertEquals(initialReserve + 1, savedBook.getReserve());
        assertEquals(bookId, savedBook.getId());
    }

    @Test
    @DisplayName("reserveBookById - должен вернуть false, если книга не найдена")
    void reserveBookById_ShouldReturnFalse_WhenBookNotFound() {
        int bookId = 99;
        when(bookRepository.findById(bookId)).thenReturn(Optional.empty());

        Boolean result = bookService.reserveBookById(bookId);

        assertFalse(result);
        verify(bookRepository, times(1)).findById(bookId);
        verify(bookRepository, never()).save(any(Book.class)); // save не должен вызываться
    }

    @Test
    @DisplayName("reserveBookById - должен вернуть false, если книга недоступна (available = 0)")
    void reserveBookById_ShouldReturnFalse_WhenBookNotAvailable() {
        int bookId = 1;
        testBook1.setAvailable(0); // Устанавливаем, что книга недоступна
        when(bookRepository.findById(bookId)).thenReturn(Optional.of(testBook1));

        Boolean result = bookService.reserveBookById(bookId);

        assertFalse(result);
        verify(bookRepository, times(1)).findById(bookId);
        verify(bookRepository, never()).save(any(Book.class)); // save не должен вызываться
    }

    @Test
    @DisplayName("deleteById - должен удалить книгу, если она существует")
    void deleteById_ShouldDeleteBook_WhenExists() {

        int bookId = 1;
        when(bookRepository.existsById(bookId)).thenReturn(true);
        doNothing().when(bookRepository).deleteById(bookId);

        bookService.deleteById(bookId);

        verify(bookRepository, times(1)).existsById(bookId);
        verify(bookRepository, times(1)).deleteById(bookId); // Проверяем, что deleteById был вызван
    }

    @Test
    @DisplayName("deleteById - должен выбросить NotFoundEntity, если книга не существует")
    void deleteById_ShouldThrowNotFoundEntity_WhenNotExists() {

        int bookId = 99;
        when(bookRepository.existsById(bookId)).thenReturn(false);

        assertThrows(NotFoundEntity.class, () -> {
            bookService.deleteById(bookId);
        });

        verify(bookRepository, times(1)).existsById(bookId);
        verify(bookRepository, never()).deleteById(anyInt()); // deleteById не должен вызываться
    }
}
