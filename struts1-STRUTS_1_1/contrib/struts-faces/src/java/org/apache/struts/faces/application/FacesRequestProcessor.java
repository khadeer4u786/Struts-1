/*
 * $Header: /home/cvs/jakarta-struts/contrib/struts-faces/src/java/org/apache/struts/faces/application/FacesRequestProcessor.java,v 1.2 2003/06/04 17:38:13 craigmcc Exp $
 * $Revision: 1.2 $
 * $Date: 2003/06/04 17:38:13 $
 *
 * ====================================================================
 *
 * The Apache Software License, Version 1.1
 *
 * Copyright (c) 2002 The Apache Software Foundation.  All rights
 * reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in
 *    the documentation and/or other materials provided with the
 *    distribution.
 *
 * 3. The end-user documentation included with the redistribution, if
 *    any, must include the following acknowlegement:
 *       "This product includes software developed by the
 *        Apache Software Foundation (http://www.apache.org/)."
 *    Alternately, this acknowlegement may appear in the software itself,
 *    if and wherever such third-party acknowlegements normally appear.
 *
 * 4. The names "The Jakarta Project", "Struts", and "Apache Software
 *    Foundation" must not be used to endorse or promote products derived
 *    from this software without prior written permission. For written
 *    permission, please contact apache@apache.org.
 *
 * 5. Products derived from this software may not be called "Apache"
 *    nor may "Apache" appear in their names without prior written
 *    permission of the Apache Group.
 *
 * THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED.  IN NO EVENT SHALL THE APACHE SOFTWARE FOUNDATION OR
 * ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF
 * USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT
 * OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF
 * SUCH DAMAGE.
 * ====================================================================
 *
 * This software consists of voluntary contributions made by many
 * individuals on behalf of the Apache Software Foundation.  For more
 * information on the Apache Software Foundation, please see
 * <http://www.apache.org/>.
 *
 */

package org.apache.struts.faces.application;


import java.io.IOException;
import javax.faces.FacesException;
import javax.faces.FactoryFinder;
import javax.faces.component.UICommand;
import javax.faces.component.UIComponent;
import javax.faces.component.UIForm;
import javax.faces.context.FacesContext;
import javax.faces.event.ActionEvent;
import javax.faces.tree.Tree;
import javax.faces.tree.TreeFactory;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.struts.Globals;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.action.RequestProcessor;
import org.apache.struts.faces.Constants;


/**
 * <p>Concrete implementation of <code>RequestProcessor</code> that
 * implements the standard Struts request processing lifecycle on a
 * request that was received as an <code>ActionEvent</code> by our
 * associated <code>ActionListener</code>.  It replaces the request processor
 * instance normally configured by Struts, so it must support non-Faces
 * requests as well.</p>
 *
 * @author Craig R. McClanahan
 * @version $Revision: 1.2 $ $Date: 2003/06/04 17:38:13 $
 */

public class FacesRequestProcessor extends RequestProcessor {


    // ----------------------------------------------------- Instance Variables


    /**
     * <p>The log instance for this class.</p>
     */
    protected static Log log = LogFactory.getLog(FacesRequestProcessor.class);



    // ------------------------------------------------------ Protected Methods


    /**
     * <p>Do a forward to specified uri using request dispatcher.
     * This method is used by all internal method needing to transfer
     * control to a new resource to create the response.  For a Faces
     * request, select the new response component tree in addition to
     * the standard processing.</p>
     *
     * @param uri Uri or Definition name to forward
     * @param request Current page request
     * @param response Current page response
     */
    protected void doForward(String uri, HttpServletRequest request,
                             HttpServletResponse response)
        throws IOException, ServletException
    {

        if (log.isTraceEnabled()) {
            log.trace("Forwarding to URI '" + uri + "'");
        }

        // On a Faces request, select the new component tree
        FacesContext context = FacesContext.getCurrentInstance();
        if ((context != null) && !uri.startsWith("/faces/")) {
            selectTree(context, uri);
        } else {
            super.doForward(uri, request, response);
            if (context != null) {
                context.responseComplete();
            }
        }

    }


