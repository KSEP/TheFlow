package com.theflow.service;

import com.theflow.dao.CompanyDao;
import com.theflow.dao.UserDao;
import com.theflow.domain.Company;
import com.theflow.domain.UserCompany;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
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

    public FlowUserDetailsService() {
    }

    //get user from the database, via Hibernate
    @Autowired
    @Lazy
    private UserDao userDao;

    @Autowired
    private CompanyDao companyDao;

    @Transactional(readOnly = true)
    @Override
    public UserDetails loadUserByUsername(String input)
            throws UsernameNotFoundException {
        logger.debug("******inside userDetailsService*******input: " + input);

        String[] split = input.split(":");
        if (split.length < 2) {
            com.theflow.domain.User user = userDao.findUserByEmail(input);
            List<GrantedAuthority> authorities = new ArrayList<>();
            authorities.add(new SimpleGrantedAuthority("Cabinet"));
            return new User(user.getEmail(), user.getPassword(), authorities, user.getFirstName(), user.getLastName(), user.getUserId());
        }

        String username = split[0];
        String domain = split[1];

        System.out.println("Username = " + username);
        System.out.println("Subdomain = " + domain);

        Company company = companyDao.getCompanyByAlias(domain);
        int companyId = company.getCompanyId();

        com.theflow.domain.User user = userDao.findUserByEmail(username);
        if (user == null) {
            throw new UsernameNotFoundException("No user found with username: " + input);
        }

        List<GrantedAuthority> authorities
                = buildUserAuthority(getUserRole(user, company));

        return buildUserForAuthentication(user, authorities, company);

    }

    // Converts com.theflow.domain.User user to
    // org.springframework.security.core.userdetails.User
    // Always synchronize with userService.buildPrincipalAfterEditing(UserDto userDto) !!!
    private User buildUserForAuthentication(com.theflow.domain.User user,
            List<GrantedAuthority> authorities, Company company) {
        String companyName = company.getName();
        String companyAlias = company.getAlias();
        return new User(user.getEmail(), user.getPassword(), authorities, user.getFirstName(), user.getLastName(), user.getUserId(), user.isEnabled(), company.getCompanyId(), companyName, companyAlias, authorities.get(0).getAuthority());
    }

    private List<GrantedAuthority> buildUserAuthority(String userRole) {

        Set<GrantedAuthority> setAuths = new HashSet<>();

        // Build user's authorities
        setAuths.add(new SimpleGrantedAuthority(userRole));

        List<GrantedAuthority> Result = new ArrayList<>(setAuths);

        return Result;
    }

    private String getUserRole(com.theflow.domain.User user, Company company) {
        String role = "";
        Set<UserCompany> userCompanies = user.getUserCompanies();
        for (UserCompany userCompany : userCompanies) {
            if (userCompany.getCompany().getCompanyId() == company.getCompanyId()) {
                role = userCompany.getUserRole();
            }
        }
        return role;
    }

    public static class User extends org.springframework.security.core.userdetails.User {

        private String fullName;
        private int userId;
        private int companyId;
        private String firstName;
        private String lastName;
        private String companyName;
        private String companyAlias;
        private String role;
        private String password;

        public User(String username, String password, List<GrantedAuthority> authorities, String firstName, String lastName, int userId, boolean enabled, int companyId, String companyName, String companyAlias, String role) {
            super(username, password, enabled, true, true, true, authorities);
            fullName = firstName + " " + lastName;
            this.userId = userId;
            this.companyId = companyId;
            this.firstName = firstName;
            this.lastName = lastName;
            this.companyName = companyName;
            this.companyAlias = companyAlias;
            this.role = role;
            this.password = password;
        }

        public User(String username, String password, List<GrantedAuthority> authorities, String firstName, String lastName, int userId) {
            super(username, password, true, true, true, true, authorities);
            fullName = firstName + " " + lastName;
            this.firstName = firstName;
            this.lastName = lastName;
            this.role = authorities.get(0).getAuthority();
            this.userId = userId;
            this.password = password;
        }

        public boolean isAdmin() {
            logger.debug("************inside principal isAdmin method*********auth.length: " + getAuthorities().size());
            for (GrantedAuthority ga : getAuthorities()) {
                if (ga.getAuthority().equals("Admin") || ga.getAuthority().equals("Account")) {
                    return true;
                }
            }
            return false;
        }

        public String getFullName() {
            return fullName;
        }

        public int getUserId() {
            return userId;
        }

        public int getCompanyId() {
            return companyId;
        }

        public String getFirstName() {
            return firstName;
        }

        public String getLastName() {
            return lastName;
        }

        public String getCompanyName() {
            return companyName;
        }

        public String getCompanyAlias() {
            return companyAlias;
        }

        public String getRole() {
            return role;
        }
        public String getUserPass() {
            return password;
        }
    }
}
