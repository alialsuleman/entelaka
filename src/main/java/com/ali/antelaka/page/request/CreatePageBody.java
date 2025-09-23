package com.ali.antelaka.page.request;

import lombok.Data;

@Data
public class CreatePageBody {
    private String tag ;
    private String description ;
    private Integer   user_id ;

}
