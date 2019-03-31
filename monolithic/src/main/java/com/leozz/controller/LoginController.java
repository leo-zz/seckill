package com.leozz.controller;

import com.leozz.dto.ResultDTO;
import com.leozz.entity.User;
import com.leozz.util.cache.UserLocalCache;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.util.Date;

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
    //springmvc请求参数获取的几种方法,参考https://www.cnblogs.com/xiaoxi/p/5695783.html
    public ResultDTO userLogin(HttpServletRequest request,@RequestParam("id") Long userId,@RequestParam("password") String password) {
        HttpSession session = request.getSession();
        Long userId1 = (Long)session.getAttribute("userId");
        if(userId1!=null){
            return new ResultDTO(false, "请勿重复登录");
        }

        if(userId==null||password==null){
            return new ResultDTO(false, "账号密码不能为空");
        }

        User user = userLocalCache.selectUserById(userId);
        if (user == null) {
            return new ResultDTO(false, "账号不存在");
        }

        if (password.equals(user.getPassword())) {
            session.setAttribute("userId", userId);
            //更新用户的登录信息
            userLocalCache.updateUserLoginStateById(userId);
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
        return new ResultDTO(true, "退出登录成功");
    }

}
