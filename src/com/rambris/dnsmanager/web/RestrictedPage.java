/* RestrictedPage.java (c) 2011 Fredrik Rambris. All rights reserved */
package com.rambris.dnsmanager.web;

import java.io.IOException;
import java.sql.SQLException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;

import com.rambris.dnsmanager.DNS;
import com.rambris.dnsmanager.Domain;
import com.rambris.dnsmanager.User;

/**
 * @author Fredrik Rambris <fredrik@rambris.com>
 * @version $Id$
 * 
 */
public abstract class RestrictedPage extends Page
{
	private final Logger log = Logger.getLogger(RestrictedPage.class);
	protected User currentUser = null;

	/**
	 * @param servlet
	 * @param request
	 * @param response
	 * @param dns
	 * @throws SQLException
	 * @throws RedirectedException
	 * @throws IOException
	 */
	public RestrictedPage(DNSServlet servlet, HttpServletRequest request, HttpServletResponse response, DNS dns) throws IOException,
			RedirectedException, SQLException
	{
		super(servlet, request, response, dns);
		getUser();
	}

	private void getUser() throws IOException, RedirectedException, SQLException
	{
		currentUser = (User) getAttribute("currentUser");

		if (currentUser == null && cookies.get("auth_hash") != null)
		{
			currentUser = User.AuthenticateUser(db, cookies.get("auth_hash"));
			if (currentUser != null)
			{
				setAttribute("currentUser", currentUser, true);
				log.info("Authenticated user by hash: " + currentUser.getName());
				return;
			}
		}

		if (currentUser == null)
		{
			setAttribute("return_to", SELF, true);
			redirect(ROOT + "/login");
			throw new RedirectedException();
		}
	}

	protected void authorizeFor(Domain domain) throws SQLException, PermissionDeniedException
	{
		if (!currentUser.isAuthorizedFor(domain))
		{
			throw new PermissionDeniedException("You do not have permission to access this domain");
		}
	}

	protected String audit(String message, Object... args)
	{
		return String.format(currentUser.getName() + " " + message, args);
	}
}
