package com.example.model;

import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import javax.validation.constraints.*;

import java.io.Serializable;

@Getter
@Setter
@NoArgsConstructor
public class Location implements Serializable {
    private static final long serialVersionUID = 1L;

    private float x;
    
    @NotNull(message = "Y не может быть null")
    private Float y;
    
    private String name;

} 