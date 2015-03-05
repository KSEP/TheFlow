package com.theflow.service;

import com.theflow.dao.CompanyDao;
import com.theflow.dao.UserDao;
import com.theflow.domain.Company;
import com.theflow.domain.User;
import com.theflow.dto.UserDto;
import com.theflow.dto.UserProfileDto;
import helpers.UserRoleConstants;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import validation.CompanyExistsException;
import validation.EmailExistsException;

/**
 *
 * @author Stas
 */
@Service
@Transactional
public class UserServiceImpl implements UserService {

    @Autowired
    private CompanyDao companyDao;

    @Autowired
    private UserDao userDao;

    //Saves user from registration page. Assignes admin role
    @Override
    public int saveUserAddedAfterRegistration(UserDto userDto) throws EmailExistsException, CompanyExistsException {
        if (emailExist(userDto.getEmail())) {
            throw new EmailExistsException("There is an account with that email adress: "
                    + userDto.getEmail());
        }
        if (companyExist(userDto.getCompanyName())) {
            throw new CompanyExistsException("There is a company already registered with name: "
                    + userDto.getCompanyName());
        }
        User user = new User();
        user.setFirstName(userDto.getFirstName());
        user.setLastName(userDto.getLastName());
        user.setEmail(userDto.getEmail());
        user.setPassword(userDto.getPassword());
        Company company = new Company(userDto.getCompanyName());
        int companyId = companyDao.saveCompany(company);
        user.setCompanyId(companyId);
        user.setUserRole(UserRoleConstants.ACCOUNT);
        user.setEnabled(true);

        int userId = userDao.saveUser(user);
        return userId;
    }

    private boolean emailExist(String email) {
        User user = userDao.findUserByEmail(email);
        if (user != null) {
            return true;
        }
        return false;
    }

    private boolean companyExist(String companyName) {
        Company company = companyDao.getCompanyByName(companyName);
        if (company != null) {
            return true;
        }
        return false;
    }

    @Override
    public List<User> getAllUsers() {
        return userDao.getAllUsers();
    }

    //add new user to existing company through manage users page. Assignes user role
    @Override
    public int saveUserAddedByAdmin(UserDto userDto) throws EmailExistsException {
        if (emailExist(userDto.getEmail())) {
            throw new EmailExistsException("There is an account with that email adress: "
                    + userDto.getEmail());
        }
        
        User user = new User();
        user.setFirstName(userDto.getFirstName());
        user.setLastName(userDto.getLastName());
        user.setEmail(userDto.getEmail());
        user.setPassword(userDto.getPassword());
        Company company = companyDao.getCompanyByName(userDto.getCompanyName());
        user.setCompanyId(company.getCompanyId());
        user.setEnabled(true);
        user.setUserRole(UserRoleConstants.USER);

        int userId = userDao.saveUser(user);
        return userId;
    }
    
    @Override
    public void updateUser(UserProfileDto userDto) {
        User user = userDao.getUserById(userDto.getUserId());
        user.setFirstName(userDto.getFirstName());
        user.setLastName(userDto.getLastName());

        userDao.updateUser(user);
    }

    @Override
    public void removeUser(int id) {
        userDao.removeUser(id);
    }

    @Override
    public User getUserById(int id) {
        return userDao.getUserById(id);
    }

    //change user role on manage users page
    @Override
    public void changeUserRole(String role, int id) {
        User user = userDao.getUserById(id);
        user.setUserRole(UserRoleConstants.valueOf(role));
        userDao.updateUser(user);
    }

    @Override
    public FlowUserDetailsService.User getPrincipal() {
        return (FlowUserDetailsService.User)SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    }
}