    /**
     * <p>Do a include to specified uri using request dispatcher.
     * This method is used by all internal method needing to transfer
     * control to a new resource to create the response.  For a Faces
     * request, select the new response component tree in addition to
     * the standard processing.</p>
     *
     * @param uri Uri or Definition name to forward
     * @param request Current page request
     * @param response Current page response
     */
    protected void doInclude(String uri, HttpServletRequest request,
                             HttpServletResponse response)
        throws IOException, ServletException
    {

        if (log.isTraceEnabled()) {
            log.trace("Including to URI '" + uri + "'");
        }

        // On a Faces request, select the new component tree
        FacesContext context = FacesContext.getCurrentInstance();
        if ((context != null)  && !uri.startsWith("/faces/")) {
            ; // FIXME - JSF spec is probably broken w.r.t includes!
            selectTree(context, uri);
        } else {
            super.doInclude(uri, request, response);
            if (context != null) {
                context.responseComplete();
            }
        }

    }


    /**
     * <p>Identify and return the path component (from the request URI for a
     * non-Faces request, or from the form event for a Faces request)
     * that we will use to select an ActionMapping to dispatch with.
     * If no such path can be identified, create an error response and return
     * <code>null</code>.</p>
     *
     * @param request The servlet request we are processing
     * @param response The servlet response we are creating
     *
     * @exception IOException if an input/output error occurs
     */
    protected String processPath(HttpServletRequest request,
                                 HttpServletResponse response)
        throws IOException {

        // Handle non-Faces requests in the usual way
        ActionEvent event = (ActionEvent)
            request.getAttribute(Constants.ACTION_EVENT_KEY);
        if (event == null) {
            if (log.isTraceEnabled()) {
                log.trace("Performing standard processPath() processing");
            }
            return (super.processPath(request, response));
        }

        // Calculate the path from the form name
        UIComponent component = event.getComponent();
        if (log.isTraceEnabled()) {
            log.trace("Locating form parent for command component " +
                      event.getComponent());
        }
        while (!(component instanceof UIForm)) {
            component = component.getParent();
            if (component == null) {
                log.warn("command component was not nested in a form!");
                return (null);
            }
        }
        if (log.isDebugEnabled()) {
            log.debug("Returning selected path of " +
                      ((UIForm) component).getFormName());
        }
        return (((UIForm) component).getFormName());

    }


    /**
     * <p>Populate the properties of the specified <code>ActionForm</code>
     * instance from the request parameters included with this request,
     * <strong>IF</strong> this is a non-Faces request.  For a Faces request,
     * this will have already been done by the <em>Update Model Values</em>
     * phase of the request processing lifecycle, so all we have to do is
     * recognize whether the request was cancelled or not.</p>
     *
     * @param request The servlet request we are processing
     * @param response The servlet response we are creating
     * @param form The ActionForm instance we are populating
     * @param mapping The ActionMapping we are using
     *
     * @exception ServletException if thrown by RequestUtils.populate()
     */
    protected void processPopulate(HttpServletRequest request,
                                   HttpServletResponse response,
                                   ActionForm form,
                                   ActionMapping mapping)
        throws ServletException {

        // Are we processing a Faces request?
        ActionEvent event = (ActionEvent)
            request.getAttribute(Constants.ACTION_EVENT_KEY);

        // Handle non-Faces requests in the usual way
        if (event == null) {
            if (log.isTraceEnabled()) {
                log.trace("Performing standard processPopulate() processing");
            }
            super.processPopulate(request, response, form, mapping);
            return;
        }

        // Faces Requests require no processing for form bean population
        // so we need only check for the cancellation command name
        if (log.isTraceEnabled()) {
            log.trace("Faces request, so no processPopulate() processing");
        }
        UIComponent source = event.getComponent();
        if (source instanceof UICommand) {
            UICommand command = (UICommand) source;
            if ("cancel".equals(((UICommand) source).getCommandName())) {
                if (log.isTraceEnabled()) {
                    log.trace("Faces request with cancel button pressed");
                }
                request.setAttribute(Globals.CANCEL_KEY, Boolean.TRUE);
            }
        }

    }


    // -------------------------------------------------------- Private Methods


    /**
     * <p>Select the response component tree that corresponds to the specified
     * URI, which is being forwarded to or included.</p>
     *
     * @param context FacesContext for the request we are processing
     * @param uri Context-relative URI of the new resource
     */
    private void selectTree(FacesContext context, String uri) {

        if (log.isTraceEnabled()) {
            log.trace("Selecting tree '" + uri + "'");
        }

        // Look up the Tree that corresponds to this URI
        try {
            log.debug("selectTree(" + uri + ")");
            TreeFactory tfactory = (TreeFactory)
                FactoryFinder.getFactory(FactoryFinder.TREE_FACTORY);
            Tree tree = tfactory.getTree(context, uri);
            context.setTree(tree);
        } catch (FacesException e) {
            log.error("selectTree(" + uri + ")", e);
        }

    }


}
