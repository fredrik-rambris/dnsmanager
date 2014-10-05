/* UserPage.java (c) 2011 Fredrik Rambris. All rights reserved */
package com.rambris.dnsmanager.web;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;

import com.rambris.dnsmanager.DNS;
import com.rambris.dnsmanager.Domain;
import com.rambris.dnsmanager.NotFoundException;
import com.rambris.dnsmanager.User;

/**
 * @author Fredrik Rambris <fredrik@rambris.com>
 * @version $Id$
 * 
 */
public class UserPage extends SuperAdminPage
{

	private static final Logger log = Logger.getLogger(UserPage.class);

	/**
	 * @param servlet
	 * @param request
	 * @param response
	 * @param dns
	 * @throws SQLException
	 * @throws RedirectedException
	 * @throws IOException
	 * @throws PermissionDeniedException
	 */
	public UserPage(DNSServlet servlet, HttpServletRequest request, HttpServletResponse response, DNS dns) throws IOException, RedirectedException,
			SQLException, PermissionDeniedException
	{
		super(servlet, request, response, dns);
	}

	@Override
	public void run(String path) throws Exception
	{
		if (path == null) path = "";
		String[] parts = path.split("/");
		String userName = "", action = "edit";
		if (parts.length > 1) action = parts[1];
		if (parts.length > 0) userName = parts[0];
		if (userName.isEmpty()) listUsers();
		else if (userName.equals("new"))
		{
			User user = new User(db);
			editUser(user);
		}
		else
		{
			User user = User.GetUserByName(db, userName);
			if (action.equals("delete"))
			{
				user.delete();
				redirect(MODULE);
				return;
			}
			else
			{
				editUser(user);
			}
		}
	}

	/**
	 * @param user
	 * @throws IOException
	 * @throws ServletException
	 * @throws SQLException
	 */
	private void editUser(User user) throws ServletException, IOException, SQLException
	{
		if (getParameter("cancel") != null)
		{
			redirect(MODULE);
			return;
		}

		if (request.getMethod().equalsIgnoreCase("POST"))
		{
			if (getParameter("user_name") != null) user.setName(getParameter("user_name"));
			if (getParameter("password") != null && !getParameter("password").isEmpty()) user.setPassword(getParameter("password"));
			user.setSuperAdmin(getBooleanParameter("superadmin"));
			user.clearDomains();
			if (getIntegerArrayParameter("domains") != null)
			{
				for (int domain_id : getIntegerArrayParameter("domains"))
				{
					try
					{
						Domain domain = Domain.GetDomainById(db, domain_id);
						user.addDomain(domain);
					}
					catch (NotFoundException e)
					{
						log.warn("Domain with id " + domain_id + " not found");
					}
				}
			}
			user.save();
			redirect(MODULE);
		}
		else
		{
			setAttribute("user", user);
			setAttribute("domains", getDomains(user));
			sendDispatch("/editUser.jsp");
		}
	}

	private List<Domain> getDomains(User user) throws SQLException
	{
		List<Domain> domains = Domain.GetDomains(db);

		for (Domain domain : user.getDomains())
		{
			/* It didn't work with a hashset */
			for (Domain d : domains)
			{
				if (domain.getId() == d.getId()) d.setSelected(true);
			}
		}
		return domains;
	}

	/**
	 * @throws SQLException
	 * @throws IOException
	 * @throws ServletException
	 * 
	 */
	private void listUsers() throws SQLException, ServletException, IOException
	{
		setAttribute("users", User.GetUsers(db));
		sendDispatch("/listUsers.jsp");
	}

}