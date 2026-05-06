package com.imooc.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.github.pagehelper.PageHelper;
import com.imooc.base.BaseInfoProperties;
import com.imooc.enums.UserRole;
import com.imooc.exceptions.GraceException;
import com.imooc.grace.result.ResponseStatusEnum;
import com.imooc.mapper.UsersMapper;
import com.imooc.pojo.Users;
import com.imooc.pojo.bo.ModifyUserBO;
import com.imooc.service.UserService;
import com.imooc.utils.PagedGridResult;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class UserServiceImpl extends BaseInfoProperties implements UserService {

    @Autowired
    private UsersMapper usersMapper;

    @Transactional
    @Override
    public void modifyUserInfo(ModifyUserBO userBO) {

        String userId = userBO.getUserId();
        if (StringUtils.isBlank(userId))
            GraceException.display(ResponseStatusEnum.USER_INFO_UPDATED_ERROR);

        Users pendingUser = new Users();
        pendingUser.setId(userId);
        pendingUser.setUpdatedTime(LocalDateTime.now());

        BeanUtils.copyProperties(userBO, pendingUser);

        usersMapper.updateById(pendingUser);
    }

    @Override
    public Users getById(String uid) {
        return usersMapper.selectById(uid);
    }

    @Override
    public Long getCountsByCompanyId(String companyId) {

        Long counts = usersMapper.selectCount(
                            new QueryWrapper<Users>()
                                .eq("hr_in_which_company_id", companyId)
        );

        return counts;
    }

    @Transactional
    @Override
    public void updateUserCompanyId(String hrUserId,
                                    String realname,
                                    String companyId) {
        Users hrUser = new Users();
        hrUser.setId(hrUserId);
        hrUser.setRealName(realname);
        hrUser.setHrInWhichCompanyId(companyId);

        hrUser.setUpdatedTime(LocalDateTime.now());

        usersMapper.updateById(hrUser);
    }

    @Transactional
    @Override
    public void updateUserToHR(String uid) {

        Users hrUser = new Users();
        hrUser.setId(uid);
        hrUser.setRole(UserRole.RECRUITER.type);

        hrUser.setUpdatedTime(LocalDateTime.now());

        usersMapper.updateById(hrUser);
    }

    @Transactional
    @Override
    public void updateUserToCand(String hrUserId) {

        Users hrUser = new Users();
        hrUser.setId(hrUserId);
        hrUser.setRole(UserRole.CANDIDATE.type);

        /**
         * update-strategy: not_empty
         */
        hrUser.setHrInWhichCompanyId("0");

        hrUser.setUpdatedTime(LocalDateTime.now());

        usersMapper.updateById(hrUser);
    }

    @Override
    public PagedGridResult getHRList(String companyId, Integer page, Integer limit) {

        PageHelper.startPage(page, limit);

        List<Users> hrList = usersMapper.selectList(
                                new QueryWrapper<Users>()
                                    .eq("hr_in_which_company_id", companyId)
        );

        return setterPagedGrid(hrList, page);
    }

    @Override
    public List<Users> getByIds(List<String> userIds) {
        return usersMapper.selectList(
                new QueryWrapper<Users>()
                    .in("id", userIds)
        );
    }
}
