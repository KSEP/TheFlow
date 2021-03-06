package com.theflow.service;

import com.theflow.dao.CompanyDao;
import com.theflow.dao.UserCompanyDao;
import com.theflow.dao.UserDao;
import com.theflow.domain.Company;
import com.theflow.domain.User;
import com.theflow.domain.UserCompany;
import com.theflow.dto.CompanyDto;
import com.theflow.dto.PasswordDto;
import com.theflow.dto.UserDto;
import com.theflow.dto.UserProfileDto;
import helpers.UserRoleConstants;
import java.util.List;
import java.util.Set;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import validation.CompanyAliasExistsException;
import validation.CompanyCreatorDeletingException;
import validation.CompanyExistsException;
import validation.EmailExistsException;
import validation.InvalidPasswordException;
import validation.UsernameDuplicationException;

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
    
    @Autowired
    private UserCompanyDao userCompanyDao;
    
    @Autowired
    private PasswordEncoder passwordEncoder;

    //Saves user from registration page. Assignes admin role
    @Override
    public int saveUserAddedAfterRegistration(UserDto userDto) throws CompanyExistsException, CompanyAliasExistsException, EmailExistsException {
        if (companyAliasExist(userDto.getCompanyAlias())) {
            throw new CompanyAliasExistsException("This company alias is alreade registered, please try another one: "
                    + userDto.getCompanyAlias());
        }
        if (emailExist(userDto.getEmail())) {
            throw new EmailExistsException("Account already exists with email: "
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
        user.setEnabled(true);
        
        UserCompany userCompany = new UserCompany();
        
        Company company = new Company(userDto.getCompanyName());
        company.setCreator(user);
        company.setAlias(userDto.getCompanyAlias());
        userDao.saveUser(user);
        
        userCompany.setUser(user);
        userCompany.setCompany(company);
        userCompany.setUserRole(UserRoleConstants.ADMIN.toString());
        

        userCompanyDao.saveUserCompany(userCompany);
        return user.getUserId();
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
    
    private boolean companyAliasExist(String companyAlias) {
        Company company = companyDao.getCompanyByAlias(companyAlias);
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
    public int saveUserAddedByAdmin(UserDto userDto) throws EmailExistsException, UsernameDuplicationException {
        if (userAlreadyAdded(userDto.getEmail())) {
            throw new UsernameDuplicationException("User already added: " + userDto.getEmail());
        }
        if (emailExist(userDto.getEmail())) {
            throw new EmailExistsException("Account already exists with email: "
                    + userDto.getEmail());
        }
        
        User user = new User();
        user.setFirstName(userDto.getFirstName());
        user.setLastName(userDto.getLastName());
        user.setEmail(userDto.getEmail());
        user.setPassword(passwordEncoder.encode(userDto.getPassword()));
        Company company = companyDao.getCompanyByName(userDto.getCompanyName());
        user.setEnabled(true);
        
        UserCompany userCompany = new UserCompany();
        userCompany.setUser(user);
        userCompany.setCompany(company);
        userCompany.setUserRole(UserRoleConstants.USER.toString());
        
        userCompanyDao.saveUserCompany(userCompany);
        return user.getUserId();
    }
    
    @Override
    public void updateUser(UserProfileDto userDto) {
        User user = userDao.getUserById(userDto.getUserId());
        user.setFirstName(userDto.getFirstName());
        user.setLastName(userDto.getLastName());

        userDao.updateUser(user);
    
    }
    @Override
    public void updateUser(User user) {
        userDao.updateUser(user);
    }

    @Override
    public void removeUser(int id) throws CompanyCreatorDeletingException {
        Company company = companyDao.getCompanyById(getPrincipal().getCompanyId());
        if (company.getCreator().getUserId() == id) {
            throw new CompanyCreatorDeletingException("Illegal user deletion");
        }
        userDao.removeUser(id, getPrincipal().getCompanyId());
    }

    @Override
    public User getUserById(int id) {
        return userDao.getUserById(id);
    }

    //change user role on manage users page
    @Override
    public void changeUserRole(String role, int id) {
        User user = userDao.getUserById(id);
        int companyId = getPrincipal().getCompanyId();
        Set<UserCompany> userCompanies = user.getUserCompanies();
        for (UserCompany userCompany : userCompanies) {
            if (userCompany.getCompany().getCompanyId() == companyId) {
                userCompany.setUserRole(role);
                userCompanyDao.updateUserCompany(userCompany);
                break;
            }
        }
    }

    @Override
    public FlowUserDetailsService.User getPrincipal() {
        return (FlowUserDetailsService.User)SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    }

    @Override
    public void addExistingUserToCompany(String email) {
        User user = userDao.findUserByEmail(email);
        Company company = companyDao.getCompanyByName(getPrincipal().getCompanyName());
        UserCompany userCompany = new UserCompany();
        userCompany.setCompany(company);
        userCompany.setUser(user);
        userCompany.setUserRole("User");
        userCompanyDao.saveUserCompany(userCompany);
    }

    private boolean userAlreadyAdded(String email) {
        boolean added = false;
        List<User> users = userCompanyDao.getUsersByCompanyId(getPrincipal().getCompanyId());
        for (User user : users) {
            if (user.getEmail().equals(email)) {
                added = true;
                break;
            }
        }
        return added;
    }

    @Override
    public List<UserCompany> getUserCompaniesForCurrentUser() {
        FlowUserDetailsService.User principal = getPrincipal();
        List<UserCompany> userCompanies = userCompanyDao.getUserCompaniesByUserId(principal.getUserId());
        return userCompanies;
    }

    @Override
    public UserCompany getUserCompanyById(int ucId) {
        return userCompanyDao.getUserCompanyById(ucId);
    }

    @Override
    public void saveNewCompanyFromCabinet(CompanyDto companyDto) throws CompanyAliasExistsException, CompanyExistsException {
        if (companyExist(companyDto.getCompanyName())) {
            throw new CompanyExistsException("There is a company already registered with name: "
                    + companyDto.getCompanyName());
        }
        if (companyAliasExist(companyDto.getCompanyAlias())) {
            throw new CompanyAliasExistsException("This company alias is alreade registered, please try another one: "
                    + companyDto.getCompanyAlias());
        }
        FlowUserDetailsService.User principal = (FlowUserDetailsService.User)SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        Company company = new Company(companyDto.getCompanyName(), companyDto.getCompanyAlias());
        User user = userDao.getUserById(principal.getUserId());
        company.setCreator(user);
        UserCompany uc = new UserCompany();
        uc.setUser(user);
        uc.setCompany(company);
        uc.setUserRole("Admin");
        userCompanyDao.saveUserCompany(uc);
    }

    @Override
    public UserCompany getUserCompanyByUserIdAndCompanyId(int userId, int companyId) {
        return userCompanyDao.getUserCompaniesByUserIdAndCompanyId(userId, companyId);
    }

    @Override
    public List<UserCompany> getUserCompanyByCompanyId(int companyId) {
        return userCompanyDao.getUserCompaniesByCompanyId(companyId);
    }

    @Override
    public List<UserCompany> getOwnCompanies() {
        return userCompanyDao.getOwnCompanies();
    }

    @Override
    public void changePassword(PasswordDto passwordDto) throws InvalidPasswordException {
        checkUserPassword(passwordDto.getPassword());
        User user = userDao.getCurrentUser();
        user.setPassword(passwordEncoder.encode(passwordDto.getNewPassword()));
        userDao.updateUser(user);
    }

    //checks if inputed password matches bd password
    private void checkUserPassword(String password) throws InvalidPasswordException {
        String actualPass = getPrincipal().getUserPass();
        if (!passwordEncoder.matches(password, actualPass)) {
            throw new InvalidPasswordException("Invalid password");
        }
    }

    @Override
    public void addNewCompanyUserExists(String username, String password, String companyName, String companyAlias) throws InvalidPasswordException {
        
        User user = userDao.findUserByEmail(username);
        String actualPass = user.getPassword();
        if (!passwordEncoder.matches(password, actualPass)) {
            throw new InvalidPasswordException("Invalid password");
        }
        Company company = new Company(companyName, companyAlias);
        company.setCreator(user);
        UserCompany uc = new UserCompany();
        uc.setUser(user);
        uc.setCompany(company);
        uc.setUserRole(UserRoleConstants.ADMIN.toString());
        userCompanyDao.saveUserCompany(uc);
    }
}
