package ru.olympusnsp.library.dto;



import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.util.List;

@NoArgsConstructor
@AllArgsConstructor
@Builder
@Setter
@Getter
public class BooksReturn {

    @NotNull
    Integer user_id;

    @NotNull
    Integer book_id;
}
