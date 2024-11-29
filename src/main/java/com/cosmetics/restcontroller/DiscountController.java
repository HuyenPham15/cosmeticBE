package com.cosmetics.restcontroller;

import com.cosmetics.model.Discount;
import com.cosmetics.service.DiscountService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@CrossOrigin("*")
public class DiscountController {

    @Autowired
    private DiscountService discountService;

    @GetMapping("/discounts")
    public ResponseEntity<List<Discount>> getAllDiscounts() {
        List<Discount> discounts = discountService.getAll();
        return ResponseEntity.ok(discounts);
    }
    @GetMapping("/valid")
    public List<Discount> getValidDiscounts() {
        Date currentDate = new Date(); // Lấy ngày hiện tại

        // Lọc các discount còn hạn
        return discountService.getAll().stream()
                .filter(discount -> discount.isActive() &&
                        discount.getStartDate().before(currentDate) &&
                        discount.getEndDate().after(currentDate))
                .collect(Collectors.toList());
    }
}
