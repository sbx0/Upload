package cn.sbx0.upload.service;

import cn.sbx0.upload.dao.UserDao;
import cn.sbx0.upload.entity.User;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;

/**
 * 用户服务层
 */
@Service
public class UserService extends BaseService {
    @Resource
    private UserDao userDao;

    /**
     * 根据id查询用户信息
     *
     * @param id
     * @return
     */
    public User findById(int id) {
        return userDao.findById(id).get();
    }

    // 辅助方法

    /**
     * 根据用户名判断用户是否存在
     *
     * @param name
     * @return
     */
    public boolean existByName(String name) {
        String result = userDao.existsByName(name);
        if (result != null) return true;
        else return false;
    }

    /**
     * 根据cookie查找User
     *
     * @param request
     * @return
     */
    public User getCookieUser(HttpServletRequest request) {
        // 查找是否存在cookie
        User user = new User();
        String[] names = {"ID", "KEY"};
        Cookie[] cookies = BaseService.getCookiesByName(names, request.getCookies());
        try {
            Cookie cookieId = cookies[0];
            Cookie cookieKey = cookies[1];
            if (cookieId.getValue() != null && !cookieId.getValue().equals("")
                    || cookieKey.getValue() != null || !cookieKey.getValue().equals(""))
                if (BaseService.getKey(Integer.parseInt(cookieId.getValue())).equals(cookieKey.getValue()))
                    user = findById(Integer.parseInt(cookieId.getValue()));
            return user;
        } catch (Exception e) {
            return null;
        }
    }


}
