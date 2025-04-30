package com.g18.ecommerce.ProfileService.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.Date;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Entity
@Table(name = "addresses")
public class Address {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
     String id;

    @ManyToOne
    @JoinColumn(name = "profile_id", nullable = false)
     Profile profile;

     String street;
     String ward;
     String city;
     String detail;
    @Enumerated(EnumType.STRING)
     AddressType type;

     boolean isDefault;
     String phoneShip;
     Date createdAt;
     Date updatedAt;

}
