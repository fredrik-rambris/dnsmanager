package com.rambris.dnsmanager;

import org.apache.log4j.Logger;
import org.xbill.DNS.DClass;
import org.xbill.DNS.Message;
import org.xbill.DNS.Name;
import org.xbill.DNS.Section;
import org.xbill.DNS.SimpleResolver;
import org.xbill.DNS.Type;

import com.rambris.Config;

/**
 * Adapter class for org.xbill.DNS
 * 
 * @author Fredrik Rambris
 */
public class DNSLookup
{
	private final Config config;
	private final Logger log = Logger.getLogger(DNSLookup.class);

	public DNSLookup(Config config)
	{
		this.config = config;
	}

	public SOARecord querySOA(Domain domain)
	{
		if (domain.isReverse()) return null;
		if (!domain.isActive()) return null;
		if (domain.getName().startsWith(".")) return null;
		org.xbill.DNS.Record rec;
		try
		{
			SimpleResolver res = new SimpleResolver(config.getString("resolver"));

			rec = org.xbill.DNS.Record.newRecord(Name.fromString(domain.getName(), Name.root), Type.SOA, DClass.IN);
			Message query = org.xbill.DNS.Message.newQuery(rec);
			Message response = res.send(query);
			for (org.xbill.DNS.Record r : response.getSectionArray(Section.ANSWER))
			{
				if (r instanceof org.xbill.DNS.SOARecord)
				{
					org.xbill.DNS.SOARecord soaresponse = (org.xbill.DNS.SOARecord) r;
					return new SOARecord(soaresponse.getHost().toString(), soaresponse.getAdmin().toString(), (int) soaresponse.getSerial(),
							(int) soaresponse.getRefresh(), (int) soaresponse.getRetry(), (int) soaresponse.getExpire(),
							(int) soaresponse.getMinimum(), (int) soaresponse.getTTL());

				}
			}
		}
		catch (Exception e)
		{
			log.error(e.getMessage(), e);
			// We do this because we are not depending on an answer.

		}
		return null;
	}
}
