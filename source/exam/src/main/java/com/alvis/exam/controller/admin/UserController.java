package com.alvis.exam.controller.admin;

import com.alvis.exam.base.BaseApiController;
import com.alvis.exam.base.RestResponse;
import com.alvis.exam.domain.User;
import com.alvis.exam.service.AuthenticationService;
import com.alvis.exam.service.UserService;
import com.alvis.exam.viewmodel.admin.user.UserCreateVM;
import com.alvis.exam.viewmodel.admin.user.UserPageRequestVM;
import com.alvis.exam.utility.PageInfoHelper;
import com.alvis.exam.viewmodel.admin.user.UserResponseVM;
import com.alvis.exam.viewmodel.admin.user.UserUpdateVM;
import com.github.pagehelper.PageInfo;
import lombok.AllArgsConstructor;

import org.apache.commons.lang3.StringUtils;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.Date;
import java.util.UUID;


/**
 * @author alvis
 */
@RestController("AdminUserController")
@RequestMapping(value = "/api/admin/user")
@AllArgsConstructor
public class UserController extends BaseApiController {

    private final UserService userService;
    private final AuthenticationService authenticationService;


    @RequestMapping(value = "/page/list", method = RequestMethod.POST)
    public RestResponse<PageInfo<UserResponseVM>> pageList(@RequestBody UserPageRequestVM model) {
        PageInfo<User> pageInfo = userService.userPage(model);
        PageInfo<UserResponseVM> page = PageInfoHelper.copyMap(pageInfo, d -> UserResponseVM.from(d));
        return RestResponse.ok(page);
    }

    @RequestMapping(value = "/select/{id}", method = RequestMethod.POST)
    public RestResponse<UserResponseVM> select(@PathVariable Integer id) {
        User user = userService.getUserById(id);
        UserResponseVM userVm = UserResponseVM.from(user);
        return RestResponse.ok(userVm);
    }

    @RequestMapping(value = "/current", method = RequestMethod.POST)
    public RestResponse<UserResponseVM> current() {
        User user = getCurrentUser();
        UserResponseVM userVm = UserResponseVM.from(user);
        return RestResponse.ok(userVm);
    }


    @RequestMapping(value = "/edit", method = RequestMethod.POST)
    public RestResponse<User> edit(@RequestBody @Valid UserCreateVM model) {
        if (model.getId() == null) {
            User existUser = userService.getUserByUserName(model.getUserName());
            if (null != existUser) {
                return new RestResponse<>(2, "用户已存在");
            }
        }
        if (StringUtils.isBlank(model.getBirthDay())) {
            model.setBirthDay(null);
        }
        User user = modelMapper.map(model, User.class);
        String encodePwd = authenticationService.pwdEncode(model.getPassword());
        user.setPassword(encodePwd);
        user.setUserUuid(UUID.randomUUID().toString());
        user.setCreateTime(new Date());
        user.setLastActiveTime(new Date());
        if (model.getId() == null) {
            userService.insertByFilter(user);
        } else {
            userService.updateByIdFilter(user);
        }
        return RestResponse.ok(user);
    }


    @RequestMapping(value = "/update", method = RequestMethod.POST)
    public RestResponse update(@RequestBody @Valid UserUpdateVM model) {
        User user = userService.selectById(getCurrentUser().getId());
        modelMapper.map(model, user);
        userService.updateByIdFilter(user);
        return RestResponse.ok();
    }


}
