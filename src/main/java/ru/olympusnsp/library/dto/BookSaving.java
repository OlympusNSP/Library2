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
    private String title;
    private Short year;
    private String description;
    private Integer count;
    private List<Integer> authorsId;
    private List<Integer> genresId;



}
