package ru.olympusnsp.library.dto;

import lombok.*;

import java.util.List;

@NoArgsConstructor
@AllArgsConstructor
@Setter
@Getter
public class ErrorList {
    private List<String> errors;
    private Integer status;
}
