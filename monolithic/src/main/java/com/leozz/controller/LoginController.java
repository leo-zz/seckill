package com.leozz.controller;

import com.leozz.dto.ResultDTO;
import com.leozz.entity.User;
import com.leozz.util.cache.UserLocalCache;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

/**
 * @Author: leo-zz
 * @Date: 2019/3/25 17:24
 */
@RestController
public class LoginController {

    @Autowired
    UserLocalCache userLocalCache;

    @RequestMapping("/login")
    @ApiOperation(value = "用户登录接口", notes = "需要页面提交id和password信息")
    public ResultDTO userLogin(HttpServletRequest request, User userParam) {
        Long userId = userParam.getId();
        User user = userLocalCache.selectUserById(userId);

        if (user == null) {
            return new ResultDTO(false, "账号不存在");
        }
        String password = user.getPassword();
        if (userParam.getPassword().equals(password)) {
            HttpSession session = request.getSession();
            session.setAttribute("userId", userId);
            return new ResultDTO(true, "登录成功");
        } else {
            return new ResultDTO(false, "密码错误");
        }
    }

    @RequestMapping("/logout")
    @ApiOperation(value = "用户登出接口", notes = "从session获取userId然后登出")
    public ResultDTO userLogout(HttpServletRequest request) {
        HttpSession session = request.getSession();
        Object userId = session.getAttribute("userId");
        if (userId==null){
            return new ResultDTO(false, "请先登录");
        }
        session.invalidate();//会解绑所有数据
        return new ResultDTO(true, "登录成功");
    }

}
