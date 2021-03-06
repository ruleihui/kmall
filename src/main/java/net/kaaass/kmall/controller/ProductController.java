package net.kaaass.kmall.controller;

import net.kaaass.kmall.controller.request.ProductAddRequest;
import net.kaaass.kmall.controller.response.ProductCommentResponse;
import net.kaaass.kmall.dto.CommentDto;
import net.kaaass.kmall.dto.ProductDto;
import net.kaaass.kmall.exception.InternalErrorExeption;
import net.kaaass.kmall.exception.NotFoundException;
import net.kaaass.kmall.service.ProductService;
import net.kaaass.kmall.vo.CommentVo;
import net.kaaass.kmall.vo.ProductExtraVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/product")
@PreAuthorize("permitAll()")
public class ProductController extends BaseController {

    @Autowired
    private ProductService productService;

    @PostMapping("/")
    @PreAuthorize("hasRole('ADMIN')")
    public ProductDto addProduct(@RequestBody ProductAddRequest productDto) {
        // TODO 输入校验
        return productService.addProduct(productDto).orElseThrow();
    }

    @PostMapping("/{id}/")
    @PreAuthorize("hasRole('ADMIN')")
    public ProductDto editProduct(@PathVariable String id, @RequestBody ProductAddRequest productDto) throws NotFoundException, InternalErrorExeption {
        // TODO 输入校验
        return productService.editProduct(id, productDto);
    }

    @DeleteMapping("/{id}/")
    @PreAuthorize("hasRole('ADMIN')")
    public void removeProduct(@PathVariable String id) {
        productService.removeProduct(id);
    }

    @GetMapping("/{id}/")
    public ProductDto getProductById(@PathVariable String id) throws NotFoundException {
        return productService.getById(id);
    }

    @GetMapping("/{id}/extra/")
    public ProductExtraVo getExtraById(@PathVariable String id, @RequestParam int count) throws NotFoundException {
        String uid = null;
        try {
            uid = getUid();
        } catch (Exception ignored) {
        }
        return productService.getExtraById(id, count, uid);
    }

    @GetMapping("/")
    public List<ProductDto> getAllProducts(Pageable pageable) {
        return productService.getAll(pageable);
    }

    @GetMapping("/index/")
    public List<ProductDto> getIndexItems() {
        return productService.getIndexItems();
    }

    @GetMapping("/quick/")
    public List<ProductDto> getQuickBuyItems() {
        return productService.getQuickBuyItems();
    }

    @GetMapping("/search/")
    public List<ProductDto> search(@RequestParam String keyword, Pageable pageable) {
        return productService.search(keyword, pageable);
    }

    @GetMapping("/category/{categoryId}/")
    public List<ProductDto> getAllProductsByCategory(@PathVariable String categoryId, Pageable pageable) throws NotFoundException {
        return productService.getAllByCategory(categoryId, pageable);
    }

    @GetMapping("/{id}/comments/")
    public ProductCommentResponse getComments(@PathVariable String id, Pageable pageable) {
        return productService.getComments(id, pageable);
    }
}
