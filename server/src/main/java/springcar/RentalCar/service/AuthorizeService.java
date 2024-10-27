package springcar.RentalCar.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import springcar.RentalCar.entity.Privilege;
import springcar.RentalCar.entity.Role;
import springcar.RentalCar.entity.User;
import springcar.RentalCar.repository.PrivilegeRepository;
import springcar.RentalCar.repository.RoleRepository;
import springcar.RentalCar.repository.UserRepository;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j(topic = "AuthorizeService")
public class AuthorizeService {
    private final RoleRepository roleRepository;
    private final PrivilegeRepository privilegeRepository;
    private final UserRepository userRepository;

    public String grantRole(User u, Long roleId) {
        User user = userRepository.findById(u.getId()).orElseThrow(
                () -> new ResponseStatusException(HttpStatus.NOT_FOUND, "사용자를 찾을 수 없습니다.")
        );
        Role role = roleRepository.findById(roleId).orElseThrow(
                () -> new ResponseStatusException(HttpStatus.NOT_FOUND, "역할을 찾을 수 없습니다.")
        );
        log.info("grantRole: " + u.getUsername() + ", " + role.getName());
        try {
            role.getUsers().add(user);
            user.getRoles().add(role);
            return role.getName();
        } catch (Exception e) {
            log.error("역할 부여 실패: " + e.getMessage());
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "역할 부여 실패");
        }
    }

    public String revokeRole(User u, Long roleId) {
        User user = userRepository.findById(u.getId()).orElseThrow(
                () -> new ResponseStatusException(HttpStatus.NOT_FOUND, "사용자를 찾을 수 없습니다.")
        );
        Role role = roleRepository.findById(roleId).orElseThrow(
                () -> new ResponseStatusException(HttpStatus.NOT_FOUND, "역할을 찾을 수 없습니다.")
        );
        log.info("revokeRole: " + u.getUsername() + ", " + role.getName());
        try {
            role.getUsers().remove(user);
            user.getRoles().remove(role);
            return role.getName();
        } catch (Exception e) {
            log.error("역할 회수 실패: " + e.getMessage());
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "역할 회수 실패");
        }
    }

    public String createRole(String roleName) {
        log.info("createRole: " + roleName);
        Role role = Role.builder()
                .name(roleName)
                .build();
        try {
            roleRepository.save(role);
        } catch (Exception e) {
            log.error("역할 생성 실패: " + e.getMessage());
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "역할 생성 실패");
        }
        return roleName;
    }

    public String deleteRole(String roleName) {
        log.info("deleteRole: " + roleName);
        Role role = roleRepository.findByName(roleName).orElseThrow(
                () -> new ResponseStatusException(HttpStatus.NOT_FOUND, "역할을 찾을 수 없습니다.")
        );
        if (!role.getUsers().isEmpty()) {
            log.error("역할에 해당하는 사용자가 존재합니다.");
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "역할에 해당하는 사용자가 존재합니다.");
        }
        try {
            roleRepository.delete(role);
        } catch (Exception e) {
            log.error("역할 삭제 실패: " + e.getMessage());
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "역할 삭제 실패");
        }
        return null;
    }

    public String grantPrivilege(Long roleId, Long privilegeId) {
        log.info("grantPrivilege: " + roleId + ", " + privilegeId);
        Role role = roleRepository.findById(roleId).orElseThrow(
                () -> new ResponseStatusException(HttpStatus.NOT_FOUND, "역할을 찾을 수 없습니다.")
        );
        Privilege privilege = privilegeRepository.findById(privilegeId).orElseThrow(
                () -> new ResponseStatusException(HttpStatus.NOT_FOUND, "권한을 찾을 수 없습니다.")
        );
        try {
            role.getPrivileges().add(privilege);
        } catch (Exception e) {
            log.error("권한 부여 실패: " + e.getMessage());
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "권한 부여 실패");
        }
        return privilege.getName();
    }

    public String revokePrivilege(Long roleId, Long privilegeId) {
        log.info("revokePrivilege: " + roleId + ", " + privilegeId);
        Role role = roleRepository.findById(roleId).orElseThrow(
                () -> new ResponseStatusException(HttpStatus.NOT_FOUND, "역할을 찾을 수 없습니다.")
        );
        Privilege privilege = privilegeRepository.findById(privilegeId).orElseThrow(
                () -> new ResponseStatusException(HttpStatus.NOT_FOUND, "권한을 찾을 수 없습니다.")
        );
        try {
            role.getPrivileges().remove(privilege);
            privilege.getRoles().remove(role);
        } catch (Exception e) {
            log.error("권한 회수 실패: " + e.getMessage());
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "권한 회수 실패");
        }
        return privilege.getName();
    }

    public String createPrivilege(String privilegeName) {
        log.info("createPrivilege: " + privilegeName);
        Privilege privilege = Privilege.builder()
                .name(privilegeName)
                .build();
        try {
            privilegeRepository.save(privilege);
        } catch (Exception e) {
            log.error("권한 생성 실패: " + e.getMessage());
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "권한 생성 실패");
        }
        return privilegeName;
    }

    public String deletePrivilege(String privilegeName) {
        log.info("deletePrivilege: " + privilegeName);
        Privilege privilege = privilegeRepository.findByName(privilegeName).orElseThrow(
                () -> new ResponseStatusException(HttpStatus.NOT_FOUND, "권한을 찾을 수 없습니다.")
        );
        try {
            privilegeRepository.delete(privilege);
        } catch (Exception e) {
            log.error("권한 삭제 실패: " + e.getMessage());
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "권한 삭제 실패");
        }
        return privilegeName;
    }

    public List<Privilege> findRolePrivileges(Long roleId) {
        log.info("findRolePrivileges: " + roleId);
        Role role = roleRepository.findById(roleId).orElseThrow(
                () -> new ResponseStatusException(HttpStatus.NOT_FOUND, "역할을 찾을 수 없습니다.")
        );
        return role.getPrivileges().stream().toList();
    }

    public List<Privilege> findAllPrivileges() {
        log.info("findAllPrivileges");
        return privilegeRepository.findAll();
    }
}
