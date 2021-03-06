/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.theflow.dao;

import com.theflow.domain.User;
import java.util.List;

/**
 *
 * @author Stas
 */
public interface UserDao {
    
    public int saveUser(User user);
    
    public void updateUser(User user);
    
    public void removeUser(int userId, int companyId);
    
    public User getUserById(int id);
    
    public User findUserByEmail(String email);
    
    public User getCurrentUser();
    
    public List<User> getAllUsers();
    
//    public User findUserByUsernameAndCompanyId(String username, int companyId);
}
