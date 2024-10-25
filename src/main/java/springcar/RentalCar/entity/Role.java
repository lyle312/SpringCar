package springcar.RentalCar.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.Set;

@Entity
@Table(name = "role")
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Role extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "name", nullable = false, unique=true)
    private String name;

    @ManyToMany(mappedBy = "roles", cascade = CascadeType.MERGE)
    private Set<User> users;

    @ManyToMany(cascade = CascadeType.MERGE)
    @JoinTable(
        name = "roles_privileges",
        joinColumns = @JoinColumn(
                name = "role_id", referencedColumnName = "id"),
        inverseJoinColumns = @JoinColumn(
                name = "privilege_id", referencedColumnName = "id"))
    private Set<Privilege> privileges;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Role )) return false;
        return id != null && id.equals(((Role) o).getId());
    }
}
