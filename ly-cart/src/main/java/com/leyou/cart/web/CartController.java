package com.leyou.cart.web;

import com.leyou.cart.entity.Cart;
import com.leyou.cart.service.CartService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
public class CartController {

    @Autowired
    private CartService cartService;

    /**
     * 添加商品到购物车
     * @param cart
     * @return
     */
    @PostMapping
    public ResponseEntity<Void> addCart(@RequestBody Cart cart) {
        cartService.addCart(cart);
        return ResponseEntity.ok().build();
    }

    /**
     * 查询购物车列表
     * @return
     */
    @GetMapping("list")
    public ResponseEntity<List<Cart>> queryCartList() {
        List<Cart> list = cartService.queryCartList();
        if (CollectionUtils.isEmpty(list)) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
        return ResponseEntity.ok(list);
    }

    /**
     * 修改购物车商品的数量
     * @param skuId
     * @param num
     * @return
     */
    @PutMapping
    public ResponseEntity<Void> updateNum(@RequestParam("id") Long skuId, @RequestParam("num") Integer num) {
        cartService.updateNum(skuId, num);
        return ResponseEntity.ok().build();
    }

    /**
     *
     * 删除购物车商品
     *
     * @param skuId
     * @return
     */
    @DeleteMapping("{skuId}")
    public ResponseEntity<Void> deleteCart(@PathVariable("skuId") String skuId) {
        cartService.deleteCart(skuId);
        return ResponseEntity.ok().build();

    }

    /**
     * 批量新增到购物车
     * @param cartList
     * @return
     */
    @PostMapping("list")
    public ResponseEntity<Void> addCartList(@RequestBody List<Cart> cartList) {
        cartService.addCartList(cartList);
        return ResponseEntity.ok().build();
    }


}
