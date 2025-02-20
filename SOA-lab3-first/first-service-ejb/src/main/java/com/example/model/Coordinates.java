package com.example.model;

import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import java.io.Serializable;
import javax.validation.constraints.*;

@Getter
@Setter
@NoArgsConstructor
public class Coordinates implements Serializable {
    private static final long serialVersionUID = 1L;

    @Max(value = 775, message = "X должно быть меньше или равно 775")
    private int x;
    
    @NotNull(message = "Y не может быть null")
    @Min(value = -531, message = "Y должно быть больше -531")
    private Double y;
} 