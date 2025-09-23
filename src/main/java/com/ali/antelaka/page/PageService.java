package com.ali.antelaka.page;

import com.ali.antelaka.page.DTO.PageDTO;
import com.ali.antelaka.page.entity.PageEntity;
import com.ali.antelaka.page.request.CreatePageBody;
import com.ali.antelaka.user.UserRepository;
import com.ali.antelaka.user.entity.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class PageService {

    @Autowired
    private  PageRepository pageRepository;
    @Autowired
    private UserRepository userRepository;


    public PageEntity getPageById(Integer pageId) {
        return pageRepository.findById(pageId)
                .orElseThrow(() -> new RuntimeException("Page not found"));
    }


}
