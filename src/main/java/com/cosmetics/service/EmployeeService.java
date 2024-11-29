package com.cosmetics.service;

import com.cosmetics.model.Authorities;
import com.cosmetics.model.Employee;
import com.cosmetics.model.Users;
import com.cosmetics.repository.EmployeeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class EmployeeService {
    @Autowired
    private EmployeeRepository employeeRepository;

    public void resetFailedLoginAttempts(String email) {
        Employee employee = employeeRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("Không tìm thấy người dùng"));
        employee.setLogin_attempts(0); // Đặt số lần đăng nhập sai về 0
        employeeRepository.save(employee); // Cập nhật người dùng vào database
    }

    // Phương thức tăng số lần đăng nhập sai
    public void incrementFailedLoginAttempts(String email) {
        Employee user = employeeRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("Không tìm thấy người dùng"));
        int attempts = user.getLogin_attempts() + 1;
        user.setLogin_attempts(attempts); // Tăng số lần đăng nhập sai
        employeeRepository.save(user); // Lưu cập nhật vào database
    }



    public void updateEmployeeAuthorities(int employeeId, List<Authorities> authorities) {
    }
}
