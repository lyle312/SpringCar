package springcar.RentalCar.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import springcar.RentalCar.entity.Privilege;
import springcar.RentalCar.entity.Role;
import springcar.RentalCar.entity.User;
import springcar.RentalCar.entity.UserGroup;
import springcar.RentalCar.enums.PrivilegeEnum;
import springcar.RentalCar.enums.UserGroupEnum;
import springcar.RentalCar.repository.PrivilegeRepository;
import springcar.RentalCar.repository.RoleRepository;
import springcar.RentalCar.repository.UserGroupRepository;
import springcar.RentalCar.repository.UserRepository;
import springcar.RentalCar.enums.RoleEnum;

import java.util.*;

@Component
@RequiredArgsConstructor
public class SetupAuthority implements ApplicationListener<ContextRefreshedEvent> {

    private boolean alreadySetup = false;
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PrivilegeRepository privilegeRepository;
    private final PasswordEncoder passwordEncoder;
    private final UserGroupRepository userGroupRepository;

    @Override
    @Transactional
    public void onApplicationEvent(ContextRefreshedEvent event) { // 애플리케이션 시작 시점에 실행
        // 이미 실행되었다면 종료
        if (alreadySetup)
            return;

        // 기본 권한 생성
        Privilege readPrivilege
                = createPrivilegeIfNotFound(PrivilegeEnum.READ_PRIVILEGE.getValue());
        Privilege carRentalPrivilege
                = createPrivilegeIfNotFound(PrivilegeEnum.CAR_RENTAL_PRIVILEGE.getValue());

        // 권한에 따른 역할 생성
        Set<Privilege> allPrivileges = Arrays.stream(PrivilegeEnum.values())
                .map(PrivilegeEnum::getValue)
                .map(this::createPrivilegeIfNotFound)
                .collect(HashSet::new, HashSet::add, HashSet::addAll);
        Set<Privilege> advancedUserPrivileges = new HashSet<>(Arrays.asList(readPrivilege, carRentalPrivilege));
        Set<Privilege> userPrivileges = Collections.singleton(readPrivilege);
        Set<Privilege> carRentalPrivileges = new HashSet<>(Arrays.asList(readPrivilege, carRentalPrivilege));

        // 역할 생성
        Role adminRole = createRoleIfNotFound(RoleEnum.ROLE_ADMIN.getValue(), allPrivileges);
        Role advancedRole = createRoleIfNotFound(RoleEnum.ROLE_ADVANCED_USER.getValue(), advancedUserPrivileges);
        Role basicRole = createRoleIfNotFound(RoleEnum.ROLE_USER.getValue(), userPrivileges);
        Role carRentalRole = createRoleIfNotFound(RoleEnum.ROLE_CAR_RENTAL.getValue(), carRentalPrivileges);

        // 그룹 생성
        List<UserGroup> allUserGroups = Arrays.stream(UserGroupEnum.values())
                .map(UserGroupEnum::getValue)
                .map(this::createUserGroupIfNotFound)
                .collect(ArrayList::new, ArrayList::add, ArrayList::addAll);

        // 관리자 그룹 생성
        UserGroup adminGroup = createUserGroupIfNotFound(UserGroupEnum.GROUP_ADMIN.getValue());

        // 일산렌터카 그룹 생성
        UserGroup ilSanGroup = createUserGroupIfNotFound(UserGroupEnum.GROUP_ILSAN.getValue());

        // admin 계정 생성
        User user = new User();
        user.setUsername("admin");
        user.setPassword(passwordEncoder.encode("1234"));
        user.setName("관리자");
        user.setRoles(Collections.singleton(adminRole));
        user.setUserGroup(adminGroup);
        userRepository.save(user);

        // 일산렌터카 계정 생성
        User ilSanUser = new User();
        ilSanUser.setUsername("user");
        ilSanUser.setPassword(passwordEncoder.encode("1234"));
        ilSanUser.setName("김민재");
        ilSanUser.setRoles(Collections.singleton(advancedRole));
        ilSanUser.setUserGroup(ilSanGroup);
        userRepository.save(ilSanUser);

        alreadySetup = true;
    }

    @Transactional
    public Privilege createPrivilegeIfNotFound(String name) {

        Privilege privilege = privilegeRepository.findByName(name).orElse(null);
        if (privilege == null) {
            privilege = Privilege.builder()
                        .name(name)
                        .build();
            privilegeRepository.save(privilege);
        }
        return privilege;
    }

    @Transactional
    public Role createRoleIfNotFound(String name, Set<Privilege> privileges) {
        Role role = roleRepository.findByName(name).orElse(null);
        if (role == null) {
            role = Role.builder()
                    .name(name)
                    .build();
            role.setPrivileges(privileges);
            roleRepository.save(role);
        }
        return role;
    }

    @Transactional
    public UserGroup createUserGroupIfNotFound(String name) {
        UserGroup userGroup = userGroupRepository.findByName(name).orElse(null);
        if (userGroup == null) {
            userGroup = UserGroup.builder()
                    .name(name)
                    .build();
            userGroupRepository.save(userGroup);
        }
        return userGroup;
    }
}
