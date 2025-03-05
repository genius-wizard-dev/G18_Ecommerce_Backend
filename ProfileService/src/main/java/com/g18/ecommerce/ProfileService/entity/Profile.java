package com.g18.ecommerce.ProfileService.entity;


import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Entity
@Table(name = "profiles")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Profile {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    String id;

    @Column(unique = true, nullable = false, columnDefinition = "varchar(36)")
    String userId;

    String email;
    String phoneNumber;
    String fullName;
    String displayName;
    boolean isActivated;
    Date createdAt;
    Date updatedAt;
    Date birthDay;
    String avatar;
    @Column(unique = true, columnDefinition = "varchar(36)")
    String shopId;
    @OneToMany(mappedBy = "profile", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Address> addresses = new ArrayList<>();

}
