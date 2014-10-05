/* ReportPage.java (c) 2011 Fredrik Rambris. All rights reserved */
package com.rambris.dnsmanager.web;

import java.io.IOException;
import java.sql.SQLException;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.rambris.dnsmanager.DNS;
import com.rambris.dnsmanager.DNSLookup;
import com.rambris.dnsmanager.Domain;
import com.rambris.dnsmanager.SOARecord;

/**
 * @author Fredrik Rambris <fredrik@rambris.com>
 * @version $Id$
 */
public class ReportPage extends SuperAdminPage
{

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
	public ReportPage(DNSServlet servlet, HttpServletRequest request, HttpServletResponse response, DNS dns) throws IOException, RedirectedException,
			SQLException, PermissionDeniedException
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
		DNSLookup lookup = dns.getLookup();
		List<Domain> domains = Domain.GetDomains(db);
		ListIterator<Domain> domainIter = domains.listIterator();
		List<Domain> aliases = new LinkedList<Domain>();
		while (domainIter.hasNext())
		{
			Domain domain = domainIter.next();
			SOARecord soaRecord = lookup.querySOA(domain);
			if (soaRecord != null) domain.setSoaRecord(soaRecord);
			else domainIter.remove();
			for (String alias : domain.getAliases())
			{
				Domain aliasDomain = (Domain) domain.clone();
				aliasDomain.setName(alias);
				soaRecord = lookup.querySOA(aliasDomain);
				if (soaRecord != null)
				{
					aliasDomain.setSoaRecord(soaRecord);
					aliases.add(aliasDomain);
				}

			}
		}
		domains.addAll(aliases);
		Collections.sort(domains);
		setAttribute("domains", domains);
		sendDispatch("/domainReport.jsp");
	}
}
