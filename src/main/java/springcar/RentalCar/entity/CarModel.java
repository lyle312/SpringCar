package springcar.RentalCar.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.List;

@Entity
@Table(name = "car_model")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CarModel extends BaseTimeEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    @Column(name = "name", nullable = false, unique = true)
    String name;

    @Column(name = "image")
    String image;

    @OneToMany(mappedBy = "carModel", cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    private List<Car> cars;
}
