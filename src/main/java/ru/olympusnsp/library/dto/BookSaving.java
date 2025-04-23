package ru.olympusnsp.library.dto;

import jakarta.persistence.Column;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.SequenceGenerator;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.util.List;

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BookSaving {

    private Long id;

    @Size(min = 3, max = 255)
    private String title;
    private Short year;
    @Size(min = 10)
    private String description;
    @NotNull
    private Integer count;
    private List<Integer> authorsId;
    private List<Integer> genresId;



}
