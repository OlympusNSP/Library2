package ru.olympusnsp.library.repository;

import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.PagingAndSortingRepository;
import ru.olympusnsp.library.model.Author;
import ru.olympusnsp.library.model.OrderBook;

public interface OrderBookRepository extends PagingAndSortingRepository<OrderBook, Long>, CrudRepository<OrderBook,Long> {

}
