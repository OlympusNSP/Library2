package ru.olympusnsp.library.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.util.List;

@NoArgsConstructor
@AllArgsConstructor
@Builder
@Setter
@Getter
public class OrderCreate {
    @NotNull
    Integer user_id;
    @NotEmpty
    List<@NotNull Integer> book_ids;
}
