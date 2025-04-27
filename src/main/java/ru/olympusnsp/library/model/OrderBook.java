package ru.olympusnsp.library.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.ColumnDefault;

import java.time.LocalDate;

@Getter
@Setter
@Entity
@Table(name = "order_book")
@NoArgsConstructor
@AllArgsConstructor
public class OrderBook {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id")
    @JsonIgnore
    private Order order;

    @NotNull
    @Column(name = "status", nullable = false)
    @Enumerated(EnumType.ORDINAL)
    private OrderBookStatus status;

    @Column(name = "date_start_rented_book")
    private LocalDate dateStartRentedBook;

    @Column(name = "date_return_upto")
    private LocalDate dateReturnUpto;

    @Column(name = "date_returned_book")
    private LocalDate dateReturnedBook;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "book_id", nullable = false)
    private Book book;

    public enum OrderBookStatus{
        CREATED, PREPARED, RENTED, RETURNED, LOSSLIBRARY, LOSSUSER, CANCELLED
    }
}