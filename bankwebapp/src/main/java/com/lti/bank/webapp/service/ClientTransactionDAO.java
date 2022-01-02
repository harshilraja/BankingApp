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

package com.lti.bank.webapp.service;

import java.util.List;

import com.lti.bank.webapp.commons.ServiceException;
import com.lti.bank.webapp.model.ClientAccount;
import com.lti.bank.webapp.model.ClientTransaction;
import com.lti.bank.webapp.model.User;


public interface ClientTransactionDAO {

	void create(ClientTransaction clientTransaction) throws ServiceException;

	List<ClientTransaction> load(User user) throws ServiceException;
	
	List<ClientTransaction> loadWaitingList() throws ServiceException;

	void updateDecision(List<ClientTransaction> transactions) throws ServiceException;

	void updateReceiver(ClientTransaction transaction) throws ServiceException;

    void updateSender(ClientTransaction transaction) throws ServiceException;

    Boolean validTransaction(ClientTransaction transaction) throws  ServiceException;


}
