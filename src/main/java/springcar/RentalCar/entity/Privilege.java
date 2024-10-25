package springcar.RentalCar.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.Set;

@Entity
@Table(name = "privilege")
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Privilege extends BaseTimeEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "name", nullable = false, unique=true)
    private String name;

    @ManyToMany(mappedBy = "privileges", cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    private Set<Role> roles;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Privilege )) return false;
        return id != null && id.equals(((Privilege) o).getId());
    }
}