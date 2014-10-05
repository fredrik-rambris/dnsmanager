/* DNS.java (c) 2010-2011 Fredrik Rambris. All rights reserved */
package com.rambris.dnsmanager;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.DateFormat;
import java.util.Date;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.regex.Pattern;

import org.apache.commons.configuration.ConfigurationException;
import org.apache.log4j.Logger;

import com.rambris.Config;
import com.rambris.Database;
import com.rambris.DatabasePool;

/**
 * @author Fredrik Rambris <fredrik@rambris.com>
 * @version $Id: DNS.java 50 2010-09-04 22:38:59Z boost $
 */
public class DNS
{
	private final Database db;
	private final Config config;
	public static final String version = "0.3";
	private final Logger log = Logger.getLogger(DNS.class);
	private final DNSLookup dnslookup;

	public DNS(File configFile) throws ConfigurationException, SQLException
	{
		config = new Config(configFile);
		db = new DatabasePool(config.toProperties("database"));
		dnslookup = new DNSLookup(config);
	}

	public Database getDatabase()
	{
		return db;
	}

	public Config getConfig()
	{
		return config;
	}

	public DNSLookup getLookup()
	{
		return dnslookup;
	}

	private static Pattern hostnamePattern = Pattern.compile("[a-zA-Z0-9_\\*-]{1,63}");

	public static boolean validHostname(String hostname)
	{
		if (hostname == null) return false;
		if (hostname.length() > 255) return false;
		for (String label : hostname.split("\\."))
		{
			if (!hostnamePattern.matcher(label).matches()) return false;
		}
		return true;
	}

	private void generateZoneFiles() throws SQLException, NotFoundException, IOException, InvalidDataException
	{
		generateZoneFiles(null);
	}

	private void generateZoneFiles(String onlyDomainName) throws SQLException, NotFoundException, IOException, InvalidDataException
	{
		File baseDir = new File(config.getString("zonefiles"));
		Set<Domain> exportedDomains = new HashSet<Domain>();
		for (Server server : Server.GetServers(db))
		{
			log.trace("Server:" + server.getHostname());
			List<View> serverViews = server.getViews();
			log.trace(serverViews);
			File serverDir = new File(baseDir, server.getHostname());
			if (!serverDir.exists()) serverDir.mkdirs();
			File zoneDir = new File(serverDir, server.getMasterPrefix());
			if (!zoneDir.exists()) zoneDir.mkdirs();
			mkdirs(server, server.getMasterZonePath());

			for (Domain domain : server.getDomains())
			{
				if (onlyDomainName != null && !domain.getName().equals(onlyDomainName)) continue;
				if (!domain.shouldExport()) continue;
				log.trace(" Domain:" + domain.getName());
				Map<String, List<Record>> records = domain.getRecords();
				for (Master view : domain.getMasters())
				{
					boolean found = false;
					for (View serverView : serverViews)
					{
						if (serverView.equals(view))
						{
							found = true;
							break;
						}
					}
					if (!found) continue;
					log.trace("  View:" + view.getName());

					PrintWriter zoneFileWriter = null;
					try
					{
						File zoneFile = new File(zoneDir, (domain.isReverse() ? domain.getReverseName() : domain.getName()) + "." + view.getName());
						log.info("Updating " + zoneFile.getPath());
						zoneFileWriter = new PrintWriter(new FileWriter(zoneFile));
						writeComment(zoneFileWriter, ";");
						zoneFileWriter.write(domain.getSOA());

						List<Group> groups = new LinkedList<Group>();
						{ // Scope!
							Group group = new Group(db);
							group.setName("common");
							groups.add(group);
						}
						groups.addAll(view.getGroups());

						/* Calculate max size of the names */
						int size = 0;
						for (Group group : groups)
						{
							if (!records.containsKey(group.getName())) continue;
							for (Record record : records.get(group.getName()))
							{
								if (record.getBuiltName().length() > size) size = record.getBuiltName().length();
							}
						}

						LinkedHashMap<String, Short> previousRecords = new LinkedHashMap<String, Short>();

						for (Group group : groups)
						{
							if (!records.containsKey(group.getName())) continue;
							if (records.get(group.getName()).size() == 0) continue;
							boolean header = false;

							for (Record record : records.get(group.getName()))
							{
								if (!record.isActive()) continue;
								if (previousRecords.containsKey(record.getBuiltName() + record.getType()))
								{
									/*
									 * Skip records with same name+type of
									 * higher priority (lower number)
									 */
									if (previousRecords.get(record.getBuiltName() + record.getType()) < group.getViewPriority()) continue;
								}
								else
								{
									previousRecords.put(record.getBuiltName() + record.getType(), group.getViewPriority());
								}

								if (!header)
								{
									zoneFileWriter.println();
									zoneFileWriter.println("; " + group.getName().replace('_', ' ') + " group");
									zoneFileWriter.println();
									header = true;
								}
								zoneFileWriter.println(record.toString(size));
							}
						}
						zoneFileWriter.println();
						zoneFileWriter.close();
						zoneFileWriter = null;
						scpZone(zoneFile, server);
					}
					finally
					{
						if (zoneFileWriter != null)
						{
							zoneFileWriter.close();
							zoneFileWriter = null;
						}
					}
				}
				exportedDomains.add(domain);
			}
		}
		for (Domain domain : exportedDomains)
		{
			log.debug("Flagging " + domain.getDisplayName() + " as exported");
			domain.setExported();
		}
	}

