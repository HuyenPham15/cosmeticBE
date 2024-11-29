package com.cosmetics.restcontroller;

import com.cosmetics.dto.AddressDTO;
import com.cosmetics.dto.UserDTO;
import com.cosmetics.exception.ResourceNotFoundException;
import com.cosmetics.model.*;
import com.cosmetics.repository.AuthoritiesRepository;
import com.cosmetics.repository.EmployeeRepository;
import com.cosmetics.repository.RoleRepository;
import com.cosmetics.repository.UserRepository;
import com.cosmetics.service.AESUtil;
import com.cosmetics.service.FileService;
import com.cosmetics.service.OrderService;
import com.cosmetics.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.User;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import javax.crypto.SecretKey;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

@CrossOrigin(origins = "*")
@RestController
@RequestMapping("/rest/user")
public class UserRestController {

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
    private OrderService orderService;

    @GetMapping
    public ResponseEntity<List<UserDTO>> getAll(@RequestParam(required = false) List<String> roles) {
        List<Users> users;

        if (roles == null || roles.isEmpty()) {
            users = userRepository.findAll();
        } else {
            users = userRepository.findByAuthorities_Role_RoleIDIn(roles);
        }

        List<UserDTO> userDTOs = users.stream().map(user -> {
            List<AddressDTO> addressDTOs = user.getAddress().stream()
                    .map(address -> {
                        // Giả sử specific_address có định dạng "street, ward, district, city"
                        String[] addressParts = address.getSpecific_address().split(","); // Tách chuỗi địa chỉ

                        String street = addressParts.length > 0 ? addressParts[0].trim() : null;
                        String ward = addressParts.length > 1 ? addressParts[1].trim() : null;
                        String district = addressParts.length > 2 ? addressParts[2].trim() : null;
                        String city = addressParts.length > 3 ? addressParts[3].trim() : null;
                        String phoneNumber = address.getPhoneNumber();
                        String firstName = address.getFirst_name();
                        String lastName = address.getLast_name();
                        boolean isDefault = address.is_default();

                        return new AddressDTO(
                                address.getAddressID(),
                                address.getSpecific_address(),
                                phoneNumber,
                                firstName,
                                lastName,
                                city,
                                district,
                                ward,
                                street,
                                isDefault
                        );
                    })
                    .collect(Collectors.toList());

            // Lấy danh sách authorities từ Users
            Set<String> authorityNames = user.getAuthorities().stream()
                    .map(authority -> authority.getRole().getRoleName()) // Lấy tên role từ Authority
                    .collect(Collectors.toSet());

            // Giả định bạn có thuộc tính role lấy từ authorities
            String role = user.getAuthorities().stream()
                    .findFirst()
                    .map(authority -> authority.getRole().getRoleName()) // Lấy tên vai trò từ Authority
                    .orElse("ROLE_USER");
            UserDTO userDTO = new UserDTO();
            userDTO.setId(user.getUserID());
            userDTO.setEmail(user.getEmail());
            userDTO.setVerified(user.isVerified());
            userDTO.setRole(role); // Thiết lập vai trò
            userDTO.setAuthorities(authorityNames);
            userDTO.setAddresses(addressDTOs);
            userDTO.setLastName(user.getLastName());
            userDTO.setFirstName(user.getFirstName());
            userDTO.setPhoneNumber(user.getPhoneNumber());
            userDTO.setAvatar(user.getAvatar());
            userDTO.setGender(user.isGender());
            userDTO.setTotal_point(user.getTotal_point());
            userDTO.setLogin_attempts(user.getLogin_attempts());
            user.getAddress().forEach(address -> {

                System.out.println("Specific Address: " + address.getSpecific_address());
            });

            return userDTO;
        }).collect(Collectors.toList());

        return ResponseEntity.ok(userDTOs);
    }

