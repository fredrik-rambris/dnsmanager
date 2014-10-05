/* LogoutPage.java (c) 2011 Fredrik Rambris. All rights reserved */
package com.rambris.dnsmanager.web;

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
public class LogoutPage extends Page
{

	private static final Logger log = Logger.getLogger(LogoutPage.class);

	/**
	 * @param servlet
	 * @param request
	 * @param response
	 * @param dns
	 */
	public LogoutPage(DNSServlet servlet, HttpServletRequest request, HttpServletResponse response, DNS dns)
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
		User currentUser = (User) getAttribute("currentUser");
		if (currentUser != null)
		{
			log.info("User logged out: " + currentUser.getName());
		}
		removeAttribute("currentUser");
		removeCookie("auth_hash");
		returnTo(true);
	}

}