	private void reloadServers() throws IOException, SQLException
	{
		for (Server server : Server.GetServers(db))
		{
			reloadServer(server);
		}
	}

	private void reloadServer(Server server) throws IOException
	{
		if (!config.getBoolean("deploy", true)) return;
		if (server.getScpAddress() == null && server.getReloadCommand() != null)
		{
			Runtime runtime = Runtime.getRuntime();
			runtime.exec(server.getReloadCommand());
			log.debug(server.getReloadCommand());
		}
		else if (server.getScpAddress() != null && server.getReloadCommand() != null)
		{
			Runtime runtime = Runtime.getRuntime();
			List<String> command = new LinkedList<String>();
			command.add("ssh");
			command.add("-q");
			command.add(server.getScpAddress());
			command.add(server.getReloadCommand());
			log.debug(command.toString());
			runtime.exec(command.toArray(new String[0]));
		}
	}

	private void scp(File source, String destination) throws IOException
	{
		Runtime runtime = Runtime.getRuntime();
		List<String> command = new LinkedList<String>();
		command.add("scp");
		command.add("-B");
		command.add(source.getPath());
		command.add(destination);
		log.debug(command.toString());
		Process exec = runtime.exec(command.toArray(new String[0]));
		try
		{
			if (exec.waitFor() != 0)
			{
				byte[] buffer = new byte[4096];
				exec.getErrorStream().read(buffer);
				log.error(new String(buffer));
			}
		}
		catch (InterruptedException e)
		{
			log.warn(e.getMessage());
		}
	}

	private void scpConfig(File source, Server server) throws IOException
	{
		if (!config.getBoolean("deploy", true)) return;

		if (server.getScpConfigPath() != null && !server.getScpConfigPath().isEmpty())
		{
			scp(source, server.getScpConfigPath());
		}
	}

	private void scpZone(File source, Server server) throws IOException
	{
		if (!config.getBoolean("deploy", true)) return;

		if (server.getScpZonePath() != null && !server.getScpZonePath().isEmpty())
		{
			scp(source, server.getScpZonePath());
		}
	}

