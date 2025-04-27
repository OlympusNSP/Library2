package ru.olympusnsp.library.dto;

import lombok.Getter;
import lombok.Setter;
import ru.olympusnsp.library.model.OrderBook;

@Getter
@Setter
public class OrderBookStatusDTO {
    OrderBook.OrderBookStatus status;
}
