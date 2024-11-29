package com.cosmetics.service;

import com.cosmetics.model.Discount;
import com.cosmetics.repository.DiscountRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class DiscountService {

    @Autowired
    private DiscountRepository discountRepository;

    public List<Discount> getAll() {
        return discountRepository.findAll(); // Lấy tất cả các bản ghi từ bảng Discount
    }
    public Optional<Discount> getDiscountById(String id) {
        return discountRepository.findById(id);
    }
    public  Discount addDiscount(Discount discount) {
        String newId = generateNewDiscountId();  // Gọi hàm để tạo discount_id mới
        discount.setDiscountID(newId);          // Set discount_id mới vào đối tượng discount
        return discountRepository.save(discount);       // Lưu đối tượng discount vào cơ sở dữ liệu
    }
    public  Discount updateDiscount(String id, Discount newDiscount) {
        try {
            return discountRepository.findById(id)
                    .map(discount -> {
                        discount.setDiscountCode(newDiscount.getDiscountCode());
                        discount.setDescription(newDiscount.getDescription());
                        discount.setDiscountType(newDiscount.getDiscountType());
                        discount.setAmount(newDiscount.getAmount());
                        discount.setStartDate(newDiscount.getStartDate());
                        discount.setEndDate(newDiscount.getEndDate());
                        discount.setActive(newDiscount.isActive());
                        discount.setMaxUsage(newDiscount.getMaxUsage());
                        discount.setUsageCount(newDiscount.getUsageCount());
                        return discountRepository.save(discount);
                    })
                    .orElseGet(() -> {
                        newDiscount.setDiscountID(id);
                        return discountRepository.save(newDiscount);
                    });
        } catch (Exception e) {
            // Log lỗi và xử lý
            throw new RuntimeException("Error updating discount: " + e.getMessage(), e);
        }
    }
    private  String generateNewDiscountId() {
        // Lấy discount_id lớn nhất từ cơ sở dữ liệu
        List<Discount> discounts = discountRepository.findAll();
        String lastId = discounts.stream()
                .map(Discount::getDiscountID)
                .max(String::compareTo)
                .orElse("DIS001"); // Nếu chưa có discount nào thì bắt đầu từ DIS000

        // Cắt bỏ phần "DIS" và chuyển phần số sang kiểu integer
        int number = Integer.parseInt(lastId.substring(3));

        // Tăng số lên 1 và tạo ra discount_id mới
        String newId = String.format("DIS%03d", number + 1);

        return newId;
    }
}