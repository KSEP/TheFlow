/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.theflow.controller;

import com.theflow.domain.Project;
import com.theflow.dto.ProjectDto;
import com.theflow.service.ProjectService;
import java.util.List;
import java.util.Locale;
import java.util.logging.Level;
import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import org.apache.log4j.Logger;
import org.hibernate.HibernateException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;
import validation.ProjectAliasExistsException;
import validation.ProjectNameExistsException;

/**
 *
 * @author Stas
 */
@Controller
public class ProjectController {
    static final Logger logger = Logger.getLogger(ProjectController.class.getName());

    @Autowired
    private ProjectService projectService;

    @Autowired
    private MessageSource messageSource;

    @PreAuthorize("hasRole('Admin')")
    @RequestMapping(value = "project/save", method = RequestMethod.POST)
    public ModelAndView saveProject(@ModelAttribute(value = "project") @Valid ProjectDto projectDto, BindingResult result) {
        if (result.hasErrors()) {
            return new ModelAndView("project/addproject", "project", projectDto);
        }
        try {
            projectService.saveProject(projectDto);
        } catch (ProjectNameExistsException e) {
            result.rejectValue("projName", "message.projectNameError");
            return new ModelAndView("/project/addproject", "project", projectDto);
        } catch (ProjectAliasExistsException ex) {
            result.rejectValue("projectAlias", "message.projectAliasError");
            return new ModelAndView("/project/addproject", "project", projectDto);
        }

        ModelAndView model = new ModelAndView("redirect:/projects/manage");
        model.addObject("message", messageSource.getMessage("message.addProject.success", null, Locale.ENGLISH) + projectDto.getProjName());
        return model;
    }
    
    @RequestMapping(value = "project/list")
    public ModelAndView getProjectList() {
        List<Project> projects = projectService.getProjectList();
        ModelAndView model = new ModelAndView("project/list");
        model.addObject("projects", projects);
        return model;
    }

    @PreAuthorize("hasAnyRole('Admin','Observer')")
    @RequestMapping(value = "projects/manage", method = RequestMethod.GET)
    public ModelAndView showManageProjectsPage() {
        ModelAndView model = new ModelAndView("project/manage");
        List<Project> projects = projectService.getProjectList();

        model.addObject("projects", projects);
        return model;
    }

    @PreAuthorize("hasRole('Admin')")
    @RequestMapping("project/add")
    public ModelAndView showCreateProjectPage() {
        ModelAndView model = new ModelAndView("project/addproject", "project", new Project());
        return model;
    }


    @PreAuthorize("hasRole('Admin')")
    @RequestMapping(value = "project/remove/{id}", method = RequestMethod.GET)
    public ModelAndView removeProject(@PathVariable(value = "id") int projectId) {
        projectService.removeProject(projectId);
        return new ModelAndView("redirect:/projects/manage");
    }

    @PreAuthorize("hasRole('Admin')")
    @RequestMapping(value = "project/edit/{id}", method = RequestMethod.GET)
    public ModelAndView editProject(@PathVariable(value = "id") int projectId) {

        ModelAndView model = new ModelAndView("project/edit");
        Project project = projectService.getProjectById(projectId);
        model.addObject("project", project);

        return model;
    }

    @PreAuthorize("hasRole('Admin')")
    @RequestMapping(value = "project/update", method = RequestMethod.POST)
    public ModelAndView updateProject(@ModelAttribute(value = "project") @Valid ProjectDto projectDto, BindingResult result) {

        if (result.hasErrors()) {
            return new ModelAndView("project/edit", "project", projectDto);
        }
        
        try {
            projectService.updateProject(projectDto);
        } catch (ProjectNameExistsException ex) {
            result.rejectValue("projName", "message.projectNameError");
            return new ModelAndView("/project/edit", "project", projectDto);
        } catch (ProjectAliasExistsException ex) {
            result.rejectValue("projectAlias", "message.projectAliasError");
            return new ModelAndView("/project/edit", "project", projectDto);
        }

        ModelAndView model = new ModelAndView("/projects/manage");
        model.addObject("message", messageSource.getMessage("label.successUpdatedProject.title", null, Locale.ENGLISH) + projectDto.getProjName());
        return new ModelAndView("redirect:/project/details/" + projectDto.getProjectId());
    }

    @PreAuthorize("hasAnyRole('Admin','Observer')")
    @RequestMapping(value = "project/details/{id}", method = RequestMethod.GET)
    public ModelAndView showProjectDetails(@PathVariable(value = "id") int projectId) {
        Project project = projectService.getProjectById(projectId);
        ModelAndView model = new ModelAndView("project/details");
        model.addObject("project", project);
        return model;
    }
    
    @ExceptionHandler(Exception.class)
    public ModelAndView handleError(HttpServletRequest req, HibernateException exception) {
        logger.error("Request: " + req.getRequestURL() + " exception " + exception);

        ModelAndView mav = new ModelAndView();
        mav.addObject("exception", exception);
        mav.addObject("url", req.getRequestURL());
        mav.setViewName("error/error");
        return mav;
    }
}
