package ru.olympusnsp.library.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import ru.olympusnsp.library.model.OrderBook;

@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class OrderBookChangeRequest {

    Long orderBookId;
    OrderBook.OrderBookStatus status;
}
