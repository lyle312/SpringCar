package springcar.RentalCar.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import springcar.RentalCar.entity.UserGroup;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserGroupRepository extends JpaRepository<UserGroup, Long> {
    boolean existsByName(String name);
    Optional<UserGroup> findByName(String name);

    @Query("SELECT ug.name FROM UserGroup ug where ug.name != 'ADMIN'")
    List<String> findAllNames();
}
