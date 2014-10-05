/* DomainView.java (c) 2011 Fredrik Rambris. All rights reserved */
package com.rambris.dnsmanager;

import java.sql.SQLException;
import java.util.HashSet;
import java.util.Set;

import com.rambris.Database;

/**
 * @author Fredrik Rambris <fredrik@rambris.com>
 * @version $Id$
 */
public class Master extends View
{
	private Set<View> slaves = new HashSet<View>();

	public Master(Database db)
	{
		super(db);
	}

	public Master(Database db, Server server)
	{
		super(db, server);
	}

	private Master(View view)
	{
		super(view.db);
		id = view.id;
		serverId = view.serverId;
		server = view.server;
		name = view.name;
		description = view.description;
		address = view.address;
		notify = view.notify;
		groups = view.groups;
	}

	public void addSlave(View slave)
	{
		slaves.add(slave);
	}

	public void clearSlaves()
	{
		slaves.clear();
	}

	public Set<View> getSlaves()
	{
		return slaves;
	}

	public boolean hasSlaves()
	{
		return slaves.size() > 0;
	}

	public static Master GetMasterById(Database db, int id) throws NotFoundException, SQLException
	{
		Master view = new Master(db);
		if (view.load(id)) return view;
		else throw new NotFoundException("No view by id " + id);
	}
}
