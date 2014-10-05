/* DomainPage.java (c) 2010 Fredrik Rambris. All rights reserved */
package com.rambris.dnsmanager.web;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;

import com.rambris.Util;
import com.rambris.dnsmanager.ARecord;
import com.rambris.dnsmanager.DNS;
import com.rambris.dnsmanager.Domain;
import com.rambris.dnsmanager.Group;
import com.rambris.dnsmanager.InvalidDataException;
import com.rambris.dnsmanager.LOCRecord;
import com.rambris.dnsmanager.MXRecord;
import com.rambris.dnsmanager.Master;
import com.rambris.dnsmanager.NotFoundException;
import com.rambris.dnsmanager.Owner;
import com.rambris.dnsmanager.PTRRecord;
import com.rambris.dnsmanager.Record;
import com.rambris.dnsmanager.SRVRecord;
import com.rambris.dnsmanager.Server;
import com.rambris.dnsmanager.View;

/**
 * @author Fredrik Rambris <fredrik@rambris.com>
 * @version $Id: DomainPage.java 51 2010-09-07 20:53:50Z boost $
 */
public class DomainPage extends RestrictedPage
{
	private static final Logger log = Logger.getLogger(DomainPage.class);

	/**
	 * @param servlet
	 * @param request
	 * @param response
	 * @param dns
	 * @throws SQLException
	 * @throws RedirectedException
	 * @throws IOException
	 */
	public DomainPage(DNSServlet servlet, HttpServletRequest request, HttpServletResponse response, DNS dns) throws IOException, RedirectedException, SQLException
	{
		super(servlet, request, response, dns);
	}

	/*
	 * (non-Javadoc)
	 * @see com.rambris.dnsmanager.web.Page#run(java.lang.String)
	 */
	@Override
	public void run(String path) throws Exception
	{
		if (path == null) path = "";
		String[] parts = path.split("/");
		String subAction = "edit", recordName = "", action = "edit", domainName = "";

		if (parts.length > 3) subAction = parts[3];
		if (parts.length > 2) recordName = parts[2];
		if (parts.length > 1) action = parts[1];
		if (parts.length > 0) domainName = parts[0];

		if (domainName.equals("new"))
		{
			if (!currentUser.isSuperAdmin()) throw new PermissionDeniedException("You are not allowed to create new domains");
			Domain domain = new Domain(db);
			if (getParameter("copyOf") != null)
			{
				domain = Domain.GetDomainByName(db, getParameter("copyOf"));
				domain.getMasters();
				domain.clear();
				if (getParameter("promote") != null) domain.clearAliases();
			}
			editDomain(domain);
		}
		else if (domainName.equals("search"))
		{
			String query = getParameter("q");
			setAttribute("query", query);
			if (query != null && !query.isEmpty())
			{
				List<DomainSearchResult> result = searchDomain(query);
				if (result != null && result.size() == 1)
				{
					DomainSearchResult item = result.get(0);
					if (item.parentDomain == null)
					{
						redirect(MODULE + "/" + item.domainName);
						return;
					}
				}
				setAttribute("result", result);
			}
			sendDispatch("searchDomains.jsp");
			return;
		}
		else if (!domainName.isEmpty())
		{
			Domain domain = Domain.GetDomainByName(db, domainName);
			authorizeFor(domain);

			if (action.equals("record"))
			{
				setAttribute("domain", domain);

				if ("new".equals(recordName))
				{
					if (getParameter("copyOf") != null)
					{
						int recordId = getIntegerParameter("copyOf");
						Record record = domain.getRecord(recordId);
						record.clear();
						editRecord(record);
					}
					else
					{
						String typeName = getParameter("type");
						if (typeName == null)
						{
							if (domain.isReverse()) typeName = "PTR";
							else typeName = "A";
						}
						Record record = domain.createRecord(typeName);
						if (!subAction.isEmpty())
						{
							if ("common".equals(subAction)) setAttribute("group_id", 0);
							else setAttribute("group_id", Group.GetGroupByName(db, subAction).getId());
						}
						editRecord(record);
					}
				}
				else if ("paste".equals(recordName))
				{
					int group_id = 0;
					if (!subAction.isEmpty())
					{
						if ("common".equals(subAction)) group_id = 0;
						else group_id = Group.GetGroupByName(db, subAction).getId();
					}
					setAttribute("group_id", group_id);
					pasteRecords(domain, group_id);
				}
				else if (!recordName.isEmpty())
				{
					int recordId = Integer.parseInt(recordName);
					Record record = domain.getRecord(recordId);
					if (subAction.equals("delete"))
					{
						record.delete();
						log.info(audit("deleted record " + record.getFQDN() + " id=" + record.getId() + " record=" + record.toString(1)));
						redirect(MODULE + "/" + domainName);
						return;
					}
					else
					{
						editRecord(record);
					}
				}
			}
			else if (action.equals("master"))
			{
				setAttribute("domain", domain);
				if ("new".equals(recordName))
				{
					editMaster(domain, null);
				}
				else if (!recordName.isEmpty())
				{
					Master master = domain.getMasterById((int) Util.Long(recordName));

					if ("delete".equals(subAction))
					{
						deleteMaster(domain, master);
					}
					else
					{
						editMaster(domain, master);
					}
				}
			}
			else if (action.equals("alias"))
			{
				setAttribute("domain", domain);

				if ("new".equals(recordName))
				{
					editAlias(domain, null);
				}
				else if (!recordName.isEmpty())
				{
					if ("delete".equals(subAction))
					{
						deleteAlias(domain, recordName);
					}
					else
					{
						editAlias(domain, recordName);
					}
				}
			}
			else if (action.equals("delete"))
			{
				domain.delete();
				redirect(MODULE);
				return;
			}
			else
			{
				if (request.getMethod().equalsIgnoreCase("GET") && getIntegerParameter("report") == 1) domain.setSoaRecord(dns.getLookup().querySOA(domain));
				editDomain(domain);
			}
		}
		else
		{
			listDomains();
		}

	}

