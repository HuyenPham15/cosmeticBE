    package com.cosmetics.model;

    import com.fasterxml.jackson.annotation.JsonIgnore;
    import com.fasterxml.jackson.annotation.JsonManagedReference;
    import jakarta.persistence.*;
    import lombok.AllArgsConstructor;
    import lombok.Data;
    import lombok.NoArgsConstructor;

    import java.util.ArrayList;
    import java.util.List;
    import java.util.Objects;

    import static org.springframework.data.jpa.domain.AbstractPersistable_.id;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Entity
    @Table(name = "users")
    public class Users {
        @Id
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        private int userID;
        private String lastName;
        private String firstName;
        private String email;
        private String password;
        private String phoneNumber;
        private String avatar;
        private boolean gender;
        private float total_point;
        private boolean verified;
        private int login_attempts;
        private String aseKey;

        @JsonIgnore
        @OneToMany(mappedBy = "users", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
        private List<Order> orders; // Đảm bảo Order được viết hoa (theo quy tắc Java)
        @JsonIgnore
        @OneToMany(mappedBy = "users", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
        private List<Address> address;

        @JsonIgnore
        @JsonManagedReference
        @OneToMany(mappedBy = "users", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
        private List<Authorities> authorities;


        @JsonIgnore
        @OneToMany(mappedBy = "users", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
        private List<UserPoint> userPoints;

        @JsonIgnore
        @OneToMany(mappedBy = "users", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
        private List<wishlist> wishlists;

        @Override
        public String toString() {
            return "Users{" +
                    "username='" + lastName + '\'' +
                    // Không gọi đến các đối tượng khác ở đây
                    '}';
        }
        @Override
        public int hashCode() {
            return Objects.hash(userID, lastName);  // Tránh tham chiếu qua lại vào UserPoint
        }
    }
