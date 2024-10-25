package springcar.RentalCar.repository;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import springcar.RentalCar.entity.UserGroup;

import java.util.List;

import static springcar.RentalCar.enums.UserGroupEnum.GROUP_ADMIN;
import static springcar.RentalCar.enums.UserGroupEnum.GROUP_AUTOEVER;

@DataJpaTest
public class LoginRepositoryTest {
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private UserGroupRepository userGroupRepository;

    @BeforeAll
    public static void setUp() {
        System.out.println("LoginRepositoryTest.setUp");
    }

    @Test
    @DisplayName("모든 그룹명 조회")
    public void findAllUserGroup() {
        UserGroup adminGroup = UserGroup.builder()
                .name(GROUP_ADMIN.getValue())
                .build();
        UserGroup autoeverGroup = UserGroup.builder()
                .name(GROUP_AUTOEVER.getValue())
                .build();
        userGroupRepository.save(adminGroup);
        userGroupRepository.save(autoeverGroup);

        List<String> userGroups = userGroupRepository.findAllNames();
        for (String userGroup : userGroups) {
            System.out.println("userGroup = " + userGroup);
        }
    }
}
