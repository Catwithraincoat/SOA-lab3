package com.example.model;

import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import java.io.Serializable;
import javax.validation.constraints.*;

@Getter
@Setter
@NoArgsConstructor
public class Person implements Serializable {
    private static final long serialVersionUID = 1L;

    private Long id;

    @NotNull(message = "Имя не может быть null")
    @NotEmpty(message = "Имя не может быть пустым")
    private String name;
    
    @Positive(message = "Рост должен быть больше 0")
    private double height;
    
    @NotNull(message = "Вес не может быть null")
    @Positive(message = "Вес должен быть больше 0")
    private Double weight;
    
    @Size(min = 5, max = 45, message = "Длина PassportID должна быть между 5 и 45 символами")
    @NotEmpty(message = "PassportID не может быть пустым")
    private String passportID;
    
    private Location location;
} 