    @PostMapping(consumes = "application/json", produces = "application/json")
    public Users createEmployee(@RequestBody Users user) {
        // Tìm kiếm vai trò 'R02' trong cơ sở dữ liệu thay vì tạo mới
        Roles roleR03 = roleRepository.findById("R03")
                .orElseThrow(() -> new ResourceNotFoundException("Role R02 not found"));

        // Tạo một Authority cho nhân viên
        Authorities authority = new Authorities();
        authority.setEmployee(null); // Gán nhân viên
        authority.setRole(roleR03); // Gán role R02
        authority.setUsers(user); // Gán người dùng

        authoritiesRepository.save(authority);


        // Kiểm tra và khởi tạo authorities nếu cần
        if (user.getAuthorities() == null) {
            user.setAuthorities(new ArrayList<>()); // Khởi tạo danh sách nếu nó null
        }
        user.getAuthorities().add(authority); // Thêm authority vào danh sách

        try {
            // Tạo khóa AES
            SecretKey key = AESUtil.generateKey();  // Tạo khóa AES mới

            // Mã hóa mật khẩu của nhân viên bằng khóa AES
            String encryptedPassword = AESUtil.encrypt(user.getPassword(), key);

            // Lưu mật khẩu đã mã hóa vào cột password
            user.setPassword(encryptedPassword);

            // Lưu khóa AES dưới dạng Base64 vào cột aseKey
            String base64Key = AESUtil.getBase64StringFromKey(key);  // Chuyển khóa AES thành chuỗi Base64 để lưu trữ
            user.setAseKey(base64Key);  // Lưu chuỗi Base64 của khóa AES vào cột aseKey

        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Error occurred during AES encryption");
        }

        // Đảm bảo nhân viên được xác thực
        user.setVerified(true);

        // Lưu nhân viên vào cơ sở dữ liệu
        return userRepository.save(user);
    }


