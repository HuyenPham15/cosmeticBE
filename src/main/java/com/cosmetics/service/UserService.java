package com.cosmetics.service;


import com.cosmetics.dto.VerificationRequestDTO;
import com.cosmetics.exception.ResourceNotFoundException;
import com.cosmetics.model.Users;
import com.cosmetics.model.Authorities;
import com.cosmetics.repository.AuthoritiesRepository;
import com.cosmetics.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.util.*;

@Service
public class UserService implements UserDetailsService {
    @Autowired
    private final EmailService emailService; // Thêm EmailService
    private static final Logger logger = LoggerFactory.getLogger(UserService.class);
    private VerificationRequestDTO requestDTO;


    @Autowired
    private UserRepository userRepository;

    @Autowired
    private AuthoritiesRepository authoritiesRepository;

    public UserService(EmailService emailService) {
        this.emailService = emailService;
    }
    public List<Users> getAllUsers() {
        return userRepository.findAll();
    }
    public boolean changePassword(String email, String oldPassword, String newPassword) {
        // Tìm kiếm người dùng theo email
        Optional<Users> userOpt = userRepository.findByEmail(email);
        if (userOpt.isPresent()) {
            Users user = userOpt.get();
            System.out.println("Old Password: " + oldPassword);
            System.out.println("Stored Encrypted Password: " + user.getPassword());

            // Lấy khóa AES từ cột aseKey
            String base64Key = user.getAseKey();

            if (base64Key == null || base64Key.isEmpty()) {
                // Nếu không có khóa AES, xử lý mật khẩu theo cách thông thường
                System.out.println("Không có khóa AES, so sánh mật khẩu bình thường.");

                // So sánh mật khẩu cũ với mật khẩu không mã hóa bằng AES
                if (oldPassword.equals(user.getPassword())) {
                    // Cập nhật mật khẩu mới không mã hóa bằng AES
                    user.setPassword(newPassword);  // Lưu mật khẩu mới vào đối tượng user
                    userRepository.save(user);  // Lưu người dùng
                    return true;  // Cập nhật mật khẩu thành công
                } else {
                    System.out.println("Mật khẩu cũ không đúng");
                    return false;
                }
            } else {
                // Nếu khóa AES tồn tại, xử lý mã hóa/giải mã AES
                try {
                    SecretKey key = AESUtil.getKeyFromBase64String(base64Key); // Lấy khóa AES từ chuỗi Base64

                    // Giải mã mật khẩu đã lưu trong cơ sở dữ liệu
                    String decryptedPassword = AESUtil.decrypt(user.getPassword(), key);
                    System.out.println("Decrypted Password: " + decryptedPassword);

                    // So sánh mật khẩu cũ do người dùng nhập với mật khẩu đã giải mã
                    if (oldPassword.equals(decryptedPassword)) {
                        // Mã hóa mật khẩu mới bằng AES
                        String encryptedNewPassword = AESUtil.encrypt(newPassword, key);
                        user.setPassword(encryptedNewPassword); // Lưu mật khẩu đã mã hóa
                        System.out.println("New Encrypted Password: " + encryptedNewPassword);

                        // Lưu người dùng với mật khẩu mới vào cơ sở dữ liệu
                        userRepository.save(user);
                        return true; // Cập nhật mật khẩu thành công
                    } else {
                        System.out.println("Mật khẩu cũ không đúng");
                        return false;
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    System.out.println("Lỗi khi xử lý mật khẩu");
                    return false;
                }
            }
        } else {
            // Người dùng không tồn tại
            System.out.println("Email không tồn tại");
            return false;
        }
    }

    private SecretKey getAESKey() {
        Users user = userRepository.findByEmail(requestDTO.getEmail()).get();
        String base64Key = user.getAseKey();
        return AESUtil.getKeyFromBase64String(base64Key);
    }

    public boolean resetPassword(String email, String newPassword) {
        Optional<Users> userOpt = userRepository.findByEmail(email);
        if (userOpt.isPresent()) {
            Users user = userOpt.get();
            try {
                SecretKey aesKey = getAESKey();
                // Mã hóa mật khẩu mới
                String encryptedPassword = AESUtil.encrypt(newPassword, aesKey);
                user.setPassword(encryptedPassword); // Lưu mật khẩu đã mã hóa
                userRepository.save(user);
                return true;
            } catch (Exception e) {
                // Xử lý các ngoại lệ liên quan đến mã hóa
                System.err.println("Lỗi khi mã hóa mật khẩu: " + e.getMessage());
                return false; // Mã hóa thất bại
            }
        }
        return false; // Email không tồn tại
    }

    public Users saveUser(Users user) {
        return userRepository.save(user);
    }

    public Optional<Users> findByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    public void confirmUser(String email, String code) {
        // Kiểm tra mã xác nhận
        if (emailService.validateVerificationCode(requestDTO)) { // Bạn cần truyền email và code vào để validate
            Optional<Users> userOptional = userRepository.findByEmail(email);
            if (userOptional.isPresent()) {
                Users user = userOptional.get();
                // Kiểm tra nếu người dùng đã xác nhận hay chưa
                if (user.isVerified()) {
                    throw new RuntimeException("Người dùng đã được xác nhận trước đó.");
                }

                user.setVerified(true); // Đặt trạng thái xác nhận
                System.out.println("User verified status after update: " + user.isVerified());

                userRepository.save(user); // Lưu người dùng đã cập nhật
                logger.info("Người dùng đã xác nhận thành công: " + email);
            } else {
                throw new RuntimeException("Người dùng không tồn tại.");
            }
        } else {
            throw new RuntimeException("Mã xác nhận không hợp lệ.");
        }
    }

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        // Lấy người dùng từ cơ sở dữ liệu
        Users user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException(email + " không tìm thấy"));

