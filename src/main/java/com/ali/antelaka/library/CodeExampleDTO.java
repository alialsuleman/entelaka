package com.ali.antelaka.library;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CodeExampleDTO {

    private String language;
    private String title;
    private String description;
    private String code;
}
