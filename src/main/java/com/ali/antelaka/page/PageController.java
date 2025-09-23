package com.ali.antelaka.page;

import com.ali.antelaka.ApiResponse;
import com.ali.antelaka.page.entity.PageEntity;
import com.ali.antelaka.page.request.CreatePageBody;
import com.ali.antelaka.user.entity.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;


@RequestMapping("/users")
@PreAuthorize("hasAnyRole('ADMIN', 'USER')")
public class PageController {

    @Autowired
    private PageService pageService ;

    @PostMapping("/create")
    public ResponseEntity<ApiResponse<PageEntity>>  createPage(
            @RequestBody CreatePageBody createPageBody ,
            Principal connectedUser
    ) {
        var user = (User) ((UsernamePasswordAuthenticationToken) connectedUser).getPrincipal();
     //   this.pageService.createPage(createPageBody , user );
        return null ;
    }

    @GetMapping("/{userId}/pages")
    public ResponseEntity<ApiResponse<List<PageEntity>>> getUserPages(
            @PathVariable Integer userId
    ) {
      //  pageService.getUserPages(userId) ;
        return null ;
    }

    // ✅ جلب صفحة واحدة حسب ID
    @GetMapping("/page/{pageId}")
    public ResponseEntity<PageEntity> getPageById(
            @PathVariable Integer pageId
    ) {
        return ResponseEntity.ok(pageService.getPageById(pageId));
    }

//    // ✅ تعديل بيانات صفحة
//    @PutMapping("/page/{pageId}")
//    public ResponseEntity<PageEntity> updatePage(
//            @PathVariable Integer pageId,
//            @RequestBody PageEntity updatedPage
//    ) {
//        return ResponseEntity.ok(pageService.updatePage(pageId, updatedPage));
//    }

    // ✅ حذف صفحة
//    @DeleteMapping("/page/{pageId}")
//    public ResponseEntity<Void> deletePage(
//            @PathVariable Integer pageId
//    ) {
//        pageService.deletePage(pageId);
//        return ResponseEntity.noContent().build();
//    }
}
