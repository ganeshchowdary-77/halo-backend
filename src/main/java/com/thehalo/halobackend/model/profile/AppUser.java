package com.thehalo.halobackend.model.profile;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;
import com.thehalo.halobackend.model.base.BaseEntity;

@Entity
@Table(name = "users")
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
@SQLDelete(sql = "UPDATE users SET deleted_at = CURRENT_TIMESTAMP WHERE id = ?")
@SQLRestriction("deleted_at IS NULL")
public class AppUser extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String password;

    @Column(nullable = false, unique = true)
    private String email;

    private String fullName;
    
    // Additional fields for compatibility
    private String firstName;
    private String lastName;

    @ManyToOne(fetch = FetchType.LAZY) // ManyToOne is Eager by default so we use lazy to only get the role when
    // needed.
    @JoinColumn(name = "role_id")
    private AppRole role;

    // Helper methods for backward compatibility
    public String getFirstName() {
        if (firstName != null) return firstName;
        if (fullName != null && fullName.contains(" ")) {
            return fullName.split(" ")[0];
        }
        return fullName;
    }

    public String getLastName() {
        if (lastName != null) return lastName;
        if (fullName != null && fullName.contains(" ")) {
            String[] parts = fullName.split(" ");
            return parts.length > 1 ? parts[parts.length - 1] : "";
        }
        return "";
    }
}