	private void mkdirs(Server server, String path) throws IOException
	{
		if (!config.getBoolean("deploy", true)) return;

		if (server.getScpAddress() == null)
		{
			File serverDir = new File(path);
			if (!serverDir.exists()) serverDir.mkdirs();
		}
		else
		{
			Runtime runtime = Runtime.getRuntime();
			List<String> command = new LinkedList<String>();
			command.add("ssh");
			command.add("-q");
			command.add(server.getScpAddress());
			command.add("mkdir -p \"" + (path.replaceAll("\"", "\\\"")) + "\"");
			log.debug(command.toString());
			runtime.exec(command.toArray(new String[0]));

		}
	}

	private void writeComment(PrintWriter out, String prefix)
	{
		DateFormat df = DateFormat.getDateTimeInstance();
		out.println(prefix + " This file is generated with DNSManager v" + version + " by Fredrik Rambris on " + df.format(new Date()));
		out.println(prefix + " Do not edit, will be overwritten.");
		out.println();
	}

	private void generateConfigs() throws SQLException, NotFoundException, IOException
	{
		String[] akamaized = { "gymgrossisten.com", "bodystore.dk", "bodystore.de", "gymgrossisten.no", "fitnesstukku.fi", "bodystore.com", "milebreaker.com" };
		File baseDir = new File(config.getString("configfiles"));
		for (Server server : Server.GetServers(db))
		{
			File serverDir = new File(baseDir, server.getHostname());
			if (!serverDir.exists()) serverDir.mkdirs();
			mkdirs(server, server.getConfigPath());
			for (View view : server.getViews())
			{
				PrintWriter configFileWriter = null;
				TreeMap<String, String> configs = new TreeMap<String, String>();
				try
				{
					File configFile = new File(serverDir, "named." + view.getName() + ".conf");
					log.info("Updating " + configFile.getPath());
					configFileWriter = new PrintWriter(new FileWriter(configFile));
					writeComment(configFileWriter, "//");
					for (Domain domain : view.getDomains())
					{
						if (!domain.isActive()) continue;
						StringWriter config = new StringWriter();
						PrintWriter configWriter = new PrintWriter(config);
						configWriter.println("zone \"%s\" {");

						if (view.isMasterOf(domain))
						{
							configWriter.println("\ttype master;");
							configWriter.printf("\tfile \"%s%s.%s\";%n", server.getMasterPrefix(), (domain.isReverse() ? domain.getReverseName() : domain.getName()), view.getName());
							Master master = domain.getMasterById(view.getId());
							if (master.getSlaves().size() > 0)
							{
								configWriter.print("\tallow-transfer {");
								for (View slave : master.getSlaves())
								{
									configWriter.printf(" %s;", slave.getAddress());
								}
								// Hack to enable transfer to Akamai. Hardcoded for now. 2013-11-20.
								for (String akadom : akamaized)
								{
									if (akadom.equalsIgnoreCase(domain.getName()))
									{
										log.info(domain.getName() + " is akamaized");
										configWriter.printf(" akamai;");
										break;
									}
								}
								configWriter.println(" };");
							}
						}
						else
						{
							configWriter.println("\ttype slave;");
							configWriter.printf("\tfile \"%s%%s.%s\";%n", server.getSlavePrefix(), view.getName());
							List<Master> masters = new LinkedList<Master>();
							for (Master master : domain.getMasters())
							{
								if (master.hasSlaves())
								{
									for (View slave : master.getSlaves())
									{
										if (slave.equals(view))
										{
											masters.add(master);
											break;
										}
									}
								}
							}
							if (masters.size() > 0)
							{
								configWriter.print("\tmasters { ");
								for (Master master : masters)
								{
									configWriter.print(master.getAddress() + "; ");
								}
								configWriter.println("};");

							}
							// Hack to enable transfer to Akamai. Hardcoded for now. 2013-11-20.
							for (String akadom : akamaized)
							{
								if (akadom.equalsIgnoreCase(domain.getName()))
								{
									log.info(domain.getName() + " is akamaized");
									configWriter.println("\tallow-transfer { akamai; };");
									break;
								}
							}

						}

						configWriter.printf("};");
						configWriter.close();
						if (validHostname(domain.getName()) && !domain.getName().contains("_"))
						{
							configs.put(domain.getName(), String.format(config.toString(), domain.getName(), (domain.isReverse() ? domain.getReverseName() : domain.getName())));
						}
						else
						{
							log.warn("Skipping invalid name: " + domain.getName());
						}
						for (String alias : domain.getAliases())
						{
							if (validHostname(alias) && !alias.contains("_"))
							{
								configs.put(alias, String.format(config.toString(), alias, alias));
							}
							else
							{
								log.warn("Skipping invalid name: " + alias);
							}
						}
					}

					for (Map.Entry<String, String> e : configs.entrySet())
					{
						if (e.getKey().startsWith(".")) continue;
						configFileWriter.println(e.getValue());
						configFileWriter.println();
					}
					configFileWriter.close();
					configFileWriter = null;
					scpConfig(configFile, server);
				}
				finally
				{
					if (configFileWriter != null)
					{
						configFileWriter.close();
						configFileWriter = null;
					}

				}
			}
		}
	}

