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

import java.io.IOException;
import java.sql.Date;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.lti.bank.webapp.commons.ServiceException;
import com.lti.bank.webapp.model.ClientInfo;
import com.lti.bank.webapp.model.Role;
import com.lti.bank.webapp.model.User;
import com.lti.bank.webapp.model.UserRole;
import com.lti.bank.webapp.service.ClientInfoDAO;
import com.lti.bank.webapp.service.ClientInfoDAOImpl;
import com.lti.bank.webapp.service.EmailService;
import com.lti.bank.webapp.service.EmailServiceImp;
import com.lti.bank.webapp.service.UserDAO;
import com.lti.bank.webapp.service.UserDAOImpl;
import com.lti.bank.webapp.service.UserRoleDAO;
import com.lti.bank.webapp.service.UserRoleDAOImpl;

/**
 * @author SUTD
 */
@WebServlet("/register")
public class RegisterServlet extends DefaultServlet {
	private static final long serialVersionUID = 1L;
	private ClientInfoDAO clientAccountDAO = new ClientInfoDAOImpl();
	private UserDAO userDAO = new UserDAOImpl();
	private UserRoleDAO userRoleDAO = new UserRoleDAOImpl();
	private EmailService emailService = new EmailServiceImp();

	public void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		response.setContentType("text/html");
		User user = new User();
		user.setUserName(request.getParameter("username"));
		user.setPassword(request.getParameter("password"));

		ClientInfo clientAccount = new ClientInfo();
		clientAccount.setFullName(request.getParameter("fullName"));
		clientAccount.setFin(request.getParameter("fin"));
		clientAccount.setDateOfBirth(Date.valueOf(request.getParameter("dateOfBirth")));
		clientAccount.setOccupation(request.getParameter("occupation"));
		clientAccount.setMobileNumber(request.getParameter("mobileNumber"));
		clientAccount.setAddress(request.getParameter("address"));
		clientAccount.setEmail(request.getParameter("email"));
		clientAccount.setUser(user);
		
		try {
			userDAO.create(user);
			clientAccountDAO.create(clientAccount);
			UserRole userRole = new UserRole();
			userRole.setUser(user);
			userRole.setRole(Role.client);
			userRoleDAO.create(userRole);
			emailService.sendMail(clientAccount.getEmail(), "SutdBank registration", "Thank you for the registration!");
			sendMsg(request, "You are successfully registered...");
			redirect(response, ServletPaths.WELCOME);
		} catch (ServiceException e) {
			sendError(request, e.getMessage());
			forward(request, response);
		}
	}
}
