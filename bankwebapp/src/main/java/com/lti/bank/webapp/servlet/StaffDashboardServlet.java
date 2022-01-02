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
import static com.lti.bank.webapp.servlet.ServletPaths.STAFF_DASHBOARD_PAGE;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.lti.bank.webapp.commons.Constants;
import com.lti.bank.webapp.commons.ServiceException;
import com.lti.bank.webapp.commons.StringUtils;
import com.lti.bank.webapp.model.ClientAccount;
import com.lti.bank.webapp.model.ClientInfo;
import com.lti.bank.webapp.model.ClientTransaction;
import com.lti.bank.webapp.model.TransactionStatus;
import com.lti.bank.webapp.model.User;
import com.lti.bank.webapp.model.UserStatus;
import com.lti.bank.webapp.service.ClientAccountDAO;
import com.lti.bank.webapp.service.ClientAccountDAOImpl;
import com.lti.bank.webapp.service.ClientInfoDAO;
import com.lti.bank.webapp.service.ClientInfoDAOImpl;
import com.lti.bank.webapp.service.ClientTransactionDAO;
import com.lti.bank.webapp.service.ClientTransactionDAOImpl;
import com.lti.bank.webapp.service.EmailService;
import com.lti.bank.webapp.service.EmailServiceImp;
import com.lti.bank.webapp.service.TransactionCodesDAO;
import com.lti.bank.webapp.service.TransactionCodesDAOImp;
import com.lti.bank.webapp.service.UserDAO;
import com.lti.bank.webapp.service.UserDAOImpl;

@WebServlet(STAFF_DASHBOARD_PAGE)
public class StaffDashboardServlet extends DefaultServlet {
	public static final String REGISTRATION_DECISION_ACTION = "registrationDecisionAction";
	public static final String TRANSACTION_DECSION_ACTION = "transactionDecisionAction";
	
	private static final long serialVersionUID = 1L;
	private ClientInfoDAO clientInfoDAO = new ClientInfoDAOImpl();
	private UserDAO userDAO = new UserDAOImpl();
	private ClientAccountDAO clientAccountDAO = new ClientAccountDAOImpl();
	private EmailService emailService = new EmailServiceImp();
	private TransactionCodesDAO transactionCodesDAO = new TransactionCodesDAOImp();
	private ClientTransactionDAO clientTransactionDAO = new ClientTransactionDAOImpl();

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		try {
			List<ClientInfo> accountList = clientInfoDAO.loadWaitingList();
			req.getSession().setAttribute("registrationList", accountList);
			List<ClientTransaction> transList = clientTransactionDAO.loadWaitingList();
			req.getSession().setAttribute("transList", transList);
		} catch (ServiceException e) {
			sendError(req, e.getMessage());
		}
		forward(req, resp);
	}
	
	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		String actionType = req.getParameter("actionType");
		if (REGISTRATION_DECISION_ACTION.endsWith(actionType)) {
			try {
				onRegistrationDecisionAction(req, resp);
			} catch (ServiceException e) {
				sendError(req, e.getMessage());
				redirect(resp, STAFF_DASHBOARD_PAGE);
			}
		} else if (TRANSACTION_DECSION_ACTION.equals(actionType)) {
			onTransactionDecisionAction(req, resp);
		}
	}

	private void onRegistrationDecisionAction(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException, ServiceException {
		String[] decisions = req.getParameterValues("decision");
		int[] userIds = toIntegerArray(req.getParameterValues("user_id"));
		String[] userEmails = req.getParameterValues("user_email");
		List<User> users = new ArrayList<User>();
		for (int i = 0; i < userIds.length; i++) {
			int userId = userIds[i];
			Decision decision = Decision.valueOf(decisions[i]);
			if (decision.getStatus() != null) {
				User user = new User();
				user.setId(userId);
				user.setStatus(decision.getStatus());
				users.add(user);
			}
		}
		if (!users.isEmpty()) {
			try {
				userDAO.updateDecision(users);
			} catch (ServiceException e) {
				sendError(req, e.getMessage());
			}
			activateAccount(userEmails, userIds, decisions);
		}
		redirect(resp, STAFF_DASHBOARD_PAGE);
	}
	
	private int[] toIntegerArray(String[] idStrs) {
		int[] result = new int[idStrs.length];
		for (int i = 0; i < idStrs.length; i++) {
			result[i] = Integer.valueOf(idStrs[i]);
		}
		return result;
	}

	private void activateAccount(String[] userEmails, int[] userIds, String[] decisions) throws ServiceException {
		for (int i = 0; i < userIds.length; i++) {
			if (Decision.valueOf(decisions[i]) == Decision.approve) {
				int userId = userIds[i];
				/* init account */
				ClientAccount clientAccount = new ClientAccount();
				clientAccount.setUser(new User(userId));
				clientAccount.setAmount(Constants.INIT_AMOUNT);
				clientAccountDAO.create(clientAccount);
				/* generate and send transaction codes */
				List<String> codes = TransactionCodeGenerator.generateCodes(100);
				transactionCodesDAO.create(codes, userId);
				emailService.sendMail(userEmails[i], "Your account has been approved ",
						"Congratulation, your account has been approved! These are your transaction codes: \n"
								+ StringUtils.join(codes, "\n"));
			}
		}
	}

	private BigDecimal[] convertStringtoBigDecimal(String[] list) {
        BigDecimal montanttt[] = new BigDecimal[list.length];
        for (int i=0; i < list.length; i++) {
            try {
                montanttt[i] = new BigDecimal(list[i]);
            } catch (NumberFormatException e) {
                System.out.println("Exception while parsing: " + list[i]);
            }
        }
        return montanttt;
    }

	private void onTransactionDecisionAction(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
		String[] decisions = req.getParameterValues("decision");
		int[] transIds = toIntegerArray(req.getParameterValues("trans_id"));
		String[] transAccNum = req.getParameterValues("trans_toAccountNum");
        BigDecimal[] transAmount = convertStringtoBigDecimal(req.getParameterValues("trans_amount"));

		List<ClientTransaction> transactions = new ArrayList<ClientTransaction>();
		for (int i = 0; i < transIds.length; i++) {
			int transId = transIds[i];
			Decision decision = Decision.valueOf(decisions[i]);
			if (decision.getStatus() != null) {
				ClientTransaction trans = new ClientTransaction();
				trans.setToAccountNum(transAccNum[i]);
				trans.setAmount(transAmount[i]);
				trans.setId(transId);
				trans.setStatus(decision.getTransStatus());
				if (decision.getStatus().name().equals("APPROVED")) {
                    try {
                        clientTransactionDAO.updateReceiver(trans);
                        clientTransactionDAO.updateSender(trans);
                    } catch (ServiceException e) {
                        sendError(req, e.getMessage());
                    }
                }

				transactions.add(trans);
			}
		}
		if (!transactions.isEmpty()) {
			try {
				clientTransactionDAO.updateDecision(transactions);
			} catch (ServiceException e) {
				sendError(req, e.getMessage());
			}
		}
		redirect(resp, STAFF_DASHBOARD_PAGE);
	}
	
	private static enum Decision {
		waiting(null, null), 
		approve(UserStatus.APPROVED, TransactionStatus.APPROVED), 
		decline(UserStatus.DECLINED, TransactionStatus.DECLINED);
		
		private UserStatus status;
		private TransactionStatus transStatus;
		private Decision(UserStatus status, TransactionStatus transStatus) {
			this.status = status;
			this.transStatus = transStatus;
		}

		public UserStatus getStatus() {
			return status;
		}
		
		public TransactionStatus getTransStatus() {
			return transStatus;
		}
	}
}
