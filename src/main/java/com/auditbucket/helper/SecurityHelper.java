package com.auditbucket.helper;

import com.auditbucket.registration.model.ISystemUser;
import com.auditbucket.registration.service.SystemUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

/**
 * User: mike
 * Date: 17/04/13
 * Time: 10:11 PM
 */
@Service
public class SecurityHelper {
    @Autowired
    private SystemUserService sysUserService;

    public String isValidUser() {
        return getUserName(true, true);
    }

    public String getLoggedInUser() {
        Authentication a = SecurityContextHolder.getContext().getAuthentication();
        if (a == null)
            throw new SecurityException("User is not authenticated");
        return a.getName();
    }

    public String getUserName(boolean exceptionOnNull, boolean isSysUser) {
        Authentication a = SecurityContextHolder.getContext().getAuthentication();
        if (a == null)
            if (exceptionOnNull)
                throw new SecurityException("User is not authenticated");
            else
                return null;

        if (isSysUser) {
            ISystemUser su = getSysUser(a.getName());
            if (su == null)
                throw new IllegalArgumentException("Not authorised");
        }
        return a.getName();
    }

    public ISystemUser getSysUser(boolean exceptionOnNull) {
        Authentication a = SecurityContextHolder.getContext().getAuthentication();
        if (a == null)
            if (exceptionOnNull)
                throw new SecurityException("User is not authenticated");
            else
                return null;

        return sysUserService.findByName(a.getName());
    }

    public ISystemUser getSysUser(String loginName) {
        return sysUserService.findByName(loginName);
    }
}
