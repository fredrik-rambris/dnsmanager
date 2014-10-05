/* PermissionTag.java (c) 2011 Fredrik Rambris. All rights reserved */
package com.rambris.dnsmanager.web;

import java.sql.SQLException;

import javax.servlet.jsp.JspTagException;
import javax.servlet.jsp.jstl.core.ConditionalTagSupport;

import com.rambris.dnsmanager.Domain;
import com.rambris.dnsmanager.User;

/**
 * @author Fredrik Rambris <fredrik@rambris.com>
 * @version $Id$
 * 
 */
public class PermissionTag extends ConditionalTagSupport
{
	private Domain domain = null;

	/**
	 * 
	 */
	public PermissionTag()
	{
		super();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.servlet.jsp.jstl.core.ConditionalTagSupport#condition()
	 */
	@Override
	protected boolean condition() throws JspTagException
	{
		Object o = pageContext.getSession().getAttribute("currentUser");
		if (o != null && o instanceof User && domain != null)
		{
			User currentUser = (User) o;
			try
			{
				return currentUser.isAuthorizedFor(domain);
			}
			catch (SQLException e)
			{

			}
		}
		// TODO Auto-generated method stub
		return false;
	}

	/**
	 * @param domain
	 *            the domain to set
	 */
	public void setDomain(Domain domain)
	{
		this.domain = domain;
	}

}
