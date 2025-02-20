package com.example.model;

import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import java.io.Serializable;
import java.time.Instant;
import javax.validation.constraints.*;
import javax.validation.Valid;

@Getter
@Setter
@NoArgsConstructor
public class Movie implements Serializable {
    private static final long serialVersionUID = 1L;

    private Long id;

    @NotNull(message = "Название не может быть null")
    @NotEmpty(message = "Название не может быть пустым")
    private String name;
    
    @NotNull(message = "Координаты не могут быть null")
    @Valid
    private Coordinates coordinates;
    
    private Instant creationDate;
    
    @Positive(message = "Это поле должно быть больше 0, поле может быть null")
    private Long oscarsCount;
    
    private MovieGenre genre;
    private MpaaRating mpaaRating;
    
    @NotNull(message = "Имя режиссера не может быть null")
    @NotEmpty(message = "Имя режиссера не может быть пустым")
    private String directorName;
    
    private Person director;
} 