	/**
	 * @throws SQLException
	 * @throws IOException
	 * @throws ServletException
	 */
	private void listDomains() throws SQLException, ServletException, IOException
	{
		if (currentUser.isSuperAdmin()) setAttribute("domains", Domain.GetDomains(db));
		else setAttribute("domains", currentUser.getDomains());
		sendDispatch("/listDomains.jsp");
	}

	/**
	 * @param domain
	 * @throws SQLException
	 * @throws IOException
	 * @throws ServletException
	 * @throws NotFoundException
	 * @throws NumberFormatException
	 */
	private void editDomain(Domain domain) throws SQLException, IOException, ServletException, NumberFormatException, NotFoundException
	{
		if (getParameter("cancel") != null)
		{
			redirect(MODULE);
			return;
		}
		if (request.getMethod().equalsIgnoreCase("POST"))
		{

			boolean create = domain.getId() == 0;
			{
				String domainName = getParameter("domain_name");
				if (domainName != null)
				{
					if (create && domainName.matches("[0-9.]+")) domain.setName(Domain.GenerateArpa(domainName));
					else domain.setName(domainName);
				}
			}
			if (getParameter("server_id") != null) domain.setServer(Server.GetServerById(db, getIntegerParameter("server_id")));
			if (getParameter("owner_id") != null) domain.setOwner(Owner.GetOwnerById(db, getIntegerParameter("owner_id")));
			if (getParameter("ttl") != null) domain.setTtl(getParameter("ttl"));
			if (getParameter("serial") != null) domain.setSerial(getLongParameter("serial"));
			if (getBooleanParameter("bump_serial")) domain.bumpSerial();
			if (getParameter("refresh") != null) domain.setRefresh(getParameter("refresh"));
			if (getParameter("retry") != null) domain.setRetry(getParameter("retry"));
			if (getParameter("expire") != null) domain.setExpire(getParameter("expire"));
			if (getParameter("minimum") != null) domain.setMinimum(getParameter("minimum"));
			domain.setActive(getBooleanParameter("active"));

			if (getParameter("comment") != null) domain.setComment(getParameter("comment"));
			if (getParameter("promote") != null)
			{
				deleteAlias(getParameter("promote"));
			}
			domain.save();
			dns.addGenerateConfigsTask();
			dns.addGenerateZoneFilesTask();
			if (create)
			{
				if (getParameter("copyOf") != null)
				{
					copyRecords(domain, getParameter("copyOf"));
				}
				redirect(MODULE + "/" + domain.getName());
			}
			else
			{
				redirect(MODULE);
			}
			return;
		}
		else if (getParameter("clear_export") != null)
		{
			domain.clearExported();
			redirect(MODULE + "/" + domain.getName());
			return;
		}
		setAttribute("copyOf", getParameter("copyOf"));
		setAttribute("promote", getParameter("promote"));
		setAttribute("domain", domain);
		setAttribute("servers", getServers());
		setAttribute("views", getViews());
		setAttribute("owners", getOwners());
		sendDispatch("/editDomain.jsp");
	}

