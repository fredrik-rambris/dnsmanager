/*!40014 SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;

DROP TABLE IF EXISTS `domain`;
CREATE TABLE `domain` (
  `domain_id` INT UNSIGNED NOT NULL AUTO_INCREMENT,
  `domain_name` varchar(255) NOT NULL,
  `server_id` INT UNSIGNED NOT NULL COMMENT 'Used for SOA and config if we''re not using views',
  `owner_id` INT UNSIGNED NOT NULL,
  `ttl` INT NOT NULL DEFAULT '21600',
  `serial` BIGINT UNSIGNED NOT NULL,
  `refresh` INT NOT NULL DEFAULT '18000',
  `retry` INT NOT NULL DEFAULT '3600',
  `expire` INT NOT NULL DEFAULT '604800',
  `minimum` INT NOT NULL DEFAULT '21600',
  `created` DATETIME NOT NULL,
  `updated` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `active` BOOL NOT NULL DEFAULT '1',
  `exported` DATETIME DEFAULT NULL,
  `comment` TEXT,
  PRIMARY KEY (`domain_id`),
  UNIQUE (`domain_name`),
  KEY (`server_id`),
  KEY (`owner_id`)
) ENGINE=InnoDB COMMENT='A domain of records';

DROP TABLE IF EXISTS `domain_alias`;
CREATE TABLE `domain_alias` (
  `domain_id` INT UNSIGNED DEFAULT NULL,
  `domain_name` varchar(255) NOT NULL,
  UNIQUE (`domain_name`),
  KEY (`domain_id`),
  FOREIGN KEY (`domain_id`) REFERENCES `domain` (`domain_id`) ON DELETE CASCADE
) ENGINE=InnoDB;

DROP TABLE IF EXISTS `domain_view`;
CREATE TABLE `domain_view` (
  `domain_id` INT UNSIGNED NOT NULL,
  `master_view_id` INT UNSIGNED NOT NULL,
  `slave_view_id` INT UNSIGNED NOT NULL DEFAULT '0',
  PRIMARY KEY (`domain_id`,`master_view_id`,`slave_view_id`),
  KEY (`master_view_id`),
  KEY (`slave_view_id`),
  KEY (`domain_id`,`master_view_id`),
  FOREIGN KEY (`domain_id`) REFERENCES `domain` (`domain_id`) ON DELETE CASCADE,
  FOREIGN KEY (`master_view_id`) REFERENCES `view` (`view_id`) ON DELETE CASCADE
) ENGINE=InnoDB  COMMENT='What views should the domain be visible in';

DROP TABLE IF EXISTS `group`;
CREATE TABLE `group` (
  `group_id` INT UNSIGNED NOT NULL AUTO_INCREMENT,
  `group_name` varchar(50) NOT NULL,
  PRIMARY KEY (`group_id`)
) ENGINE=InnoDB;

DROP TABLE IF EXISTS `group_view`;
CREATE TABLE `group_view` (
  `group_id` INT UNSIGNED NOT NULL,
  `view_id` INT UNSIGNED NOT NULL,
  `view_priority` tinyint(4) NOT NULL DEFAULT '0',
  PRIMARY KEY (`group_id`,`view_id`),
  KEY (`group_id`),
  KEY (`view_id`),
  KEY (`view_priority`),
  FOREIGN KEY (`group_id`) REFERENCES `group` (`group_id`) ON DELETE CASCADE,
  FOREIGN KEY (`view_id`) REFERENCES `view` (`view_id`) ON DELETE CASCADE
) ENGINE=InnoDB  COMMENT='A view is a collection of groups';

DROP TABLE IF EXISTS `owner`;
CREATE TABLE `owner` (
  `owner_id` INT UNSIGNED NOT NULL AUTO_INCREMENT,
  `owner_name` varchar(40) DEFAULT NULL,
  `owner_email` varchar(40) NOT NULL DEFAULT 'hostmaster@it.cdon.com',
  PRIMARY KEY (`owner_id`)
) ENGINE=InnoDB COMMENT='Owners of domains, used mainly for SOAs';

DROP TABLE IF EXISTS `record`;
CREATE TABLE `record` (
  `record_id` INT UNSIGNED NOT NULL AUTO_INCREMENT,
  `domain_id` INT UNSIGNED NOT NULL,
  `group_id` INT UNSIGNED DEFAULT NULL,
  `ttl` INT NOT NULL DEFAULT '0',
  `type` enum('NS','MX','TXT','A','CNAME','LOC','SRV','PTR') NOT NULL DEFAULT 'A',
  `name` varchar(255) NOT NULL,
  `data` varchar(1024) NOT NULL,
  `priority` INT NOT NULL DEFAULT '0',
  `active` BOOL NOT NULL DEFAULT '0',
  `comment` TEXT,
  `created` DATETIME NOT NULL,
  `updated` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`record_id`),
  KEY (`domain_id`),
  KEY (`group_id`),
  FOREIGN KEY (`domain_id`) REFERENCES `domain` (`domain_id`) ON DELETE CASCADE,
  FOREIGN KEY (`group_id`) REFERENCES `group` (`group_id`) ON DELETE CASCADE
) ENGINE=InnoDB;

DROP TABLE IF EXISTS `server`;
CREATE TABLE `server` (
  `server_id` INT UNSIGNED NOT NULL AUTO_INCREMENT,
  `hostname` varchar(255) NOT NULL,
  `master_prefix` varchar(45) DEFAULT NULL COMMENT 'Path or prefix for master zone files',
  `slave_prefix` varchar(45) DEFAULT NULL COMMENT 'Path or prefix for slave zone files',
  `scp_address` varchar(255) DEFAULT NULL COMMENT 'Destination host when SCPing config and zone files',
  `zone_path` TEXT NOT NULL COMMENT 'Where does master and slave_prefix live on the server',
  `config_path` TEXT NOT NULL COMMENT 'Where does config files live on server',
  `reload_command` TEXT,
  PRIMARY KEY (`server_id`),
  UNIQUE `hostname` (`hostname`)
) ENGINE=InnoDB COMMENT='A server, hostname which can hold domains';

DROP TABLE IF EXISTS `task`;
CREATE TABLE `task` (
  `task_name` varchar(50) NOT NULL,
  `updated` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`task_name`)
) ENGINE=InnoDB;

DROP TABLE IF EXISTS `view`;
CREATE TABLE `view` (
  `view_id` INT UNSIGNED NOT NULL AUTO_INCREMENT,
  `server_id` INT UNSIGNED DEFAULT NULL,
  `view_name` varchar(50) NOT NULL,
  `view_description` TEXT NOT NULL,
  `view_address` varchar(255) NOT NULL,
  `notify` BOOL NOT NULL DEFAULT '0' COMMENT 'Notify slaves from this view',
  PRIMARY KEY (`view_id`),
  KEY (`server_id`),
  FOREIGN KEY (`server_id`) REFERENCES `server` (`server_id`)
) ENGINE=InnoDB COMMENT='If a server has multiple views these are defined here.';

/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
