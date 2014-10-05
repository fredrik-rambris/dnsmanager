DNSManager by Fredrik Rambris <fredrik@rambris.com>
===================================================

License
=======
This software is copyright (c) 2011 Fredrik Rambris. All rights reserved.

A web interface to all your DNS zones. The reason for its creation was that I
became fed upp with editing zone files and configs manually. I created it for
my needs and I couldn't find an existing tool that fit those exact needs.

It is written in Java and is hosted within a Tomcat container, connected to a 
MySQL database.

Without giving away too much information about my networks let me give you a
brief overview of what scenario DNSManager was created for.

We have two networks. Office (where we sit with office servers and
test/development servers) and Colocation (where the servers for our web sites
are). Between these we have VPN connection.  I want the workstations and
servers in our office to get the internal Ps of our office servers as well as
the colocation servers. I also want the rest of the Internet to only get the
exposed public machines external IPs. Adding to that I do not want the
colocation servers to be able to lookup our office machines as the web site
servers are more exposed to threats than our office and greater risk of getting
hacked. I setup my DNS so that they only serve on a need to know basis. We have
a number of DNS servers at our colocation acting primary DNS for most of our
domains and recursive resolver for the servers there. At our office we have a
number of DNS servers acting as secondary DNS for most of our domains and
primary to some office-only domains. On most public domains I have set the 
colocation external view to be master of office external view and colocation 
internal view to be master of office internal view. This way www1.domain.tld 
may be 192.168.10.10 from inside colocation and inside office but 92.68.10.10 
for the rest of Internet. Sometimes we want to access colocation servers with 
their external IP from our office (say www.domain.tld) to be sure we see what 
the rest of you see. What I have done then is to have our office internal view 
serve a special version of the domain which is a combination of internal and 
external IPs. Often the only public IP address is NATed to a load balancer. The 
individual web servers never have their own public IP address.

I have now created this system which uploads zones and configs to our DNS 
servers with SCP and instruct named to reload with SSH. Uploads and reloads are 
triggered when domains are bumped, added or removed. When domains are created 
or updated the configs are updated and uploaded. When the domains are bumped 
(serial incremented) the zone files are uploaded too. To keep the tomcat user 
from being able to access the DNS servers I have setup a system where a cron 
job periodicly runs triggered uploads. The servlet only edits the database, the 
application generates zones and configs and uploads this to DNS servers.


Installation
============
Create database from schema.sql. Create a user and give it SELECT, INSERT, 
UPDATE and DELETE privileges. Extract the DNSManager archive into your tomcat 
webbaps dir. Move the dnsmanager.xml to a good location (/etc comes to mind).  
Move the dnsmanager.logging.properties to a good location too. Set the params 
in dnsmanager.xml. Edit DNSManager/WEB-INF/web.xml and set the path of 
dnsmanager.xml therein. Copy dnsmanager.sh to /usr/local/sbin, edit it and set 
the paths. Start tomcat. You should be home free. Access it through 
http://ipaddress/DNSManager/. Setup the crontab to run 
/usr/local/sbin/dnsmanager.sh every minute or so as root. See to it that root 
can SSH to your DNS servers root account without passphrase. If need be only 
allow it to run 'rndc reload' and scp.


User interface
==============

Server
------
A DNS server. It has a host name and a set of views. The hostname is used in 
the SOA of the domains.  It also has config parameters for what address to use 
when SSH/SCP to the server, what paths configs and zones are, and what path 
relative to server root primary zone files and slave zone files are. In my 
named.conf I have directory "/var/named/data" so Zone Path is then set to 
"/var/named/chroot/var/named/data". Within that dir I have pz and sz so Master 
and Slave prefix is set to "pz/" and "sz/". In my named.conf I have in each 
view an  include "/etc/named.internal.conf" which is why I have 
"/var/named/chroot/etc" as Config Path. Reload command I have /usr/sbin/rndc 
reload. If Scp address is left empty the commands are run locally. If you run 
DNSManager on a server which is also a DNSManager managed DNS it may be nice to 
be able to run the commands locally instead of SSHing to localhost (although 
you can do that too, if you like).


View
----
A view of a DNS server. When asked from one side you get answers from one set
of zone files, and when asked from the other side you get answers from another
set of zone files. In my case 'external' and 'internal'. A view is connected to
one or more groups. A view also has an IP address instead of the whole server.
In my setup I have an internal and external IP address of our DNS servers,
NATed in the firewall. The servers is setup in a "split horizon" where when you
ask it from Internet you only get public names and addresses whereas when you
ask the same query from the inside you get internal addresses. The view can
have different priorities on the groups. If a record with the same name and
type but in a different group the priority determines which one is put into the
view zone file.


Group
-----
Records can be tied to a group which in turn connects them to one or
more views. In my case I use networks as groups. 'office_internal',
'office_external', 'colocation_internal'. A record can be also be without
group; said to be in infered group 'common'; visible in all views.


Owner
-----
Just the admin email to put into SOA of the domains.


Domain
------
Pretty obvious. Domain can be visible in a set of views, called
Masters which in turn have slaves. This generates configs with the zone as
master in each of the masterviews. The slaves to each master have an entry in
its generated config defining its master. In my case I often let
officedns:internal be a slave of colocationdns:internal and officedns:external
be a slave of colocation:external. The colocationdns being the primary DNS and
officedns secondary.

The serial can be bumped to the next in sequence common format (YYMMDDnn) serial.

A domain can have a set of aliases; zones which will load the same data as the
main domain. I have a domain called PARKED_DOMAIN which only have NS and MX
records. In it I have 20-30 or so aliases. Should I move the MX I only have to
set it once.

A convenient function is the copy domain. I often have the same masters/slaves,
owner, NS and MX records. Because of that I have created a .template domain
which is inactive. When a new domain is to be hosted I simply copy that, set
active and save.

Record
------
A domain name record. Pretty strait forward. You can set priority on some
types. A records and PTR records have a set of checks to find and update
eachother. When you update or create a record you. A record is connected to a
group.


Report
------
Generates a domain report, checking the SOA host from an external DNS. I use
this to see if a domain has been transfered to another DNS server or does not
exist anymore. Not everyone bothers to tell the old hostmaster that they are
moving their domain.