	/**
	 * @param domain
	 * @param source
	 * @throws NotFoundException
	 * @throws SQLException
	 */
	private void copyRecords(Domain domain, String source) throws SQLException, NotFoundException
	{
		Domain sourceDomain = Domain.GetDomainByName(db, source);
		domain.copyRecords(sourceDomain);

	}

	private Map<Integer, Server> getServers() throws SQLException
	{
		Map<Integer, Server> servers = new LinkedHashMap<Integer, Server>();
		for (Server server : Server.GetServers(db))
		{
			servers.put(server.getId(), server);
		}
		return servers;
	}

	private Map<Integer, View> getViews() throws SQLException, NotFoundException
	{
		Map<Integer, View> views = new LinkedHashMap<Integer, View>();
		for (Server server : Server.GetServers(db))
		{
			for (View view : server.getViews())
			{
				views.put(view.getId(), view);
			}
		}
		return views;
	}

	private Map<Integer, Owner> getOwners() throws SQLException
	{
		Map<Integer, Owner> owners = new LinkedHashMap<Integer, Owner>();
		for (Owner owner : Owner.GetOwners(db))
		{
			owners.put(owner.getId(), owner);
		}
		return owners;
	}

	/**
	 * @param record
	 * @throws SQLException
	 * @throws IOException
	 * @throws ServletException
	 * @throws NotFoundException
	 * @throws InvalidDataException
	 * @throws PermissionDeniedException
	 */
	private void editRecord(Record record) throws SQLException, IOException, ServletException, NotFoundException, InvalidDataException, PermissionDeniedException
	{
		if (record != null)
		{
			if (getParameter("cancel") != null)
			{
				redirect(MODULE + "/" + record.getDomain().getName());
				return;
			}
			if (request.getMethod().equalsIgnoreCase("POST"))
			{
				boolean create = record.getId() == 0;
				PTRRecord reverse = null;
				if (record instanceof ARecord && "1".equals(getParameter("update_reverse")))
				{
					ARecord arecord = (ARecord) record;
					reverse = arecord.getReverse(false);
				}
				if (record instanceof SRVRecord)
				{
					SRVRecord srv = (SRVRecord) record;
					if (getParameter("service") != null) srv.setService(getParameter("service"));
					if (getParameter("protocol") != null) srv.setProtocol(getParameter("protocol"));
					if (getParameter("priority") != null) srv.setPriority(getIntegerParameter("priority"));
					if (getParameter("weight") != null) srv.setWeight(getIntegerParameter("weight"));
					if (getParameter("target") != null) srv.setTarget(getParameter("target"));
					if (getParameter("port") != null) srv.setPort(getIntegerParameter("port"));

				}
				if (getParameter("name") != null) record.setName(getParameter("name"));
				record.setActive(getBooleanParameter("active"));
				if (getIntegerParameter("group_id") != -1)
				{
					int groupId = getIntegerParameter("group_id");
					if (groupId != 0) record.setGroup(Group.GetGroupById(db, getIntegerParameter("group_id")));
					else record.setGroup(null);
				}
				if (getParameter("ttl") != null) record.setTtl(getParameter("ttl"));
				if (record instanceof LOCRecord)
				{
					LOCRecord loc = (LOCRecord) record;
					if (getParameter("lat_d") != null && getParameter("lat_m") != null && getParameter("lat_s") != null && getParameter("lat_cardinal") != null)
					{
						loc.setLatitude(getShortParameter("lat_d"), getShortParameter("lat_m"), getFloatParameter("lat_s"), getCharParameter("lat_cardinal"));
					}

					if (getParameter("lon_d") != null && getParameter("lon_m") != null && getParameter("lon_s") != null && getParameter("lon_cardinal") != null)
					{
						loc.setLongitude(getShortParameter("lon_d"), getShortParameter("lon_m"), getFloatParameter("lon_s"), getCharParameter("lon_cardinal"));
					}

					if (getParameter("alt") != null) loc.setAltitude(getFloatParameter("alt"));
					if (getFloatParameter("hp") != -1 && getFloatParameter("vp") != -1) loc.setPrecision(getFloatParameter("hp"), getFloatParameter("vp"));
				}
				else
				{
					if (getParameter("data") != null)
					{
						String data = getParameter("data");
						if (reverse != null)
						{
							if (!data.equals(record.getData()))
							{
								reverse.delete();
								reverse = null;
							}
						}
						record.setData(data);
					}
				}
				if (record instanceof MXRecord) if (getParameter("priority") != null) ((MXRecord) record).setPriority(getIntegerParameter("priority"));
				if (getParameter("comment") != null) record.setComment(getParameter("comment"));
				record.save();
				log.info(audit((create ? "created" : "updated") + " record " + record.getFQDN() + " id=" + record.getId() + " active=" + record.isActive() + " record=" + record.toString(1)));
				if (record instanceof ARecord && "1".equals(getParameter("update_reverse")))
				{
					if (reverse == null)
					{
						try
						{
							if (currentUser.isAuthorizedFor(((ARecord) record).getReverseDomain()))
							{

								reverse = ((ARecord) record).getReverse(true);
								if (reverse != null)
								{
									flashMessage("Created <a href=\"" + MODULE + "/" + reverse.getDomain().getName() + "/record/" + reverse.getId() + "\">PTR record</a> for A record " + record.getFQDN());
									log.info(audit("created reverse " + reverse.getFQDN() + " id=" + reverse.getId() + " for A record " + record.getFQDN() + " record=" + reverse.toString(1)));
								}
							}
							else
							{
								flashError("You are not permitted to update reverse");
							}
						}
						catch (NotFoundException e)
						{
						}
					}
					else
					{
						if (currentUser.isAuthorizedFor(reverse.getDomain()))
						{
							reverse.setData(record.getFQDN() + ".");
							reverse.save();
							flashMessage("Updated <a href=\"" + MODULE + "/" + reverse.getDomain().getName() + "/record/" + reverse.getId() + "\">PTR record</a> for A record " + record.getFQDN());
							log.info(audit("updated reverse " + reverse.getFQDN() + " id=" + reverse.getId() + " for A record " + record.getFQDN() + " record=" + reverse.toString(1)));

						}
						else
						{
							flashError("You are not permitted to update reverse");
						}
					}
				}
				redirect(MODULE + "/" + record.getDomain().getName());
				return;
			}
			setAttribute("record", record);
		}
		else
		{
			removeAttribute("record");
		}
		setAttribute("groups", getGroups());
		setAttribute("protocols", SRVRecord.GetProtocols());
		setAttribute("types", Record.getTypeNames());
		sendDispatch("/editRecord.jsp");
	}

