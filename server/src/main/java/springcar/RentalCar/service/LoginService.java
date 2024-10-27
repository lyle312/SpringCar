package springcar.RentalCar.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import springcar.RentalCar.dto.user.RoleDto;
import springcar.RentalCar.dto.user.UserDto;
import springcar.RentalCar.entity.Privilege;
import springcar.RentalCar.entity.Role;
import springcar.RentalCar.entity.User;
import springcar.RentalCar.entity.UserGroup;
import springcar.RentalCar.repository.RoleRepository;
import springcar.RentalCar.repository.UserGroupRepository;
import springcar.RentalCar.repository.UserRepository;
import springcar.RentalCar.utils.Converter;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import static springcar.RentalCar.enums.RoleEnum.ROLE_USER;

@Service
@Slf4j(topic = "UserService")
@Transactional
@RequiredArgsConstructor
public class LoginService implements UserDetailsService {
    private final UserRepository userRepository;
    private final UserGroupRepository userGroupRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final Converter converter;

    // 로그인 여부 확인, 로그인 되어있으면 true, 아니면 false
    public static boolean isAuthenticated() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || authentication instanceof AnonymousAuthenticationToken) {
            return false;
        }
        return authentication.isAuthenticated();
    }

    // 로그인 되어있는 유저 이름 반환
    public static String getAuthenticatedUsername() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || authentication instanceof AnonymousAuthenticationToken) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "로그인이 필요합니다.");
        }
        return authentication.getName();
    }

    public User getLoginUser() {
        String username = getAuthenticatedUsername();
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "로그인된 사용자를 찾을 수 없습니다."));
    }

    public UserDto getLoginUserDto() {
        User user = getLoginUser();
        return UserDto.builder()
                .id(user.getId())
                .username(user.getUsername())
                .password(user.getPassword())
                .name(user.getName())
                .userGroup(user.getUserGroup().getName())
                .build();
    }

    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    public List<UserDto> getAllUserDto() {
        List<User> users = getAllUsers();
        return converter.convertToUserDtoList(users);
    }

    public List<Role> findUserRoles(Long id) {
        User user = findById(id);
        return user.getRoles().stream().toList();
    }

    public List<String> getLoginUserRoles() {
        User user = getLoginUser();
        List<String> roles = new ArrayList<>();
        user.getRoles().forEach(role -> roles.add(role.getName()));
        return roles;
    }

    public void validateUsername(String username) {
        if (username.length() > 20) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "아이디는 20자 이하로 입력해주세요.");
        }
        userRepository.findByUsername(username)
            .ifPresent(m -> {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "이미 존재하는 회원입니다.");
            });
    }

    public User findByName(String name) {
        return userRepository.findByUsername(name)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "회원을 찾을 수 없습니다."));
    }

    public User findById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "회원을 찾을 수 없습니다."));
    }

    public List<String> findAllUserNames() {
        return userRepository.findAllNames();
    }

    public List<String> findAllGroupNames() {
        return userGroupRepository.findAllNames();
    }

    public List<String> findAllRoleNames() {
        return roleRepository.findAllNames();
    }

    public List<Role> findAllRoles() {
        return roleRepository.findAll();
    }

    public List<RoleDto> findAllRoleDto() {
        List<Role> roles = findAllRoles();
        return converter.convertToRoleDtoList(roles);
    }


    // 기본 유저로 회원가입
    public User register(UserDto userDto) {
        validateUsername(userDto.getUsername());
        UserGroup userGroup = userGroupRepository.findByName(userDto.getUserGroup())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "유효한 그룹이 아닙니다."));
        Role role = roleRepository.findByName(ROLE_USER.getValue())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "회원가입에 실패했습니다."));

        User user = User.builder()
                .username(userDto.getUsername())
                .password(passwordEncoder.encode(userDto.getPassword()))
                .name(userDto.getName())
                .userGroup(userGroup)
                .roles(Collections.singleton(role))
                .build();
        try {
            userRepository.save(user);
            return user;
        } catch (Exception e) {
            log.error("회원가입 실패", e);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "회원가입에 실패했습니다.");
        }
    }


    /** ====================================================
     * <아래는 UserDetailsService 구현부>
     * =====================================================*/

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("사용자를 찾을 수 없습니다."));
        return org.springframework.security.core.userdetails.User.builder()
                .username(user.getUsername())
                .password(user.getPassword())
                .authorities(getAuthorities(user.getRoles()))
                .build();
    }

    private Collection<? extends GrantedAuthority> getAuthorities(
            Collection<Role> roles) {

        return getGrantedAuthorities(getPrivileges(roles));
    }

    private List<GrantedAuthority> getGrantedAuthorities(List<String> privileges) {
        List<GrantedAuthority> authorities = new ArrayList<>();
        for (String privilege : privileges) {
            authorities.add(new SimpleGrantedAuthority(privilege));
        }
        return authorities;
    }

    private List<String> getPrivileges(Collection<Role> roles) {

        List<String> privileges = new ArrayList<>();
        List<Privilege> collection = new ArrayList<>();
        for (Role role : roles) {
//            privileges.add(role.getName());  // 이거는 role 이름을 권한으로 인식하는데 왜 하는지 모름
            collection.addAll(role.getPrivileges());
        }
        for (Privilege item : collection) {
            privileges.add(item.getName());
        }
        return privileges;
    }
}
