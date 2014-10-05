/* PasswordPage.java (c) 2011 Fredrik Rambris. All rights reserved */
package com.rambris.dnsmanager.web;

import java.io.IOException;
import java.sql.SQLException;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.rambris.dnsmanager.DNS;

/**
 * @author Fredrik Rambris <fredrik@rambris.com>
 * @version $Id$
 * 
 */
public class PasswordPage extends RestrictedPage
{

	/**
	 * @param servlet
	 * @param request
	 * @param response
	 * @param dns
	 * @throws IOException
	 * @throws RedirectedException
	 * @throws SQLException
	 */
	public PasswordPage(DNSServlet servlet, HttpServletRequest request, HttpServletResponse response, DNS dns) throws IOException,
			RedirectedException, SQLException
	{
		super(servlet, request, response, dns);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.rambris.dnsmanager.web.Page#run(java.lang.String)
	 */
	@Override
	public void run(String path) throws Exception
	{
		if (request.getMethod().equalsIgnoreCase("POST"))
		{
			if (getParameter("password") != null && !getParameter("password").isEmpty()) currentUser.setPassword(getParameter("password"));
			currentUser.save();
			response.addCookie(new Cookie("auth_hash", currentUser.getAuthHash()));
			super.flashMessage("Password updated");
			redirect(ROOT);
		}
		else
		{
			sendDispatch("/password.jsp");
		}

	}

}