	private void editMaster(Domain domain, Master master) throws SQLException, NotFoundException, ServletException, IOException
	{
		if (request.getMethod().equals("POST"))
		{
			if (master == null)
			{
				int master_id = getIntegerParameter("master_id");
				if (master_id == 0) throw new NotFoundException("You must select a master view");
				else
				{

					master = Master.GetMasterById(db, master_id);
					domain.removeMaster(master);
					domain.addMaster(master);
					domain.save();
					redirect(MODULE + "/" + domain.getName() + "/master/" + master_id);
					return;
				}
			}
			else
			{
				master.clearSlaves();
				int[] slaveViews = getIntegerArrayParameter("slaves");
				if (slaveViews != null && slaveViews.length > 0)
				{
					for (Integer viewId : slaveViews)
					{
						master.addSlave(View.GetViewById(db, viewId));
					}
				}
				domain.save();
				dns.addGenerateConfigsTask();
				redirect(MODULE + "/" + domain.getName());
				return;
			}
		}
		Map<Integer, View> views = getViews();
		HashSet<String> slaves = new HashSet<String>();
		if (master != null) views.remove(master.getId());
		for (Master view : domain.getMasters())
		{
			if (master != null && view.compareTo(master) == 0 && view.hasSlaves())
			{
				for (View slave : view.getSlaves())
				{
					slaves.add(Integer.toString(slave.getId()));
				}
			}
			else if (view.hasSlaves())
			{
				for (View slave : view.getSlaves())
				{
					views.remove(slave.getId());
				}
			}
			views.remove(view.getId());

		}
		setAttribute("slaves", slaves);
		setAttribute("views", views);
		setAttribute("master", master);

		sendDispatch("/editMaster.jsp");
	}

