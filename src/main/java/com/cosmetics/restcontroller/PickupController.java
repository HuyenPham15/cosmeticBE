package com.cosmetics.restcontroller;

import com.cosmetics.dto.Shift;
import com.cosmetics.service.PickupService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/ghn")
@CrossOrigin("*")
public class PickupController {

    @Autowired
    private PickupService pickupService;

    @GetMapping("/shifts")
    public List<Shift> getShifts(@RequestParam("date") long date) {
        // Gọi service để lấy danh sách ca lấy hàng từ API GHN
        List<Shift> shifts = pickupService.getShifts(date);

        // Trả về danh sách ca lấy hàng dưới dạng JSON
        return shifts;
    }
}