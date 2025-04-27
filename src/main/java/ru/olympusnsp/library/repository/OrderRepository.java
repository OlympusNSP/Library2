package ru.olympusnsp.library.repository;

import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.stereotype.Repository;
import ru.olympusnsp.library.model.Order;

import java.util.Optional;

@Repository
public interface OrderRepository extends PagingAndSortingRepository<Order, Long>,CrudRepository<Order,Long> {
    Optional<Order> findById(Long orderId);
}