	private void deleteMaster(Domain domain, Master master) throws IOException, SQLException
	{
		domain.removeMaster(master);
		domain.save();
		dns.addGenerateConfigsTask();
		redirect(MODULE + "/" + domain.getName());
		return;
	}

	private void editAlias(Domain domain, String alias) throws ServletException, IOException, SQLException
	{
		if (request.getMethod().equals("POST"))
		{
			if (alias != null) domain.removeAlias(alias);
			if (getParameter("alias_name") != null && !getParameter("alias_name").isEmpty())
			{
				String aliasName = getParameter("alias_name").trim().toLowerCase();
				for (String a : aliasName.split("\\s+"))
				{
					domain.addAlias(a);
				}
			}
			domain.save();
			dns.addGenerateConfigsTask();
			redirect(MODULE + "/" + domain.getName());
			return;
		}
		setAttribute("domain", domain);
		setAttribute("alias", alias);
		sendDispatch("/editAlias.jsp");
	}

	private void deleteAlias(Domain domain, String alias) throws ServletException, IOException, SQLException
	{
		if (alias != null) domain.removeAlias(alias);
		domain.save();
		dns.addGenerateConfigsTask();
		redirect(MODULE + "/" + domain.getName());
		return;
	}

	private void deleteAlias(String alias) throws SQLException
	{
		Connection conn = null;
		PreparedStatement stmt = null;
		ResultSet rs = null;
		int domainId;
		try
		{
			conn = db.getConnection();

			stmt = conn.prepareStatement("SELECT `domain_id` FROM `domain_alias` WHERE `domain_name` = ?");
			stmt.setString(1, alias);
			rs = stmt.executeQuery();
			if (!rs.next()) return;
			domainId = rs.getInt("domain_id");
			db.closeResultSet(rs);
			rs = null;
			db.closeStatement(stmt);
			stmt = null;
			stmt = conn.prepareStatement("DELETE FROM `domain_alias` WHERE `domain_name` = ?");
			stmt.setString(1, alias);
			stmt.executeUpdate();
			db.closeStatement(stmt);
			stmt = null;
			stmt = conn.prepareStatement("UPDATE `domain` SET `updated`=NOW() WHERE `domain_id`=?");
			stmt.setInt(1, domainId);
			stmt.executeUpdate();
			dns.addGenerateConfigsTask();
		}
		finally
		{
			db.closeAll(conn, stmt, rs);
		}
	}

	private Map<Integer, String> getGroups() throws SQLException
	{
		Map<Integer, String> groups = new LinkedHashMap<Integer, String>();
		groups.put(0, "common");
		for (Group group : Group.GetGroups(db))
		{
			groups.put(group.getId(), group.getName());
		}
		return groups;
	}

	public class RecordPair
	{
		private String parsedRecord;
		private String originalRecord;
		public Record record;

		public RecordPair(String parsedRecord, String originalRecord)
		{
			this.originalRecord = originalRecord;
			this.parsedRecord = parsedRecord;
		}

		public RecordPair(Record record, String originalRecord)
		{
			this.originalRecord = originalRecord;
			this.parsedRecord = record != null ? record.toString() : "";
			this.record = record;
		}

		public String getParsedRecord()
		{
			return parsedRecord;
		}

		public void setParsedRecord(String parsedRecord)
		{
			this.parsedRecord = parsedRecord;
		}

		public String getOriginalRecord()
		{
			return originalRecord;
		}

		public void setOriginalRecord(String originalRecord)
		{
			this.originalRecord = originalRecord;
		}

		@Override
		public String toString()
		{
			return "RecordPair [parsedRecord=" + parsedRecord + ", originalRecord=" + originalRecord + "]";
		}

	}