    @GetMapping("/{id}")
    public ResponseEntity<UserDTO> getEmployeeById(@PathVariable("id") int userID) {
        Users user = userRepository.findById(userID)
                .orElseThrow(() -> new ResourceNotFoundException("Employee not exist with id: " + userID));
        try {
            String base64Key = user.getAseKey();  // Cột 'aseKey' lưu trữ khóa AES ở dạng Base64

            if (base64Key != null && !base64Key.isEmpty()) {
                SecretKey key = AESUtil.getKeyFromBase64String(base64Key);
                String decryptedPassword = AESUtil.decrypt(user.getPassword(), key);
                user.setPassword(decryptedPassword);
            }
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(null);
        }
        // Tạo DTO để chuyển đổi dữ liệu trả về
        UserDTO userDTO = new UserDTO();
        // Chuyển đổi danh sách địa chỉ của employee sang danh sách AddressDTO
        List<AddressDTO> addressDTOs = user.getAddress().stream()
                .map(address -> {
                    String specificAddress = address.getSpecific_address();


                    String street = null;
                    String city = null;
                    String district = null;
                    String ward = null;
                    String phoneNumber = address.getPhoneNumber();
                    String firstName = address.getFirst_name();
                    String lastName = address.getLast_name();
                    try {
                        // Tách chuỗi bằng dấu phẩy
                        String[] parts = specificAddress.split(", ");

                        if (parts.length >= 3) {
                            city = parts[parts.length - 1].trim();       // Hồ Chí Minh
                            district = parts[parts.length - 2].trim();   // Quận 9

                            // Nếu có 3 phần và không có phường
                            ward = (parts.length == 3) ? null : parts[parts.length - 3].trim();

                            street = parts[0].trim();                     // 1515 Đường Nguyễn Duy Trinh
                        } else {
                            throw new IllegalArgumentException("Địa chỉ không đầy đủ thông tin (ít hơn 3 phần).");
                        }
                    } catch (Exception e) {
                        System.err.println("Lỗi khi tách chuỗi địa chỉ: " + e.getMessage());
                    }

                    // Trả về đối tượng AddressDTO
                    return new AddressDTO(
                            address.getAddressID(),
                            address.getSpecific_address(),
                            phoneNumber,
                            firstName,
                            lastName,
                            street,
                            ward,
                            district,  // Ward là null vì không có thông tin về xã/phường
                            city,
                            address.is_default()
                    );
                })
                .collect(Collectors.toList());
        // Xử lý Authorities (Role)
        Set<String> authorityNames = user.getAuthorities().stream()
                .map(authority -> authority.getRole().getRoleName()) // Lấy tên vai trò từ Authority
                .collect(Collectors.toSet());

        String role = user.getAuthorities().stream()
                .findFirst()
                .map(authority -> authority.getRole().getRoleName()) // Lấy tên vai trò từ Authority
                .orElse("ROLE_USER");

        // Thiết lập các thuộc tính cho UserDTO
        userDTO.setId(user.getUserID());
        userDTO.setEmail(user.getEmail());
        userDTO.setVerified(user.isVerified());
        userDTO.setRole(role); // Thiết lập vai trò
        userDTO.setAuthorities(authorityNames);
        userDTO.setAddresses(addressDTOs);
        userDTO.setLastName(user.getLastName());
        userDTO.setPassword(user.getPassword());
        userDTO.setFirstName(user.getFirstName());
        userDTO.setPhoneNumber(user.getPhoneNumber());
        userDTO.setAvatar(user.getAvatar());
        userDTO.setGender(user.isGender());
        userDTO.setTotal_point(user.getTotal_point());
        userDTO.setLogin_attempts(user.getLogin_attempts());
        // Trả về đối tượng DTO đã xử lý
        return ResponseEntity.ok(userDTO);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Users> updateEmployee(@PathVariable("id") int id, @RequestPart("employee") UserDTO employee, @RequestPart(value = "avatar", required = false) MultipartFile avatar) throws Exception {

        Users emp = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        // Cập nhật thông tin người dùng
        emp.setFirstName(employee.getFirstName());
        emp.setLastName(employee.getLastName());
        emp.setEmail(employee.getEmail());
        emp.setPhoneNumber(employee.getPhoneNumber());
        emp.setGender(employee.isGender());
        emp.setTotal_point(employee.getTotal_point());
        emp.setUserID(employee.getId());

        // Cập nhật địa chỉ
        if (employee.getAddresses() != null && !employee.getAddresses().isEmpty()) {
            List<Address> addresses = new ArrayList<>();  // Tạo danh sách để lưu địa chỉ

            for (AddressDTO addressDTO : employee.getAddresses()) {  // Sử dụng List<AddressDTO>
                Address address = new Address();  // Tạo đối tượng Address mới
                address.setAddressID(addressDTO.getAddressID());
                address.set_default(addressDTO.isDefaultAddress());

                // Ghép các phần của địa chỉ thành một chuỗi
                String combinedAddress = String.join(", ",
                        addressDTO.getStreet() != null ? addressDTO.getStreet() : "",
                        addressDTO.getWard() != null ? addressDTO.getWard() : "",
                        addressDTO.getDistrict() != null ? addressDTO.getDistrict() : "",
                        addressDTO.getCity() != null ? addressDTO.getCity() : ""
                );

                address.setSpecific_address(combinedAddress);  // Lưu địa chỉ ghép
                address.setPhoneNumber(addressDTO.getPhoneNumber());
                address.setFirst_name(addressDTO.getFirstName());
                address.setLast_name(addressDTO.getLastName());
                address.setUsers(emp);  // Liên kết với đối tượng người dùng
                addresses.add(address);  // Thêm địa chỉ vào danh sách
            }
            emp.setAddress(addresses);  // Cập nhật danh sách địa chỉ cho người dùng
        }

        // Mã hóa và lưu mật khẩu
        if (emp.getAseKey() == null || emp.getAseKey().isEmpty()) {
            SecretKey key = AESUtil.generateKey();
            String encryptedPassword = AESUtil.encrypt(employee.getPassword(), key);
            emp.setPassword(encryptedPassword);
            String base64Key = AESUtil.getBase64StringFromKey(key);
            emp.setAseKey(base64Key);
        } else {
            String base64Key = emp.getAseKey();
            SecretKey key = AESUtil.getKeyFromBase64String(base64Key);
            String encryptedPassword = AESUtil.encrypt(employee.getPassword(), key);
            emp.setPassword(encryptedPassword);
        }

        // Xử lý file ảnh nếu có
        if (avatar != null && !avatar.isEmpty()) {
            try {
                String avatarUrl = fileService.saveAvatar(avatar);
                emp.setAvatar(avatarUrl);
            } catch (IOException e) {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
            }
        }

        // Lưu đối tượng người dùng sau khi cập nhật
        Users updatedUser = userRepository.save(emp);
        return ResponseEntity.ok(updatedUser);  // Trả về phản hồi
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable("id") int id) {
        Optional<Users> userOptional = userRepository.findById(id);
        if (!userOptional.isPresent()) {
            return ResponseEntity.notFound().build();
        }

        Users user = userOptional.get();
        user.setVerified(false); // Cập nhật trạng thái verified thành false
        userRepository.save(user); // Lưu lại thay đổi

        return ResponseEntity.ok().build(); // Trả về phản hồi thành công
    }

    @PatchMapping("/{id}/disable")
    public ResponseEntity<Void> disableUser(@PathVariable("id") int id) {
        Optional<Users> userOptional = userRepository.findById(id);
        if (!userOptional.isPresent()) {
            return ResponseEntity.notFound().build();
        }

        Users user = userOptional.get();
        user.setVerified(false); // Vô hiệu hóa người dùng
        userRepository.save(user); // Lưu thay đổi

        return ResponseEntity.ok().build(); // Trả về phản hồi thành công
    }

    @PatchMapping("/{id}/enable")
    public ResponseEntity<Void> enableUser(@PathVariable("id") int id) {
        Optional<Users> userOptional = userRepository.findById(id);
        if (!userOptional.isPresent()) {
            return ResponseEntity.notFound().build();
        }

        Users user = userOptional.get();
        user.setVerified(true); // Mở lại trạng thái cho nhân viên
        userRepository.save(user); // Lưu lại thay đổi

        return ResponseEntity.ok().build(); // Trả về phản hồi thành công
    }


    @GetMapping("/next-id")
    public ResponseEntity<Integer> getNextUserId() {
        Integer maxUserId = userRepository.findMaxUserId();  // Truy vấn userID lớn nhất
        if (maxUserId == null) {
            maxUserId = 0;
        }
        return ResponseEntity.ok(maxUserId + 1);  // Trả về giá trị userID tiếp theo
    }

    @GetMapping("/auth")
    public ResponseEntity<List<UserDTO>> getUserWithAuthorities(@RequestParam(required = false) List<String> roles) {
        List<Users> users;

        if (roles == null || roles.isEmpty()) {
            users = userRepository.findAll();
        } else {
            users = userRepository.findByAuthorities_Role_RoleIDIn(roles);
        }

        List<UserDTO> userDTOs = users.stream().map(user -> {
            List<AddressDTO> addressDTOs = user.getAddress().stream()
                    .map(address -> {
                        // Giả sử specific_address có định dạng "street, ward, district, city"
                        String[] addressParts = address.getSpecific_address().split(","); // Tách chuỗi địa chỉ

                        String street = addressParts.length > 0 ? addressParts[0].trim() : null;
                        String ward = addressParts.length > 1 ? addressParts[1].trim() : null;
                        String district = addressParts.length > 2 ? addressParts[2].trim() : null;
                        String city = addressParts.length > 3 ? addressParts[3].trim() : null;
                        String phoneNumber = address.getPhoneNumber();
                        String firstName = address.getFirst_name();
                        String lastName = address.getLast_name();
                        boolean isDefault = address.is_default();

                        return new AddressDTO(
                                address.getAddressID(),
                                address.getSpecific_address(),
                                phoneNumber,
                                firstName,
                                lastName,
                                city,
                                district,
                                ward,
                                street,
                                isDefault
                        );
                    })
                    .collect(Collectors.toList());

            // Lấy danh sách authorities từ Users
            Set<String> authorityNames = user.getAuthorities().stream()
                    .map(authority -> authority.getRole().getRoleName()) // Lấy tên role từ Authority
                    .collect(Collectors.toSet());

            // Giả định bạn có thuộc tính role lấy từ authorities
            String role = user.getAuthorities().stream()
                    .findFirst()
                    .map(authority -> authority.getRole().getRoleName()) // Lấy tên vai trò từ Authority
                    .orElse("ROLE_USER");
            UserDTO userDTO = new UserDTO();
            userDTO.setId(user.getUserID());
            userDTO.setEmail(user.getEmail());
            userDTO.setVerified(user.isVerified());
            userDTO.setRole(role); // Thiết lập vai trò
            userDTO.setAuthorities(authorityNames);
            userDTO.setAddresses(addressDTOs);
            userDTO.setLastName(user.getLastName());
            userDTO.setFirstName(user.getFirstName());
            userDTO.setPhoneNumber(user.getPhoneNumber());
            userDTO.setAvatar(user.getAvatar());
            userDTO.setGender(user.isGender());
            userDTO.setTotal_point(user.getTotal_point());
            userDTO.setLogin_attempts(user.getLogin_attempts());
            user.getAddress().forEach(address -> {

                System.out.println("Specific Address: " + address.getSpecific_address());
            });

            return userDTO;
        }).collect(Collectors.toList());

        return ResponseEntity.ok(userDTOs);
    }

    @PutMapping("/{userId}/authorities")
    public ResponseEntity<Void> updateUserAuthorities(@PathVariable int userId, @RequestBody List<Authorities> authorities) {
        userService.updateUserAuthorities(userId, authorities);
        return ResponseEntity.noContent().build(); // Trả về 204 No Content
    }
    @GetMapping("/orders/{userID}")
    public ResponseEntity<List<Order>> getOrdersByUserId(@PathVariable int userID) {
        List<Order> orders = orderService.findOrdersByUserId(userID);
        if (orders.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Collections.emptyList());
        }
        return ResponseEntity.ok(orders);
    }

}
