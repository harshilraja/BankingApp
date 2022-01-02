
package com.lti.bank.webapp.service;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import com.lti.bank.webapp.commons.ServiceException;

public class TransactionCodesDAOImp extends AbstractDAOImpl implements TransactionCodesDAO {

	@Override
	public synchronized void create(List<String> codes, int userId) throws ServiceException {
		Connection conn = connectDB();
		PreparedStatement ps = null;
		try {

			StringBuilder query = new StringBuilder();
			query.append("INSERT INTO transaction_code(code, user_id, used)"
					+ " VALUES ");
			int idx = 1;
			for (int i = 0; i < codes.size(); i++) {
				query.append("(?, ?, ?)");
				if (i < (codes.size() - 1)) {
					query.append(", ");
				}
			}
			ps = prepareStmt(conn, query.toString());
			for (int i = 0; i < codes.size(); i++) {
				ps.setString(idx++, codes.get(i));
				ps.setInt(idx++, userId);
				ps.setBoolean(idx++, false);
			}
			int rowNum = ps.executeUpdate();
			if (rowNum == 0) {
				throw new SQLException("Update failed, no rows affected!");
			}
		} catch (SQLException e) {
			throw ServiceException.wrap(e);
		} finally {
            closeDb(conn, ps, null);
        }
	}

	@Override
	public synchronized void updateUsage(String code, int userId) throws ServiceException {
		Connection conn = connectDB();
		PreparedStatement ps = null;
		String acode = "\"" + code + "\"";
		try {
			String query = String.format("UPDATE transaction_code SET used = 1 WHERE code=%s",acode);
			ps = prepareStmt(conn, query);
			int rowNum = ps.executeUpdate();
			if (rowNum == 0) {
				throw new SQLException("Update Failed, the code has expired!!");
			}
		} catch (SQLException e) {
			throw ServiceException.wrap(e);
		} finally {
			closeDb(conn, ps, null);
		}

	}

	@Override
	public Boolean validCode(String code, int userId) throws ServiceException {
		Connection conn = connectDB();
		PreparedStatement ps = null;
		ResultSet rs = null;
		String acode = "\"" + code + "\"";
		try {
			String query = String.format("SELECT * FROM transaction_code WHERE code= %s AND user_id = %s AND used = 0", acode, userId);
			ps = prepareStmt(conn, query);
			System.out.println(query);
			rs = ps.executeQuery();
			if (!rs.isBeforeFirst()) {
				throw new SQLException("Your Code is invalid or has expired, please use another valid transaction code emailed to your account. Thank you");
			}
		} catch (SQLException e) {
			throw ServiceException.wrap(e);
		} finally {
            closeDb(conn, ps, null);
        }
		return true;
	}





}
