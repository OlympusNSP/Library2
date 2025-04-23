package ru.olympusnsp.library.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.util.List;

@Getter
@Setter
@Entity
@Table(name = "order_")
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Order {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    @JsonIgnore
    private ru.olympusnsp.library.model.User user;

    @Column(name = "manager_id")
    private Integer managerId;

    @NotNull
    @Column(name = "status", nullable = false)
    private OrderStatus status;

    @ManyToMany
    @JoinTable(name = "order_book",
            joinColumns = @JoinColumn(name = "order_id"),
            inverseJoinColumns = @JoinColumn(name = "book_id"))
    private List<Book> reservedBooks;


    public enum OrderStatus {
        READY, COMPLETED, CANCELLED
    }

}