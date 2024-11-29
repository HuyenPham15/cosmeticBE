package com.cosmetics.restcontroller;

import com.cosmetics.dto.LoginRequest;
import com.cosmetics.dto.PasswordResetDTO;
import com.cosmetics.dto.SignupDTO;
import com.cosmetics.dto.VerificationRequestDTO;
import com.cosmetics.model.Authorities;
import com.cosmetics.model.Employee;
import com.cosmetics.model.Users;
import com.cosmetics.model.Roles;
import com.cosmetics.repository.AuthoritiesRepository;
import com.cosmetics.repository.EmployeeRepository;
import com.cosmetics.repository.RoleRepository;
import com.cosmetics.repository.UserRepository;
import com.cosmetics.service.AESUtil;
import com.cosmetics.service.EmailService;
import com.cosmetics.service.EmployeeService;
import com.cosmetics.service.UserService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.*;

import javax.crypto.SecretKey;
import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/auth")
@CrossOrigin(origins = "*")
        public class AuthController {
    private static final Logger logger = LoggerFactory.getLogger(AuthController.class);

    private final AuthenticationManager authenticationManager;
    private final UserService userService;
    @Autowired
    private EmailService emailService;
    @Autowired
    private RoleRepository roleRepository;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private EmployeeRepository employeeRepository;
    @Autowired
    private EmployeeService employeeService;
    @Autowired
    private AuthoritiesRepository authoritiesRepository;

    @Autowired
    public AuthController(AuthenticationManager authenticationManager, UserService userService) {
        this.authenticationManager = authenticationManager;
        this.userService = userService;
    }

    public boolean isValidEmail(String email) {
        String emailRegex = "^[\\w-\\.]+@([\\w-]+\\.)+[\\w-]{2,4}$"; 
        return email != null && Pattern.matches(emailRegex, email.trim());
    }

    @PostMapping("/register")
    public ResponseEntity<?> registerUser(@RequestBody SignupDTO signupDTO) {
        // Kiểm tra nếu email đã tồn tại
        if (userService.findByEmail(signupDTO.getEmail()).isPresent()) {
            return ResponseEntity.badRequest().body("Email đã tồn tại");
        }

        // Kiểm tra mật khẩu và xác nhận mật khẩu có khớp nhau không
        if (!signupDTO.getPassword().equals(signupDTO.getConfirmPassword())) {
            return ResponseEntity.badRequest().body("Mật khẩu và xác nhận mật khẩu không khớp");
        }

        Users user = new Users();
        user.setFirstName(signupDTO.getFirst_name());
        user.setLastName(signupDTO.getLast_name());
        user.setEmail(signupDTO.getEmail());
        user.setPassword(signupDTO.getPassword());
        if (user.getAuthorities() == null) {
            user.setAuthorities(new ArrayList<>()); // Khởi tạo danh sách rỗng nếu chưa có
        }

        // Lấy vai trò từ cơ sở dữ liệu
        Roles role = roleRepository.findById("R03")
                .orElseThrow(() -> new RuntimeException("Role not found"));

        // Tạo đối tượng authorities
        Authorities authority = new Authorities();
        authority.setUsers(user);
        authority.setRole(role);
        authority.setEmployee(null);

        user.getAuthorities().add(authority);
        userService.saveUser(user);

        // Gửi email xác nhận
        emailService.sendVerificationEmail(signupDTO.getEmail());
        return ResponseEntity.ok("Email đã đăng kí thành công. Vui lòng xác nhận tài khoản qua email.");
    }

    @PostMapping("/confirm-registration")
    public ResponseEntity<Map<String, String>> confirmRegistration(@RequestBody Map<String, String> request) {
        String email = request.get("email");
        String code = request.get("code");

        Map<String, String> response = new HashMap<>();
        try {
            // Kiểm tra xem người dùng có tồn tại không
            Optional<Users> userOpt = userRepository.findByEmail(email);
            if (!userOpt.isPresent()) {
                response.put("error", "Người dùng không tồn tại.");
                return ResponseEntity.badRequest().body(response);
            }

            Users user = userOpt.get();
            // Kiểm tra nếu người dùng đã xác nhận hay chưa
            if (user.isVerified()) {
                response.put("error", "Người dùng đã được xác nhận trước đó.");
                return ResponseEntity.badRequest().body(response);
            }

            userRepository.save(user); // Lưu người dùng đã cập nhật
            response.put("message", "Xác nhận tài khoản thành công!");
            return ResponseEntity.ok(response); // Trả về đối tượng JSON
        } catch (RuntimeException e) {
            response.put("error", e.getMessage()); // Trả về thông báo lỗi nếu có
            return ResponseEntity.badRequest().body(response);
        }
    }

//    @PostMapping("/login")
//    public ResponseEntity<?> loginUser(@RequestBody Users user) {
//        Optional<Users> userOptional = userRepository.findByEmail(user.getEmail());
//
//        try {
//            // Kiểm tra số lần nhập sai
//            int failedAttempts = userService.getFailedLoginAttempts(user.getEmail());
//            if (failedAttempts >= 5) {
//                Map<String, String> response = new HashMap<>();
//                response.put("message", "Tài khoản bị khóa do nhập sai quá nhiều. Vui lòng nhập email mới để nhận mã xác nhận khôi phục tài khoản.");
//                return ResponseEntity.status(403).body(response);
//            }
//
//            // Lấy thông tin người dùng từ cơ sở dữ liệu
//            Users userFromDb = userService.findByEmail(user.getEmail())
//                    .orElseThrow(() -> new UsernameNotFoundException("Không tìm thấy người dùng"));
//
//            String rawPassword = user.getPassword().trim();  // Mật khẩu người dùng nhập
//            String storedEncryptedPassword = userFromDb.getPassword();  // Mật khẩu đã mã hóa trong cơ sở dữ liệu
//            String base64Key = userFromDb.getAseKey();  // Lấy khóa AES từ cơ sở dữ liệu
//
//            // Nếu không có khóa AES, so sánh trực tiếp mật khẩu
//            String decryptedPassword = storedEncryptedPassword; // Giả định mật khẩu chưa mã hóa
//            if (base64Key != null && !base64Key.isEmpty()) {
//                try {
//                    SecretKey key = AESUtil.getKeyFromBase64String(base64Key);  // Giải mã khóa AES
//                    decryptedPassword = AESUtil.decrypt(storedEncryptedPassword, key);  // Giải mã mật khẩu
//                } catch (Exception e) {
//                    e.printStackTrace();
//                    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Lỗi giải mã mật khẩu");
//                }
//            }
//
//            // So sánh mật khẩu người dùng nhập với mật khẩu đã giải mã hoặc chưa mã hóa
//            if (rawPassword.equals(decryptedPassword)) {
//                // Đặt lại số lần nhập sai nếu đăng nhập thành công
//                userService.resetFailedLoginAttempts(user.getEmail());
//                user.setVerified(true);
//
//                // Lấy thông tin và vai trò
//                String firstName = userFromDb.getFirstName();
//                String lastName = userFromDb.getLastName();
//                List<Authorities> authorities = userFromDb.getAuthorities();
//                List<String> roles = authorities.stream()
//                        .map(auth -> auth.getRole().getRoleID())
//                        .collect(Collectors.toList());
//
//                // Tạo phản hồi thành công
//                Map<String, Object> response = new HashMap<>();
//                response.put("userID",userFromDb.getUserID());
//                response.put("firstName", firstName);
//                response.put("lastName", lastName);
//                response.put("email", userFromDb.getEmail());
//                response.put("phoneNumber", userFromDb.getPhoneNumber());
//                response.put("address", userFromDb.getAddress());
//                response.put("roles", roles);
//                response.put("isVerified", userFromDb.isVerified());  // Trả về giá trị isVerified
//                response.put("avatar", userFromDb.getAvatar());
//                user.setVerified(true);
//                return ResponseEntity.ok(response);
//            } else {
//                // Mật khẩu không chính xác
//                userService.incrementFailedLoginAttempts(user.getEmail());
//                return ResponseEntity.status(401).body("Mật khẩu không chính xác");
//            }
//        } catch (UsernameNotFoundException e) {
//            return ResponseEntity.status(401).body("Không tìm thấy người dùng");
//        } catch (Exception e) {
//            e.printStackTrace();
//            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Đã xảy ra lỗi trong quá trình đăng nhập");
//        }
//    }

    @PostMapping("/login")
    public ResponseEntity<?> loginUser(@RequestBody LoginRequest loginRequest) {
        try {
            // Tìm kiếm email trong bảng người dùng và nhân viên
            Optional<Users> userOptional = userRepository.findByEmail(loginRequest.getEmail());
            Optional<Employee> employeeOptional = employeeRepository.findByEmail(loginRequest.getEmail());

            if (userOptional.isPresent()) {
                // Đăng nhập cho người dùng
                Users user = userOptional.get();
                return authenticateUser(user, loginRequest.getPassword());
            } else if (employeeOptional.isPresent()) {
                // Đăng nhập cho nhân viên
                Employee employee = employeeOptional.get();
                return authenticateEmployee(employee, loginRequest.getPassword());
            } else {
                // Nếu không tìm thấy người dùng hoặc nhân viên
                return ResponseEntity.status(401).body("Không tìm thấy người dùng hoặc nhân viên");
            }
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Đã xảy ra lỗi trong quá trình đăng nhập");
        }
    }

    private ResponseEntity<?> authenticateUser(Users user, String rawPassword) {
        try {
            // Kiểm tra nếu tài khoản đã bị khóa
            if (user.isVerified()) {
                return ResponseEntity.status(403).body("Tài khoản đã bị khóa do đăng nhập sai quá nhiều lần.");
            }

            String storedEncryptedPassword = user.getPassword();
            String base64Key = user.getAseKey();
            String decryptedPassword = decryptPassword(storedEncryptedPassword, base64Key);

            if (rawPassword.equals(decryptedPassword)) {
                // Đặt lại số lần đăng nhập sai nếu thành công
                user.setLogin_attempts(0);
                userRepository.save(user);

                Map<String, Object> response = new HashMap<>();
                response.put("userID", user.getUserID());
                response.put("firstName", user.getFirstName());
                response.put("lastName", user.getLastName());
                response.put("email", user.getEmail());
                response.put("phoneNumber", user.getPhoneNumber());
                response.put("address", user.getAddress());
                response.put("roles", getRoles(user.getAuthorities()));
                response.put("isVerified", user.isVerified());
                response.put("avatar", user.getAvatar());
                return ResponseEntity.ok(response);
            } else {
                // Tăng số lần đăng nhập sai
                int attempts = user.getLogin_attempts() + 1;
                user.setLogin_attempts(attempts);

                // Khóa tài khoản nếu quá 5 lần
                if (attempts >= 5) {
                    user.setVerified(true);
                }

                userRepository.save(user);
                return ResponseEntity.status(401).body("Mật khẩu không chính xác");
            }
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Lỗi đăng nhập người dùng");
        }
    }

    private ResponseEntity<?> authenticateEmployee(Employee employee, String rawPassword) {
        try {
            String storedEncryptedPassword = employee.getPassword();
            String base64Key = employee.getAseKey();
            String decryptedPassword = decryptPassword(storedEncryptedPassword, base64Key);

            if (rawPassword.equals(decryptedPassword)) {
                // Đặt lại số lần nhập sai nếu đăng nhập thành công
                employeeService.resetFailedLoginAttempts(employee.getEmail());
                List<Authorities> authorities = authoritiesRepository.findByEmployee_EmployeeID(employee.getEmployeeID());

                if (authorities.isEmpty()) {
                    return ResponseEntity.status(403).body("Nhân viên không có vai trò.");
                }

                Map<String, Object> response = new HashMap<>();
                response.put("employeeID", employee.getEmployeeID());
                response.put("firstName", employee.getFirstName());
                response.put("lastName", employee.getLastName());
                response.put("email", employee.getEmail());
                response.put("phoneNumber", employee.getPhoneNumber());
                response.put("address", employee.getAddress());
                response.put("roles", getRoles(authorities)); // Đảm bảo lấy đúng vai trò
                response.put("isVerified", employee.isVerified());
                response.put("avatar", employee.getAvatar());
                return ResponseEntity.ok(response);
            } else {
                employeeService.incrementFailedLoginAttempts(employee.getEmail());
                return ResponseEntity.status(401).body("Mật khẩu không chính xác");
            }
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Lỗi đăng nhập nhân viên");
        }
    }


    private String decryptPassword(String encryptedPassword, String base64Key) {
        String decryptedPassword = encryptedPassword;  // Giả định mật khẩu chưa mã hóa
        if (base64Key != null && !base64Key.isEmpty()) {
            try {
                SecretKey key = AESUtil.getKeyFromBase64String(base64Key);  // Giải mã khóa AES
                decryptedPassword = AESUtil.decrypt(encryptedPassword, key);  // Giải mã mật khẩu
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return decryptedPassword;
    }

    public List<String> getRoles(List<Authorities> authorities) {
        return authorities.stream()
                .map(auth -> auth.getRole().getRoleID())  // Lấy tên vai trò
                .collect(Collectors.toList());
    }

    @PostMapping("/send-verification-code")
    public ResponseEntity<?> sendVerificationCode(@RequestBody String jsonInput) {
        ObjectMapper objectMapper = new ObjectMapper();
        String email;

        try {
            JsonNode jsonNode = objectMapper.readTree(jsonInput);
            email = jsonNode.get("email").asText().trim(); // Lấy giá trị email từ JSON
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Collections.singletonMap("error", "Định dạng JSON không hợp lệ."));
        }

        // Kiểm tra nếu email hợp lệ
        if (email.isEmpty() || !isValidEmail(email)) {
            return ResponseEntity.badRequest().body(Collections.singletonMap("error", "Email không hợp lệ."));
        }

        Optional<Users> exists = userRepository.findByEmail(email);
        if (exists.isPresent()) {
            // Có thể gửi mã xác nhận mà không cần kiểm tra email đã tồn tại
            String verificationCode = emailService.generateVerificationCode();
            emailService.sendPasswordResetEmail(email, verificationCode);
            return ResponseEntity.ok(Collections.singletonMap("message", "Mã xác nhận đã được gửi đến email của bạn."));
        }

        // Nếu email chưa tồn tại, thông báo cho người dùng
        return ResponseEntity.badRequest().body(Collections.singletonMap("error", "Email không tồn tại."));
    }

    @PostMapping("/confirm-verification-code")
    public ResponseEntity<?> confirmVerificationCode(@RequestBody VerificationRequestDTO request) {
        String email = request.getEmail();
        String code = request.getCode();
        long expirationTime = request.getExpirationTime();

        // In thông tin debug
        System.out.println("Received Email: " + email);
        System.out.println("Received Code: " + code);
        System.out.println("Received Expiration Time: " + expirationTime);
        System.out.println("Current Time: " + System.currentTimeMillis());

        boolean isCodeValid = emailService.validateVerificationCode(request);
        if (!isCodeValid) {
            return ResponseEntity.status(403).body(Collections.singletonMap("error", "Mã xác nhận không hợp lệ hoặc đã hết hạn."));
        }

        // Xử lý nếu mã xác nhận hợp lệ
        return ResponseEntity.ok(Collections.singletonMap("message", "Mã xác nhận thành công."));
    }

    @PostMapping("/change-password")
    public ResponseEntity<?> changePassword(@RequestBody PasswordResetDTO request) {
        // Validate các trường nhập
        if (request.getOldPassword() == null || request.getOldPassword().trim().isEmpty() ||
                request.getNewPassword() == null || request.getNewPassword().trim().isEmpty() ||
                request.getConfirmNewPassword() == null || request.getConfirmNewPassword().trim().isEmpty()) {
            return ResponseEntity.badRequest().body("Tất cả thông tin đều là bắt buộc.");
        }

        // Kiểm tra mật khẩu mới và xác nhận mật khẩu
        if (!request.getNewPassword().equals(request.getConfirmNewPassword())) {
            return ResponseEntity.badRequest().body("Mật khẩu mới và xác nhận mật khẩu không khớp.");
        }

        boolean isUpdated = userService.changePassword(request.getEmail(), request.getOldPassword(), request.getNewPassword());

        if (!isUpdated) {
            return ResponseEntity.badRequest().body("Mật khẩu không hợp lệ");
        }

        return ResponseEntity.ok("Mật khẩu đã được cập nhật thành công.");
    }

    @PostMapping("/reset-password")
    public ResponseEntity<?> resetPassword(@RequestBody PasswordResetDTO request) {
        // Kiểm tra các trường bắt buộc
        if (request.getEmail() == null || request.getEmail().trim().isEmpty() ||
                request.getNewPassword() == null || request.getNewPassword().trim().isEmpty() ||
                request.getConfirmNewPassword() == null || request.getConfirmNewPassword().trim().isEmpty()) {
            return ResponseEntity.badRequest().body("Tất cả thông tin đều là bắt buộc.");
        }

        // Kiểm tra xem mật khẩu mới và xác nhận mật khẩu có khớp không
        if (!request.getNewPassword().equals(request.getConfirmNewPassword())) {
            return ResponseEntity.badRequest().body("Mật khẩu mới và xác nhận mật khẩu không khớp.");
        }

        // Gọi service để đặt lại mật khẩu
        boolean isReset = userService.resetPassword(request.getEmail(), request.getNewPassword());
        if (!isReset) {
            return ResponseEntity.badRequest().body("Email không tồn tại.");
        }
        userService.resetFailedLoginAttempts(request.getEmail());
        return ResponseEntity.ok("Mật khẩu đã được đặt lại thành công.");
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logoutUser() {
        SecurityContextHolder.clearContext();
        return ResponseEntity.ok("Đăng xuất thành công");
    }
}