package com.ali.antelaka.page.DTO;


import com.ali.antelaka.page.entity.PageEntity;
import com.ali.antelaka.user.entity.User;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Data;

@Data
public class PageDTO {

    private Integer id;
    private String tag ;
    private String pageType ;
    private String description ;
    private Integer user_id ;

    public PageDTO ()
    {
    }


    public PageDTO (PageEntity pageEntity)
    {
        this.id = pageEntity.getId() ;
        this.user_id = pageEntity.getUser().getId() ;
        this.tag =  pageEntity.getTag() ;
        this.description= pageEntity.getDescription() ;
        this.pageType = pageEntity.getPageType() ;
    }

}