	private void pasteRecords(Domain domain, int group_id) throws IOException, InvalidDataException, ServletException, SQLException, NotFoundException
	{
		if (request.getMethod().equals("POST"))
		{
			Group group = null;
			if (group_id != 0) group = Group.GetGroupById(db, group_id);
			List<RecordPair> parsedRecords = new ArrayList<RecordPair>();

			if (getParameter("records") != null)
			{
				BufferedReader recordReader = new BufferedReader(new StringReader(getParameter("records")));
				String recordLine;
				while ((recordLine = recordReader.readLine()) != null)
				{
					recordLine = recordLine.trim();
					if (recordLine.length() == 0) continue;
					else if (recordLine.startsWith("#")) continue;
					else if (recordLine.startsWith(";")) continue;
					try
					{
						log.error(recordLine);
						Record record = Record.ParseRecord(db, domain, recordLine);
						if (record != null) record.setGroup(group);
						parsedRecords.add(new RecordPair(record, recordLine));
					}
					catch (InvalidDataException e)
					{
						parsedRecords.add(new RecordPair("; " + e.getMessage(), recordLine));
					}
				}
			}
			else if (request.getParameterValues("record[]") != null)
			{
				for (String recordLine : request.getParameterValues("record[]"))
				{
					recordLine = recordLine.trim();
					if (recordLine.length() == 0) continue;
					else if (recordLine.startsWith("#")) continue;
					else if (recordLine.startsWith(";")) continue;
					try
					{
						Record record = Record.ParseRecord(db, domain, recordLine);
						if (record != null) record.setGroup(group);
						parsedRecords.add(new RecordPair(record, recordLine));
					}
					catch (InvalidDataException e)
					{
						parsedRecords.add(new RecordPair("; " + e.getMessage(), recordLine));
					}
				}
			}
			if (getParameter("save") != null && parsedRecords.size() > 0)
			{
				for (RecordPair pair : parsedRecords)
				{
					log.info(audit("created record " + pair.record.getFQDN() + " id=" + pair.record.getId() + " active=" + pair.record.isActive() + " record=" + pair.record.toString(1)));

					if (pair.record != null) pair.record.save();
				}
				redirect(MODULE + "/" + domain.getName());
				return;
			}
			setAttribute("parsedRecords", parsedRecords);
		}
		setAttribute("domain", domain);
		sendDispatch("/pasteRecords.jsp");
	}

	private List<DomainSearchResult> searchDomain(String domainName) throws SQLException
	{
		List<DomainSearchResult> domains = new LinkedList<DomainSearchResult>();
		Connection conn = null;
		PreparedStatement stmt = null;
		ResultSet rs = null;
		try
		{
			conn = db.getConnection();
			stmt = conn.prepareStatement("SELECT `domain_name` FROM `domain` WHERE `domain_name` LIKE ?");
			stmt.setString(1, domainName);
			rs = stmt.executeQuery();
			while (rs.next())
			{
				if (currentUser.isAuthorizedFor(rs.getString("domain_name")))
				{
					domains.add(new DomainSearchResult(rs.getString("domain_name"), null));
				}
			}
			db.closeResultSet(rs);
			rs = null;
			db.closeStatement(stmt);
			stmt = null;
			stmt = conn.prepareStatement("SELECT domain_alias.domain_name, domain.domain_name AS parent_domain from domain_alias JOIN domain ON domain_alias.domain_id=domain.domain_id WHERE domain_alias.domain_name LIKE ?");
			stmt.setString(1, domainName);
			rs = stmt.executeQuery();
			while (rs.next())
			{
				if (currentUser.isAuthorizedFor(rs.getString("parent_domain")))
				{
					domains.add(new DomainSearchResult(rs.getString("domain_name"), rs.getString("parent_domain")));
				}
			}
			Collections.sort(domains);
			return domains;
		}
		finally
		{
			db.closeAll(conn, stmt, rs);
		}
	}

	public class DomainSearchResult implements Comparable<DomainSearchResult>
	{
		public String domainName;
		public String parentDomain;

		public DomainSearchResult(String domainName, String parentDomain)
		{
			this.domainName = domainName;
			this.parentDomain = parentDomain;
		}

		public String getDomainName()
		{
			return domainName;
		}

		public String getParentDomain()
		{
			return parentDomain;
		}

		@Override
		public int compareTo(DomainSearchResult arg0)
		{
			return domainName.compareTo(arg0.domainName);
		}

	}
}
