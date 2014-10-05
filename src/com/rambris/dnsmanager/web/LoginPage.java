/* LoginPage.java (c) 2011 Fredrik Rambris. All rights reserved */
package com.rambris.dnsmanager.web;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;

import com.rambris.dnsmanager.DNS;
import com.rambris.dnsmanager.User;

/**
 * @author Fredrik Rambris <fredrik@rambris.com>
 * @version $Id$
 * 
 */
public class LoginPage extends Page
{
	private static final Logger log = Logger.getLogger(LoginPage.class);

	/**
	 * @param servlet
	 * @param request
	 * @param response
	 * @param dns
	 */
	public LoginPage(DNSServlet servlet, HttpServletRequest request, HttpServletResponse response, DNS dns)
	{
		super(servlet, request, response, dns);
	}

	@Override
	public void run(String path) throws Exception
	{
		User currentUser = (User) getAttribute("currentUser");

		if (currentUser != null)
		{
			returnTo(true);
			return;
		}
		else if (request.getMethod().equalsIgnoreCase("POST"))
		{
			String name = request.getParameter("user_name");
			String password = request.getParameter("password");
			if (name != null && !name.trim().isEmpty() && password != null && !password.trim().isEmpty())
			{
				currentUser = User.AuthenticateUser(db, name.trim(), password.trim());
				if (currentUser != null)
				{
					setAttribute("currentUser", currentUser, true);
					response.addCookie(new Cookie("auth_hash", currentUser.getAuthHash()));
					log.info("Authenticated user by name+password: " + currentUser.getName());
					returnTo(true);
					return;
				}
				flashError("Wrong name or password");
			}
			else
			{
				flashError("Missing email or password");
			}
		}
		else if (cookies.containsKey("auth_hash"))
		{
			currentUser = User.AuthenticateUser(db, cookies.get("auth_hash"));
			if (currentUser != null)
			{
				setAttribute("currentUser", currentUser, true);
				log.info("Authenticated user by hash: " + currentUser.getName());
				returnTo(true);
				return;
			}
			else
			{
				removeCookie("auth_hash");
			}
		}
		sendDispatch("login.jsp");
	}

}
