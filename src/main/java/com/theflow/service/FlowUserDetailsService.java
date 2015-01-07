package com.theflow.service;

import com.theflow.dao.UserDao;
import com.theflow.domain.UserRole;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
//import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 *
 * @author Stas
 */
@Service("userDetailsService")
public class FlowUserDetailsService implements UserDetailsService {

    static final Logger logger = Logger.getLogger(FlowUserDetailsService.class.getName());

    //get user from the database, via Hibernate
    @Autowired
    private UserDao userDao;

    @Transactional(readOnly = true)
    @Override
    public UserDetails loadUserByUsername(String username)
            throws UsernameNotFoundException {
        logger.debug("******inside userDetailsService*******username: " + username);

        com.theflow.domain.User user = userDao.findByEmail(username);

        List<GrantedAuthority> authorities
                = buildUserAuthority(user.getUserRole());

        return buildUserForAuthentication(user, authorities);

    }

    // Converts com.theflow.domain.User user to
    // org.springframework.security.core.userdetails.User
    private User buildUserForAuthentication(com.theflow.domain.User user,
            List<GrantedAuthority> authorities) {
        return new User(user.getEmail(), user.getPassword(), authorities);
    }
//    private User buildUserForAuthentication(com.theflow.domain.User user,
//            List<GrantedAuthority> authorities) {
//        return new User(user.getEmail(), user.getPassword(),
//                true, true, true, true, authorities);
//    }

    private List<GrantedAuthority> buildUserAuthority(Set<UserRole> userRoles) {

        Set<GrantedAuthority> setAuths = new HashSet<>();

        // Build user's authorities
        for (UserRole userRole : userRoles) {
            setAuths.add(new SimpleGrantedAuthority(userRole.getRole()));
        }

        List<GrantedAuthority> Result = new ArrayList<>(setAuths);

        return Result;
    }
//
    private static class User extends org.springframework.security.core.userdetails.User {


        public User(String username, String password, List<GrantedAuthority> authorities) {
            super(username, password, authorities);
        }


        public boolean isAdmin() {
            logger.debug("************inside principle isAdmin method*********auth.length: " + getAuthorities().size());
            for (GrantedAuthority ga : getAuthorities()) {
                if (ga.getAuthority().equals("ROLE_ADMIN")) {
                    return true;
                }
            }
            return false;
        }
    }
}