        // Lấy các quyền của người dùng từ cơ sở dữ liệu
        String[] roles = user.getAuthorities().stream()
                .map(authority -> authority.getRole().getRoleName()) // Lấy tên vai trò
                .toArray(String[]::new);

        return User.withUsername(user.getEmail()) // Sử dụng email làm tên người dùng
                .password(user.getPassword()) // Mật khẩu đã mã hóa
                .roles(roles) // Vai trò
                .accountExpired(false) // Nếu tài khoản chưa hết hạn
                .accountLocked(false) // Nếu tài khoản chưa bị khóa
                .credentialsExpired(false) // Nếu thông tin đăng nhập chưa hết hạn
                .disabled(!user.isVerified()) // Nếu người dùng chưa xác nhận
                .build();
    }

    // Phương thức reset số lần đăng nhập sai về 0
    public void resetFailedLoginAttempts(String email) {
        Users user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("Không tìm thấy người dùng"));
        user.setLogin_attempts(0); // Đặt số lần đăng nhập sai về 0
        userRepository.save(user); // Cập nhật người dùng vào database
    }

    // Phương thức tăng số lần đăng nhập sai
    public void incrementFailedLoginAttempts(String email) {
        Users user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("Không tìm thấy người dùng"));
        int attempts = user.getLogin_attempts() + 1;
        user.setLogin_attempts(attempts); // Tăng số lần đăng nhập sai
        userRepository.save(user); // Lưu cập nhật vào database
    }

    // Phương thức lấy số lần đăng nhập sai
    public int getFailedLoginAttempts(String email) {
        Users user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("Không tìm thấy người dùng"));
        return user.getLogin_attempts(); // Trả về số lần đăng nhập sai
    }


    // Cập nhật quyền cho người dùng
    public void updateUserAuthorities(int userId, List<Authorities> newAuthorities) {
        Users user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found")); // Xử lý lỗi nếu không tìm thấy người dùng

        // Xóa quyền cũ (chỉ cần clear danh sách authorities của người dùng)
        user.getAuthorities().clear();

        // Liên kết quyền mới với người dùng
        for (Authorities authority : newAuthorities) {
            authority.setUsers(user); // Gán quyền cho người dùng
        }

        // Thêm quyền mới
        user.getAuthorities().addAll(newAuthorities);

        // Lưu lại thay đổi
        userRepository.save(user);
    }


    public Users findByUserId(int userID) {
        return userRepository.findById(userID)
                .orElseThrow(() -> new ResourceNotFoundException("User  not found with ID: " + userID));
    }

}
