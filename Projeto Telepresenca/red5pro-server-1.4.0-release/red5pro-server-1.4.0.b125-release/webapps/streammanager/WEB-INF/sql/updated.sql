CREATE TABLE `node` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `host` varchar(150) NOT NULL,
  `state` int(11) DEFAULT '0',
  `type` int(11) DEFAULT '0',
  `name` varchar(150) DEFAULT NULL,
  `node_platform_id` varchar(150) DEFAULT NULL,
  `region` varchar(64) DEFAULT NULL,
  `launchTime` TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  `group_id` bigint(20) NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `host_UNIQUE` (`host`),
  UNIQUE KEY `name_UNIQUE` (`name`),
  KEY `group_id_INDEX` (`group_id`)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8;


CREATE TABLE `node_info` (
  `info_id` bigint(20) NOT NULL AUTO_INCREMENT,
  `node_id` bigint(20) DEFAULT NULL,
  `client_count` int(11) DEFAULT '0',
  `stream_count` int(11) DEFAULT '0',
  `origins` text,
  `edges` text,
  `connection_capacity` int(11) DEFAULT NULL,
  `last_ping` bigint DEFAULT '0',
  PRIMARY KEY (`info_id`),
  UNIQUE KEY `node_id_UNIQUE` (`node_id`)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8;


CREATE TABLE `stream_info` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `node_id` bigint(20) DEFAULT NULL,
  `name` varchar(64) DEFAULT NULL,
  `description` text,
  `scope` varchar(64) DEFAULT NULL,
  `current_subscribers` int(11) DEFAULT '0',
  `start_time` bigint,
  PRIMARY KEY (`id`),
  KEY `node_id_INDEX` (`node_id`)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8;


CREATE TABLE `autoscalelog` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `group_id` bigint(20) NOT NULL,
  `alarm_trigger_type` int(11) DEFAULT '1',
  `scale_policy` varchar(64) DEFAULT NULL,
  `timestamp` TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `group_id_index` (`group_id`)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8;


CREATE TABLE `node_group` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `name` varchar(64) DEFAULT NULL,
  `originConnections` int(8) NOT NULL,
  `regions` text,
  `launch_config` varchar(64) DEFAULT NULL,
  `scale_policy` varchar(64) DEFAULT NULL,
  `type` int(11) DEFAULT '0',
  `state` int(11) DEFAULT '0',
  `timestamp` TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `name_UNIQUE` (`name`)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8;
