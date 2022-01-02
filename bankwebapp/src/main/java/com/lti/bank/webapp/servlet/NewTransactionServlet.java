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

import static com.lti.bank.webapp.servlet.ServletPaths.NEW_TRANSACTION;

import java.io.IOException;
import java.math.BigDecimal;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.lti.bank.webapp.commons.Helper;
import com.lti.bank.webapp.commons.ServiceException;
import com.lti.bank.webapp.model.ClientInfo;
import com.lti.bank.webapp.model.ClientTransaction;
import com.lti.bank.webapp.model.User;
import com.lti.bank.webapp.service.*;

import java.text.Normalizer;
import java.text.Normalizer.Form;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


@WebServlet(NEW_TRANSACTION)
public class NewTransactionServlet extends DefaultServlet {
	private static final long serialVersionUID = 1L;
	private ClientTransactionDAO clientTransactionDAO = new ClientTransactionDAOImpl();
	private TransactionCodesDAO transactionCodesDAO = new TransactionCodesDAOImp();


	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		try {
			ClientTransaction clientTransaction = new ClientTransaction();
			ClientInfo clientInfo = new ClientInfo();
			User user = new User(getUserId(req));
			clientTransaction.setUser(user);
			clientInfo.setUser(user);
			BigDecimal amount = new BigDecimal(req.getParameter("amount"));
			clientTransaction.setAmount(amount);

			//Transcode may be malicious
			String code = req.getParameter("transcode");
			code = Helper.input_normalizer(code);
			if (Helper.xss_match(code)) {
				throw new ServletException("XSS Injection Attempt");
			}
			clientTransaction.setTransCode(code);
			clientTransaction.setToAccountNum(req.getParameter("toAccountNum"));

			if (transactionCodesDAO.validCode(code,clientInfo.getUser().getId()) && clientTransactionDAO.validTransaction(clientTransaction)) {
				transactionCodesDAO.updateUsage(code, clientInfo.getUser().getId());
				clientTransactionDAO.create(clientTransaction);
				redirect(resp, ServletPaths.CLIENT_DASHBOARD_PAGE);
			} else {
				throw new ServletException("Code is invalid or Account does not have enough Balance");
			}
		} catch (ServiceException e) {
			sendError(req, e.getMessage());
			forward(req, resp);
		}
	}

}
