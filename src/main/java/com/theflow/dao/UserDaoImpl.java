package com.theflow.dao;

import com.theflow.domain.User;
import com.theflow.domain.UserCompany;
import com.theflow.service.FlowUserDetailsService;
import java.util.ArrayList;
import java.util.List;
import org.apache.log4j.Logger;
import org.hibernate.Criteria;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.criterion.Restrictions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Repository;

/**
 *
 * @author Stas
 */
@Repository("userDao")
public class UserDaoImpl implements UserDao {

    static final Logger logger = Logger.getLogger(UserDaoImpl.class.getName());

    @Autowired
    private SessionFactory sessionFactory;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    public int saveUser(User user) {
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        sessionFactory.getCurrentSession().save(user);
        return user.getUserId();
    }

    @Override
    public void updateUser(User user) {
        Session session = sessionFactory.getCurrentSession();
        session.update(user);
    }

    @Override
    public void removeUser(int userId, int companyId) {
        Session session = sessionFactory.getCurrentSession();
        String hql = "delete from UserCompany uc where uc.user.userId = :userId and uc.company.companyId = :companyId";
        Query q = session.createQuery(hql);
        q.setParameter("userId", userId);
        q.setParameter("companyId", companyId);
        q.executeUpdate();
    }

    @Override
    public User getUserById(int id) {
        return (User) sessionFactory.getCurrentSession().get(User.class, id);
    }

    @Override
    public User findUserByEmail(String email) {
        List<User> users = new ArrayList<>();

        users = sessionFactory.getCurrentSession()
                .createQuery("from User where email=?")
                .setParameter(0, email)
                .list();
        if (users.size() > 0) {
            return users.get(0);
        } else {
            return null;
        }
    }

    @Override
    public User getCurrentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return findUserByEmail(auth.getName());
    }

    @Override
    public List<User> getAllUsers() {
        Session session = sessionFactory.getCurrentSession();
        FlowUserDetailsService.User principal = getPrincipal();
        int companyId = principal.getCompanyId();
        String hql = "select uc.user from UserCompany uc where uc.company.companyId = :companyId";
        Query q = session.createQuery(hql);
        q.setParameter("companyId", companyId);
        List<User> users = (List<User>) q.list();
        return users;
    }

    private FlowUserDetailsService.User getPrincipal() {
        return (FlowUserDetailsService.User) SecurityContextHolder
                .getContext()
                .getAuthentication()
                .getPrincipal();
    }
}
