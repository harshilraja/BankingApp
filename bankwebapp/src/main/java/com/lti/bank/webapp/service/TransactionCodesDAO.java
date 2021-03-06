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

public interface TransactionCodesDAO {

	void create(List<String> codes, int userId) throws ServiceException;

	void updateUsage(String code, int userId) throws ServiceException;

	Boolean validCode(String code, int userId) throws ServiceException;



}