	public void clearExported() throws SQLException
	{
		Connection conn = null;
		Statement stmt = null;
		try
		{
			conn = db.getConnection();
			stmt = conn.createStatement();
			stmt.executeUpdate("UPDATE `domain` SET `exported`=NULL");
		}
		finally
		{
			db.closeAll(conn, stmt, null);
		}
	}

	public String[] getTasks(boolean clear) throws SQLException
	{
		Connection conn = null;
		Statement stmt = null;
		ResultSet rs = null;
		LinkedHashSet<String> tasks = new LinkedHashSet<String>();
		try
		{
			conn = db.getConnection();
			conn.setAutoCommit(false);
			stmt = conn.createStatement();
			rs = stmt.executeQuery("SELECT `task_name` FROM `task` ORDER BY `updated`");
			while (rs.next())
			{
				tasks.add(rs.getString("task_name"));
			}
			db.closeResultSet(rs);
			rs = null;
			if (clear)
			{
				stmt.executeUpdate("DELETE FROM `task`");
			}
			conn.commit();
			return tasks.toArray(new String[0]);
		}
		finally
		{
			db.closeAll(conn, stmt, rs);
		}
	}

	private void executeTasks() throws SQLException, NotFoundException, IOException, InvalidDataException
	{
		boolean reloadServers = false;
		for (String taskName : getTasks(true))
		{
			if (taskName.equals("generateZoneFiles")) generateZoneFiles();
			else if (taskName.equals("generateConfigs")) generateConfigs();
			reloadServers = true;
		}
		if (reloadServers) reloadServers();
	}

	private void addTask(String taskName) throws SQLException
	{
		Connection conn = null;
		PreparedStatement stmt = null;
		try
		{
			conn = db.getConnection();
			stmt = conn.prepareStatement("REPLACE INTO `task` (`task_name`) VALUES (?)");
			stmt.setString(1, taskName);
			stmt.executeUpdate();
		}
		finally
		{
			db.closeAll(conn, stmt, null);
		}
	}

	public void addGenerateZoneFilesTask() throws SQLException
	{
		addTask("generateZoneFiles");
	}

	public void addGenerateConfigsTask() throws SQLException
	{
		addTask("generateConfigs");
	}

	public static void main(String[] args) throws Exception
	{
		File configFile = new File(System.getProperty("CONFIG_FILE"));
		DNS dns = new DNS(configFile);
		if (args.length > 0)
		{
			if ("-a".equals(args[0]))
			{
				dns.addupdateUser(args[1], args[2]);
			}
		}
		dns.executeTasks();
	}

	private void addupdateUser(String username, String password) throws SQLException
	{
		User user = null;
		try
		{
			user = User.GetUserByName(db, username);
		}
		catch (NotFoundException e)
		{
			user = new User(db);
			user.setName(username);
		}
		user.setPassword(password);
		user.setSuperAdmin(true);
		user.save();
		System.err.println("Created user");
		System.exit(0);
	}
}
