package com.theflow.dao;

import com.theflow.domain.User;
import com.theflow.domain.UserCompany;
import com.theflow.service.FlowUserDetailsService;
import java.util.List;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Repository;

/**
 *
 * @author Stas
 */
@Repository("userCompanyDao")
public class UserCompanyDaoImpl implements UserCompanyDao{
    
    @Autowired
    private SessionFactory sessionFactory;

    @Override
    public void saveUserCompany(UserCompany userCompany) {
        Session session = sessionFactory.getCurrentSession();
        session.save(userCompany);
    }

    @Override
    public void updateUserCompany(UserCompany userCompany) {
        Session session = sessionFactory.getCurrentSession();
        session.update(userCompany);
    }

    @Override
    public List<User> getUsersByCompanyId(int companyId) {
        Session session = sessionFactory.getCurrentSession();
        String hql = "select uc.user from UserCompany uc where uc.company.companyId = :companyId";
        Query q = session.createQuery(hql);
        q.setParameter("companyId", companyId);
        return  q.list();
    }

    @Override
    public List<UserCompany> getUserCompaniesByUserId(int userId) {
        Session session = sessionFactory.getCurrentSession();
        String hql = "from UserCompany uc where uc.user.userId = :userId";
        Query q = session.createQuery(hql);
        q.setParameter("userId", userId);
        return q.list();
    }

    @Override
    public UserCompany getUserCompanyById(int ucId) {
        Session session = sessionFactory.getCurrentSession();
        String hql = "from UserCompany uc where uc.userCompanyId = :ucId";
        Query q = session.createQuery(hql);
        q.setParameter("ucId", ucId);
        List<UserCompany> userCompanies = q.list();
        if ((userCompanies != null)&&(!userCompanies.isEmpty())) {
            return userCompanies.get(0);
        }
        return null;
    }

    @Override
    public UserCompany getUserCompaniesByUserIdAndCompanyId(int userId, int companyId) {
        Session session = sessionFactory.getCurrentSession();
        String hql = "from UserCompany uc where uc.user.userId = :userId and uc.company.companyId = :companyId";
        Query q = session.createQuery(hql);
        q.setParameter("userId", userId);
        q.setParameter("companyId", companyId);
        List<UserCompany> userCompanies = q.list();
        if ((userCompanies != null)&&(!userCompanies.isEmpty())) {
            return userCompanies.get(0);
        }
        return null;
    }

    @Override
    public List<UserCompany> getUserCompaniesByCompanyId(int companyId) {
        Session session = sessionFactory.getCurrentSession();
        String hql = "from UserCompany uc where uc.company.companyId = :companyId";
        Query q = session.createQuery(hql);
        q.setParameter("companyId", companyId);
        List<UserCompany> userCompanies = q.list();
        if ((userCompanies != null)&&(!userCompanies.isEmpty())) {
            return userCompanies;
        }
        return null;
    }

    @Override
    public List<UserCompany> getOwnCompanies() {
        Session session = sessionFactory.getCurrentSession();
        FlowUserDetailsService.User principal = (FlowUserDetailsService.User)SecurityContextHolder
                .getContext()
                .getAuthentication()
                .getPrincipal();
        String hql = "from UserCompany uc where uc.company.creator.userId = :userId and uc.user.userId = :userId";
        Query q = session.createQuery(hql);
        q.setParameter("userId", principal.getUserId());
        List<UserCompany> userCompanies = q.list();
        if ((userCompanies != null)&&(!userCompanies.isEmpty())) {
            return userCompanies;
        }
        return null;
    }
}
