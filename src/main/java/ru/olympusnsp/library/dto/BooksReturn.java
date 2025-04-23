package ru.olympusnsp.library.dto;

import lombok.*;

import java.util.List;

@NoArgsConstructor
@AllArgsConstructor
@Builder
@Setter
@Getter
public class BooksReturn {
    Integer user_id;
    Integer book_id;
}
