package springcar.RentalCar.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import springcar.RentalCar.dto.*;
import springcar.RentalCar.dto.user.PrivilegeChangeReq;
import springcar.RentalCar.dto.user.RoleChangeReq;
import springcar.RentalCar.dto.user.RoleDto;
import springcar.RentalCar.dto.user.UserDto;
import springcar.RentalCar.entity.Privilege;
import springcar.RentalCar.entity.Role;
import springcar.RentalCar.entity.User;
import springcar.RentalCar.service.AuthorizeService;
import springcar.RentalCar.service.LoginService;
import springcar.RentalCar.utils.Converter;
import springcar.RentalCar.dto.JsonBody;

import java.util.*;

@Controller
@RequiredArgsConstructor
@PreAuthorize(value = "hasAuthority('ADMIN')")
@RequestMapping("/authorize")
public class AuthorizeController {
    private final LoginService loginService;
    private final AuthorizeService authorizeService;
    private final Converter converter;

    @GetMapping("/manage")
    public String manage() {
        return "authorizeManage";
    }

    @GetMapping("/user/role/manage")
    public String userRole(Model model) {
        List<UserDto> userList = loginService.getAllUserDto();
        model.addAttribute("userList", userList);
        return "userRoleManage";
    }

    @GetMapping("/role/privilege/manage")
    public String rolePrivilege(Model model) {
        List<RoleDto> roleList = loginService.findAllRoleDto();
        model.addAttribute("roleList", roleList);
        return "rolePrivilegeManage";
    }

    @ResponseBody
    @GetMapping("/user/role")
    public JsonBody userRole(@RequestParam("id") Long userId) {
        List<Object> roleLists = new ArrayList<>();
        List<Role> assignedRole = loginService.findUserRoles(userId);
        List<Role> unassignedRole = new ArrayList<>();

        // 전제 role 목록 중에서 assignedRole에 포함된 role 목록을 제외한 목록을 unassignedRole에 추가
        List<Role> allRoles = loginService.findAllRoles();
        for (Role role : allRoles) {
            if (!assignedRole.contains(role)) {
                unassignedRole.add(role);
            }
        }

        roleLists.add(converter.convertToRoleDtoList(assignedRole));
        roleLists.add(converter.convertToRoleDtoList(unassignedRole));
        return JsonBody.builder()
                .message("역할 조회 성공")
                .data(roleLists)
                .build();
    }

    @ResponseBody
    @PostMapping("/user/role")
    public JsonBody userAddRole(@Validated @RequestBody RoleChangeReq req) {
        User user = loginService.findById(req.getUserId());
        return JsonBody.builder()
                .message("역할 부여 성공")
                .data(authorizeService.grantRole(user, req.getRoleId()))
                .build();
    }

    @ResponseBody
    @DeleteMapping("/user/role")
    public JsonBody userDeleteRole(@Validated @RequestBody RoleChangeReq req) {
        User user = loginService.findById(req.getUserId());
        return JsonBody.builder()
                .message("역할 회수 성공")
                .data(authorizeService.revokeRole(user, req.getRoleId()))
                .build();
    }

    @ResponseBody
    @PostMapping("/role")
    public JsonBody roleAdd(@RequestParam String roleName) {
        return JsonBody.builder()
                .message("역할 추가 성공")
                .data(authorizeService.createRole(roleName))
                .build();
    }

    @ResponseBody
    @DeleteMapping("/role")
    public JsonBody roleDelete(@RequestParam String roleName) {
        // 역할에 해당하는 사용자가 없는지 확인 필요. 없는 경우만 삭제 가능
        return JsonBody.builder()
                .message("역할 삭제 성공")
                .data(authorizeService.deleteRole(roleName))
                .build();
    }

    @ResponseBody
    @GetMapping("/role/privilege")
    public JsonBody rolePrivilege(@RequestParam("id") Long roleId) {
        List<Object> privilegeLists = new ArrayList<>();
        List<Privilege> assignedPrivilege = authorizeService.findRolePrivileges(roleId);
        List<Privilege> unassignedPrivilege = new ArrayList<>();

        // 전제 privilege 목록 중에서 assignedPrivilege에 포함된 privilege 목록을 제외한 목록을 unassignedPrivilege에 추가
        List<Privilege> allPrivileges = authorizeService.findAllPrivileges();
        for (Privilege privilege : allPrivileges) {
            if (!assignedPrivilege.contains(privilege)) {
                unassignedPrivilege.add(privilege);
            }
        }

        privilegeLists.add(converter.convertToPrivilegeDtoList(assignedPrivilege));
        privilegeLists.add(converter.convertToPrivilegeDtoList(unassignedPrivilege));
        return JsonBody.builder()
                .message("권한 조회 성공")
                .data(privilegeLists)
                .build();
    }

    @ResponseBody
    @PostMapping("/role/privilege")
    public JsonBody roleAddPrivilge(@Validated @RequestBody PrivilegeChangeReq req) {
        return JsonBody.builder()
                .message("역할에게 권한 부여 성공")
                .data(authorizeService.grantPrivilege(req.getRoleId(), req.getPrivilegeId()))
                .build();
    }

    @ResponseBody
    @DeleteMapping("/role/privilege")
    public JsonBody roleDeletePrivilege(@Validated @RequestBody PrivilegeChangeReq req) {
        return JsonBody.builder()
                .message("역할에서 권한 회수 성공")
                .data(authorizeService.revokePrivilege(req.getRoleId(), req.getPrivilegeId()))
                .build();
    }

    @PostMapping("/privilege")
    public JsonBody privilegeAdd(@RequestParam String privilegeName) {
        return JsonBody.builder()
                .message("권한 추가 성공")
                .data(authorizeService.createPrivilege(privilegeName))
                .build();
    }

    @DeleteMapping("/privilege")
    public JsonBody privilegeDelete(@RequestParam String privilegeName) {
        return JsonBody.builder()
                .message("권한 삭제 성공")
                .data(authorizeService.deletePrivilege(privilegeName))
                .build();
    }
}
