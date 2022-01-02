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

import static com.lti.bank.webapp.servlet.ServletPaths.*;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import com.lti.bank.webapp.commons.ServiceException;
import com.lti.bank.webapp.model.ClientInfo;
import com.lti.bank.webapp.model.User;
import com.lti.bank.webapp.model.UserStatus;
import com.lti.bank.webapp.service.*;


@WebServlet(LOGIN)
public class LoginServlet extends DefaultServlet {
	private static final long serialVersionUID = 1L;
	private UserDAO userDAO = new UserDAOImpl();

	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		try {
			String userName = req.getParameter("username");
			User user = userDAO.loadUser(userName);
			if (user != null && (user.getStatus() == UserStatus.APPROVED)) {
				req.login(userName, req.getParameter("password"));
				HttpSession session = req.getSession(true);
				session.setAttribute("authenticatedUser", req.getRemoteUser());
				setUserId(req, user.getId());
				if (req.isUserInRole("client")) {
					redirect(resp, CLIENT_DASHBOARD_PAGE);
				} else if (req.isUserInRole("staff")) {
					redirect(resp, STAFF_DASHBOARD_PAGE);
				}
				return;
			}
//            System.out.println(">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>");
//
//            userDAO.blockUser(user);
//            System.out.println(">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>");

            User user2 = userDAO.loadUser(userName);

//            if (user2.getStatus() == UserStatus.BLOCKED) {
//                EmailServiceImp emailServiceImp = new EmailServiceImp();
//                ClientInfoDAO clientInfo = new ClientInfoDAOImpl();
//                clientInfo.loadAccountInfo(user2.getUserName());
//                ClientInfo clientInfo1 = clientInfo.loadAccountInfo(user2.getUserName());
//                emailServiceImp.sendMail(clientInfo1.getEmail(), "Your Account Has Been Locked", "Your Account has been suspended due to suspected misuse of service. This is part of Digital Banking's Security Feature, Please email us to have your account renewed.");
//            }

			sendError(req, "Invalid username/password!");
		} catch(ServletException | ServiceException ex) {
			sendError(req, ex.getMessage());
		}
		forward(req, resp);
	}

}
