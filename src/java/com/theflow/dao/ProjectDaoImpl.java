/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.theflow.dao;

import com.theflow.domain.Project;
import org.apache.log4j.Logger;
import org.hibernate.SessionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

/**
 *
 * @author Stas
 */
@Repository("projectDao")
public class ProjectDaoImpl implements ProjectDao{
    
    static final Logger logger = Logger.getLogger(ProjectDao.class.getName());
    
    @Autowired
    private SessionFactory sessionFactory;

    @Override
    public void saveProject(Project project) {
    }

    @Override
    public void updateProject(Project project) {
    }

    @Override
    public void removeProject(int id) {
    }

    @Override
    public Project getProject(int id) {
        logger.debug("***************inside ProjectService***********project id: " + id);
        return (Project)sessionFactory.openSession().get(Project.class, id);
    }
    
}
