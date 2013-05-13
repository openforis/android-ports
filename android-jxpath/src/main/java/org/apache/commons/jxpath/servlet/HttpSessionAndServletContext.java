/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.commons.jxpath.servlet;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpSession;

/**
 * Just a structure to hold a ServletRequest and ServletContext together.
 *
 * @author Dmitri Plotnikov
 * @version $Revision: 604304 $ $Date: 2007-12-14 15:31:19 -0600 (Fri, 14 Dec 2007) $
 */
public class HttpSessionAndServletContext {

    private HttpSession session;
    private ServletContext context;

    /**
     * Create a new HttpSessionAndServletContext.
     * @param session HttpSession
     * @param context ServletContext
     */
    public HttpSessionAndServletContext(HttpSession session,
            ServletContext context) {
        this.session = session;
        this.context = context;
    }

    /**
     * Get the session.
     * @return HttpSession
     */
    public HttpSession getSession() {
        return session;
    }

    /**
     * Get the ServletContext.
     * @return ServletContext
     */
    public ServletContext getServletContext() {
        return context;
    }
}
