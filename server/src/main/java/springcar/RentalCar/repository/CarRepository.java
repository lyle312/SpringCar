package springcar.RentalCar.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import springcar.RentalCar.entity.Car;

import java.util.List;
import java.util.Optional;

@Repository
public interface CarRepository extends JpaRepository<Car, Long> {
    boolean existsByNumber(String number);

    Optional<Car> findByNumber(String number);

    List<Car> findByNumberContaining(String number);

    List<Car> findByCarModel_IdAndNumberContaining(Long carModelId, String number);

    List<Car> findByCarModel_Id(Long carModel);

    boolean existsByCarModel_Id(Long id);
    List<Car> findByCarModel_NameContaining(String search);
}
