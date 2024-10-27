package springcar.RentalCar.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import springcar.RentalCar.entity.RentCar;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface RentCarRepository extends JpaRepository<RentCar, Long> {
    Boolean existsByCar_IdAndReturnDateGreaterThanEqualAndPickupDateLessThanEqualAndIsReturned(Long carId, LocalDate pickUpDate, LocalDate returnDate, Boolean isReturned);
    List<RentCar> findByCar_IdAndReturnDateGreaterThanEqualAndPickupDateLessThanEqual(Long carId, LocalDate pickUpDate, LocalDate returnDate);
    List<RentCar> findByCar_IdAndReturnDateGreaterThanEqual(Long carId, LocalDate pickUpDate);
    List<RentCar> findByCar_IdAndReturnDateGreaterThanEqualAndIsReturned(Long carId, LocalDate pickUpDate, Boolean isReturned);
    Optional<RentCar> findByUser_IdAndCarIdAndPickupDateAndReturnDate(Long userId, Long carId, LocalDate pickUpDate, LocalDate returnDate);

    boolean existsByCar_IdAndPickupDateGreaterThanEqualAndIsReturned(Long carId, LocalDate pickUpDate, Boolean isReturned);

    List<RentCar> findByUser_Id(Long id);

    List<RentCar> findByUser_IdAndCar_NumberContaining(Long id, String search);

    List<RentCar> findByUser_IdAndCar_CarModel_NameContaining(Long id, String search);
}
