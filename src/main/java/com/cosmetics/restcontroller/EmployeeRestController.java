package com.cosmetics.restcontroller;

import com.cosmetics.dto.AddressDTO;
import com.cosmetics.dto.EmployeeDTO;
import com.cosmetics.dto.UserDTO;
import com.cosmetics.exception.ResourceNotFoundException;
import com.cosmetics.model.*;
import com.cosmetics.repository.AuthoritiesRepository;
import com.cosmetics.repository.EmployeeRepository;
import com.cosmetics.repository.RoleRepository;
import com.cosmetics.repository.UserRepository;
import com.cosmetics.service.AESUtil;
import com.cosmetics.service.EmployeeService;
import com.cosmetics.service.FileService;
import com.cosmetics.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import javax.crypto.SecretKey;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@CrossOrigin(origins = "*")
@RestController
@RequestMapping("/rest/employee")
public class EmployeeRestController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private FileService fileService;
    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private UserService userService;
    @Autowired
    private EmployeeRepository employeeRepository;
    @Autowired
    private AuthoritiesRepository authoritiesRepository;
    @Autowired
    private EmployeeService employeeService;

    @GetMapping
    public ResponseEntity<List<EmployeeDTO>> getAll(@RequestParam(required = false) List<String> roles) {
        List<Employee> employees;

        if (roles == null || roles.isEmpty()) {
            employees = employeeRepository.findAll();
        } else {
            employees = employeeRepository.findByAuthorities_Role_RoleIDIn(roles);
        }
//        try {
//            String base64Key = employees.getAseKey();  // Cột 'aseKey' lưu trữ khóa AES ở dạng Base64
//
//            if (base64Key != null && !base64Key.isEmpty()) {
//                SecretKey key = AESUtil.getKeyFromBase64String(base64Key);
//                String decryptedPassword = AESUtil.decrypt(user.getPassword(), key);
//                user.setPassword(decryptedPassword);  // Thiết lập lại mật khẩu đã giải mã
//            }
//        } catch (Exception e) {
//            e.printStackTrace();
//            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
//                    .body(null);
//        }
        // Chuyển đổi danh sách Employee sang EmployeeDTO
        List<EmployeeDTO> employeeDTOs = employees.stream().map(employee -> {
            EmployeeDTO employeeDTO = new EmployeeDTO();

            // Tách địa chỉ nếu cần
            String address = employee.getAddress();
            String street = null, city = null, district = null, ward = null;
            try {
                if (address != null && !address.isEmpty()) {
                    String[] parts = address.split(", ");
                    if (parts.length >= 3) {
                        city = parts[parts.length - 1].trim(); // Thành phố
                        district = parts[parts.length - 2].trim(); // Quận
                        ward = (parts.length == 3) ? null : parts[parts.length - 3].trim(); // Phường
                        street = parts[0].trim(); // Đường
                    }
                }
            } catch (Exception e) {
                System.err.println("Lỗi khi tách chuỗi địa chỉ: " + e.getMessage());
            }
            String fullAddress = street + (ward != null ? ", " + ward : "") + ", " + district + ", " + city;

            // Thiết lập các thuộc tính cho EmployeeDTO
            employeeDTO.setId(employee.getEmployeeID());
            employeeDTO.setEmail(employee.getEmail());
            employeeDTO.setVerified(employee.isVerified());
            employeeDTO.setAddress(fullAddress);
            employeeDTO.setLastName(employee.getLastName());
            employeeDTO.setFirstName(employee.getFirstName());
            employeeDTO.setPhoneNumber(employee.getPhoneNumber());
            employeeDTO.setAvatar(employee.getAvatar());
            employeeDTO.setGender(employee.isGender());
            employeeDTO.setPassword(employee.getPassword());
            employeeDTO.setLogin_attempts(employee.getLogin_attempts());

            // Lấy vai trò và danh sách quyền hạn
            String role = employee.getAuthorities().stream()
                    .findFirst()
                    .map(authority -> authority.getRole().getRoleName())
                    .orElse("ROLE_USER");
            employeeDTO.setRole(role);

            Set<String> authorityNames = employee.getAuthorities().stream()
                    .map(authority -> authority.getRole().getRoleName())
                    .collect(Collectors.toSet());
            employeeDTO.setAuthorities(authorityNames);

            return employeeDTO;
        }).collect(Collectors.toList());

        // Trả về danh sách EmployeeDTO
        return ResponseEntity.ok(employeeDTOs);
    }


    @PostMapping(consumes = "application/json", produces = "application/json")
    public Employee createEmployee(@RequestBody Employee employee) {
        // Tìm kiếm vai trò 'R02' trong cơ sở dữ liệu thay vì tạo mới
        Roles roleR02 = roleRepository.findById("R02")
                .orElseThrow(() -> new ResourceNotFoundException("Role R02 not found"));

        // Tạo một Authority cho nhân viên
        Authorities authority = new Authorities();
        authority.setEmployee(employee); // Gán nhân viên
        authority.setRole(roleR02); // Gán role R02
        authority.setUsers(null); // Gán người dùng

        authoritiesRepository.save(authority);


        // Kiểm tra và khởi tạo authorities nếu cần
        if (employee.getAuthorities() == null) {
            employee.setAuthorities(new ArrayList<>()); // Khởi tạo danh sách nếu nó null
        }
        employee.getAuthorities().add(authority); // Thêm authority vào danh sách

        try {
            // Tạo khóa AES
            SecretKey key = AESUtil.generateKey();  // Tạo khóa AES mới

            // Mã hóa mật khẩu của nhân viên bằng khóa AES
            String encryptedPassword = AESUtil.encrypt(employee.getPassword(), key);

            // Lưu mật khẩu đã mã hóa vào cột password
            employee.setPassword(encryptedPassword);

            // Lưu khóa AES dưới dạng Base64 vào cột aseKey
            String base64Key = AESUtil.getBase64StringFromKey(key);  // Chuyển khóa AES thành chuỗi Base64 để lưu trữ
            employee.setAseKey(base64Key);  // Lưu chuỗi Base64 của khóa AES vào cột aseKey

        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Error occurred during AES encryption");
        }

        // Đảm bảo nhân viên được xác thực
        employee.setVerified(true);

        // Lưu nhân viên vào cơ sở dữ liệu
        return employeeRepository.save(employee);
    }


    @GetMapping("/{id}")
    public ResponseEntity<EmployeeDTO> getEmployeeById(@PathVariable("id") int employeeID) {
        Employee employee = employeeRepository.findById(employeeID)
                .orElseThrow(() -> new ResourceNotFoundException("Employee not exist with id: " + employeeID));
        try {
            String base64Key = employee.getAseKey();  // Cột 'aseKey' lưu trữ khóa AES ở dạng Base64

            if (base64Key != null && !base64Key.isEmpty()) {
                SecretKey key = AESUtil.getKeyFromBase64String(base64Key);
                String decryptedPassword = AESUtil.decrypt(employee.getPassword(), key);
                employee.setPassword(decryptedPassword);  // Thiết lập lại mật khẩu đã giải mã
            }
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(null);
        }

        EmployeeDTO employeeDTO = new EmployeeDTO();

        String address = employee.getAddress();

        // Tách chuỗi địa chỉ nếu cần thiết
        String street = null;
        String city = null;
        String district = null;
        String ward = null;

        try {
            // Tách chuỗi địa chỉ bằng dấu phẩy
            String[] parts = address.split(", ");

            if (parts.length >= 3) {
                city = parts[parts.length - 1].trim();  // Thành phố
                district = parts[parts.length - 2].trim();  // Quận
                ward = (parts.length == 3) ? null : parts[parts.length - 3].trim();  // Phường
                street = parts[0].trim();  // Đường
            }
        } catch (Exception e) {
            System.err.println("Lỗi khi tách chuỗi địa chỉ: " + e.getMessage());
        }

        // Trả về chuỗi địa chỉ hoàn chỉnh
        String fullAddress = street + (ward != null ? ", " + ward : "") + ", " + district + ", " + city;

        String role = employee.getAuthorities().stream()
                .findFirst()
                .map(authority -> authority.getRole().getRoleName())
                .orElse("ROLE_USER");

        // Thiết lập các thuộc tính cho EmployeeDTO
        employeeDTO.setId(employee.getEmployeeID());
        employeeDTO.setEmail(employee.getEmail());
        employeeDTO.setVerified(employee.isVerified());
        employeeDTO.setAddress(fullAddress);  // Thiết lập địa chỉ là chuỗi
        employeeDTO.setRole(role);  // Thiết lập vai trò
        employeeDTO.setAuthorities(employee.getAuthorities().stream()
                .map(authority -> authority.getRole().getRoleName())
                .collect(Collectors.toSet()));
        employeeDTO.setLastName(employee.getLastName());
        employeeDTO.setFirstName(employee.getFirstName());
        employeeDTO.setPhoneNumber(employee.getPhoneNumber());
        employeeDTO.setPassword(employee.getPassword());
        employeeDTO.setAvatar(employee.getAvatar());
        employeeDTO.setGender(employee.isGender());
        employeeDTO.setLogin_attempts(employee.getLogin_attempts());

        // Trả về đối tượng DTO đã xử lý
        return ResponseEntity.ok(employeeDTO);
    }


    @PutMapping("/{id}")
    public ResponseEntity<Employee> updateEmployee(
            @PathVariable("id") int id,
            @RequestPart("employee") EmployeeDTO employee,
            @RequestPart(value = "avatar", required = false) MultipartFile avatar) throws Exception {

        // Lấy thông tin nhân viên từ database
        Employee emp = employeeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Employee not found"));

        // Cập nhật thông tin người dùng
        emp.setFirstName(employee.getFirstName());
        emp.setLastName(employee.getLastName());
        emp.setEmail(employee.getEmail());
        emp.setPhoneNumber(employee.getPhoneNumber());
        emp.setGender(employee.isGender());

        // Cập nhật địa chỉ nếu có
        if (employee.getAddress() != null && !employee.getAddress().isEmpty()) {
            emp.setAddress(employee.getAddress());
        }

        if (employee.getPassword() != null && !employee.getPassword().isEmpty()) {
            if (emp.getAseKey() == null || emp.getAseKey().isEmpty()) {
                // Tạo mới khóa nếu chưa có
                SecretKey key = AESUtil.generateKey();
                String encryptedPassword = AESUtil.encrypt(employee.getPassword(), key);
                emp.setPassword(encryptedPassword);
                String base64Key = AESUtil.getBase64StringFromKey(key);
                emp.setAseKey(base64Key);
            } else {
                // Sử dụng khóa đã có để mã hóa mật khẩu mới
                String base64Key = emp.getAseKey();
                SecretKey key = AESUtil.getKeyFromBase64String(base64Key);
                String encryptedPassword = AESUtil.encrypt(employee.getPassword(), key);
                emp.setPassword(encryptedPassword);
            }
        }

        // Xử lý file ảnh đại diện nếu có
        if (avatar != null && !avatar.isEmpty()) {
            try {
                String avatarUrl = fileService.saveAvatar(avatar);
                emp.setAvatar(avatarUrl);
            } catch (IOException e) {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
            }
        }

        // Lưu thông tin nhân viên đã cập nhật
        Employee updatedEmployee = employeeRepository.save(emp);
        return ResponseEntity.ok(updatedEmployee); // Trả về phản hồi với nhân viên đã cập nhật
    }


    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable("id") int id) {
        Optional<Employee> empOptional = employeeRepository.findById(id);
        if (!empOptional.isPresent()) {
            return ResponseEntity.notFound().build();
        }

        Employee employee = empOptional.get();
        employee.setVerified(false); // Cập nhật trạng thái verified thành false
        employeeRepository.save(employee); // Lưu lại thay đổi

        return ResponseEntity.ok().build(); // Trả về phản hồi thành công
    }

    @PatchMapping("/{id}/disable")
    public ResponseEntity<Void> disableEmployee(@PathVariable("id") int id) {
        Optional<Employee> empOptional = employeeRepository.findById(id);
        if (!empOptional.isPresent()) {
            return ResponseEntity.notFound().build();
        }

        Employee emp = empOptional.get();
        emp.setVerified(false); // Vô hiệu hóa người dùng
        employeeRepository.save(emp); // Lưu thay đổi

        return ResponseEntity.ok().build(); // Trả về phản hồi thành công
    }

    @PatchMapping("/{id}/enable")
    public ResponseEntity<Void> enableEmployee(@PathVariable("id") int id) {
        Optional<Employee> empOptional = employeeRepository.findById(id);
        if (!empOptional.isPresent()) {
            return ResponseEntity.notFound().build();
        }

        Employee employee = empOptional.get();
        employee.setVerified(true); // Mở lại trạng thái cho nhân viên
        employeeRepository.save(employee); // Lưu lại thay đổi

        return ResponseEntity.ok().build(); // Trả về phản hồi thành công
    }


    @GetMapping("/next-id")
    public ResponseEntity<Integer> getNextEmployeeId() {
        Integer maxUserId = employeeRepository.findMaxEmployeeId();  // Truy vấn userID lớn nhất
        if (maxUserId == null) {
            maxUserId = 0;
        }
        return ResponseEntity.ok(maxUserId + 1);  // Trả về giá trị userID tiếp theo
    }

    @GetMapping("/auth")
    public ResponseEntity<List<EmployeeDTO>> getEmployeeWithAuthorities(@RequestParam(required = false) List<String> roles) {
        List<Employee> employees;

        if (roles == null || roles.isEmpty()) {
            employees = employeeRepository.findAll();
        } else {
            employees = employeeRepository.findByAuthorities_Role_RoleIDIn(roles);
        }

        List<EmployeeDTO> empDTO = employees.stream().map(employee -> {
            String address = employee.getAddress(); // Giả sử address là một chuỗi chứa địa chỉ

            // Tách chuỗi địa chỉ nếu cần thiết
            String street = null;
            String city = null;
            String district = null;
            String ward = null;

            try {
                // Tách chuỗi địa chỉ bằng dấu phẩy
                String[] parts = address.split(", ");

                if (parts.length >= 3) {
                    city = parts[parts.length - 1].trim();  // Thành phố
                    district = parts[parts.length - 2].trim();  // Quận
                    ward = (parts.length == 3) ? null : parts[parts.length - 3].trim();  // Phường
                    street = parts[0].trim();  // Đường
                }
            } catch (Exception e) {
                System.err.println("Lỗi khi tách chuỗi địa chỉ: " + e.getMessage());
            }

            // Trả về chuỗi địa chỉ hoàn chỉnh
            String fullAddress = street + (ward != null ? ", " + ward : "") + ", " + district + ", " + city;

            Set<String> authorityNames = employee.getAuthorities().stream()
                    .map(authority -> authority.getRole().getRoleName()) // Lấy tên role từ Authority
                    .collect(Collectors.toSet());

            // Giả định bạn có thuộc tính role lấy từ authorities
            String role = employee.getAuthorities().stream()
                    .findFirst()
                    .map(authority -> authority.getRole().getRoleName()) // Lấy tên vai trò từ Authority
                    .orElse("ROLE_USER");
            EmployeeDTO employeeDTO = new EmployeeDTO();
            employeeDTO.setId(employee.getEmployeeID());
            employeeDTO.setEmail(employee.getEmail());
            employeeDTO.setVerified(employee.isVerified());
            employeeDTO.setRole(role); // Thiết lập vai trò
            employeeDTO.setAuthorities(authorityNames);
            employeeDTO.setAddress(fullAddress);
            employeeDTO.setLastName(employee.getLastName());
            employeeDTO.setFirstName(employee.getFirstName());
            employeeDTO.setPhoneNumber(employee.getPhoneNumber());
            employeeDTO.setAvatar(employee.getAvatar());
            employeeDTO.setGender(employee.isGender());
            employeeDTO.setLogin_attempts(employee.getLogin_attempts());

            return employeeDTO;
        }).collect(Collectors.toList());

        return ResponseEntity.ok(empDTO);
    }

    @PutMapping("/{employeeId}/authorities")
    public ResponseEntity<Void> updateUserAuthorities(@PathVariable int employeeId, @RequestBody List<Authorities> authorities) {
        employeeService.updateEmployeeAuthorities(employeeId, authorities);
        return ResponseEntity.noContent().build(); // Trả về 204 No Content
    }
}
