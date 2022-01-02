/*
 * Copyright 2017 SUTD Licensed under the
	Educational Community License, Version 2.0 (the "License"); you may
	not use this file except in compliance with the License. You may
	obtain a copy of the License at

https://opensource.org/licenses/ECL-2.0

	Unless required by applicable law or agreed to in writing,
	software distributed under the License is distributed on an "AS IS"
	BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
	or implied. See the License for the specific language governing
	permissions and limitations under the License.
 */

package com.lti.bank.webapp.servlet;

import static com.lti.bank.webapp.servlet.ServletPaths.CLIENT_DASHBOARD_PAGE;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.lti.bank.webapp.commons.ServiceException;
import com.lti.bank.webapp.model.ClientInfo;
import com.lti.bank.webapp.service.ClientInfoDAO;
import com.lti.bank.webapp.service.ClientInfoDAOImpl;

@WebServlet(CLIENT_DASHBOARD_PAGE)
public class ClientDashboardServlet extends DefaultServlet {
	private static final long serialVersionUID = 1L;
	private ClientInfoDAO clientInforDao = new ClientInfoDAOImpl();
	
	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		try {
			ClientInfo clientInfo = clientInforDao.loadAccountInfo(req.getRemoteUser());
			req.getSession().setAttribute("clientInfo", clientInfo);
		} catch (ServiceException e) {
			sendError(req, e.getMessage());
		}
		forward(req, resp);
	}
}
