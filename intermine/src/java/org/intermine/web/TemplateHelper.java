package org.intermine.web;

/*
 * Copyright (C) 2002-2004 FlyMine
 *
 * This code may be freely distributed and modified under the
 * terms of the GNU Lesser General Public Licence.  This should
 * be distributed with the code.  See the LICENSE file for more
 * information or http://www.gnu.org/copyleft/lesser.html.
 *
 */

import java.util.Iterator;
import java.util.ListIterator;
import java.util.Map;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import org.intermine.objectstore.query.ConstraintOp;

/**
 * Static helper routines related to templates.
 *
 * @author  Thomas Riley
 */
public class TemplateHelper
{
    /** Type parameter indicating globally shared template. */
    public static final String GLOBAL_TEMPLATE = "global";
    /** Type parameter indicating group shared template. */
    public static final String SHARED_TEMPLATE = "shared";
    /** Type parameter indicating private user template. */
    public static final String USER_TEMPLATE = "user";
    
    /**
     * Locate TemplateQuery by identifier. The type parameter
     *
     * @param request     the current http request
     * @param identifier  template query identifier/name
     * @param type        type of tempate, either GLOBAL_TEMPLATE, SHARED_TEMPLATE or USER_TEMPLATE
     * @return            the located template query with matching identifier
     */
    public static TemplateQuery findTemplate(HttpServletRequest request,
                                             String identifier,
                                             String type) {
        HttpSession session = request.getSession();
        ServletContext servletContext = session.getServletContext();
        Map templates = null;
        Profile profile = (Profile) session.getAttribute(Constants.PROFILE);
        
        if (USER_TEMPLATE.equals(type)) {
            templates = profile.getSavedTemplates();
        } else if (SHARED_TEMPLATE.equals(type)) {
            // TODO implement shared templates
        } else if (GLOBAL_TEMPLATE.equals(type)) {
            templates = (Map) servletContext.getAttribute(Constants.GLOBAL_TEMPLATE_QUERIES);
        } else {
            throw new IllegalArgumentException("type: " + type);
        }
        
        return (TemplateQuery) templates.get(identifier);
    }
    
    /**
     * Create a new PathQuery with input submitted by user contained within
     * a TemplateForm bean.
     *
     * @param tf        the template form bean
     * @param template  the template query involved
     * @return          a new PathQuery matching template with user supplied constraints
     */
    public static PathQuery templateFormToQuery(TemplateForm tf, TemplateQuery template) {
        PathQuery queryCopy = (PathQuery) template.getQuery().clone();
        
        // Step over nodes and their constraints in order, ammending our
        // PathQuery copy as we go
        int j = 0;
        for (Iterator i = template.getNodes().iterator(); i.hasNext();) {
            PathNode node = (PathNode) i.next();
            for (Iterator ci = template.getConstraints(node).iterator(); ci.hasNext();) {
                Constraint c = (Constraint) ci.next();
                
                // Parse user input
                String op = (String) tf.getAttributeOps("" + (j + 1));
                ConstraintOp constraintOp = ConstraintOp.getOpForIndex(Integer.valueOf(op));
                Object constraintValue = tf.getParsedAttributeValues("" + (j + 1));
                
                // In query copy, replace old constraint with new one
                PathNode nodeCopy = (PathNode) queryCopy.getNodes().get(node.getPath());
                nodeCopy.getConstraints().set(node.getConstraints().indexOf(c),
                                              new Constraint(constraintOp, constraintValue));
                j++;
            }
        }
        
        return queryCopy;
    }
    
    /**
     * Create a TemplateQuery with input submited by user contained within
     * a BuildTemplateForm.
     *
     * @param tf     the BuildTemplateForm bean
     * @param query  the query that will end up as part of the template (clone first)
     * @return       the constructed template query
     */
    public static TemplateQuery buildTemplateQuery(BuildTemplateForm tf, PathQuery query) {
        int j = 0;
        Iterator niter = query.getNodes().entrySet().iterator();
        
        while (niter.hasNext()) {
            Map.Entry entry = (Map.Entry) niter.next();
            PathNode node = (PathNode) entry.getValue();
            
            if (node.isAttribute()) {
                ListIterator citer = node.getConstraints().listIterator();
                while (citer.hasNext()) {
                    Constraint c = (Constraint) citer.next();
                    String key = "" + (j + 1);
                    String desc = (String) tf.getConstraintLabel(key);
                    boolean editable = tf.getConstraintEditable(key);
                    
                    c = new Constraint(c.getOp(), c.getValue(), editable, desc, "c" + key);
                    citer.set(c);
                    
                    j++;
                }
            }
        }
        
        TemplateQuery template = new TemplateQuery(tf.getShortName(),
                                                   tf.getDescription(),
                                                   null, query);
        return template;
    }
}
