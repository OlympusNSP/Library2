package ru.olympusnsp.library.repository;

import org.springframework.data.repository.CrudRepository;
import ru.olympusnsp.library.model.RentalBook;

public interface RentalBookRepository extends CrudRepository<RentalBook, Long> {
}
