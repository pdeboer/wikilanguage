# ************************************************************
# Sequel Pro SQL dump
# Version 4096
#
# http://www.sequelpro.com/
# http://code.google.com/p/sequel-pro/
#
# Host: 127.0.0.1 (MySQL 5.1.72-0ubuntu0.10.04.1)
# Database: wikilanguage
# Generation Time: 2014-02-10 16:19:26 +0000
# ************************************************************


/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!40101 SET NAMES utf8 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;


# Dump of table categories
# ------------------------------------------------------------

DROP TABLE IF EXISTS `categories`;

CREATE TABLE `categories` (
  `id` int(11) unsigned NOT NULL AUTO_INCREMENT,
  `name` varchar(255) DEFAULT NULL,
  `wiki_language` char(2) CHARACTER SET latin1 DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `name` (`name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;



# Dump of table connections
# ------------------------------------------------------------

DROP TABLE IF EXISTS `connections`;

CREATE TABLE `connections` (
  `id` int(11) unsigned NOT NULL AUTO_INCREMENT,
  `person_from` int(11) NOT NULL,
  `person_to` int(11) NOT NULL,
  `article_name` int(11) NOT NULL,
  `wiki_language` char(2) NOT NULL DEFAULT '',
  `year_from` int(11) DEFAULT NULL,
  `year_to` int(11) DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `person_from_2` (`person_from`,`person_to`),
  KEY `person_from` (`person_from`),
  KEY `pplto` (`person_to`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;



# Dump of table people
# ------------------------------------------------------------

DROP TABLE IF EXISTS `people`;

CREATE TABLE `people` (
  `id` int(11) unsigned NOT NULL AUTO_INCREMENT,
  `name` varchar(255) NOT NULL DEFAULT '',
  `wiki_language` char(2) NOT NULL DEFAULT '',
  `person_group_id` int(11) DEFAULT NULL,
  `year_from` int(11) DEFAULT NULL,
  `year_to` int(11) DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `name` (`name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;



# Dump of table people_aux
# ------------------------------------------------------------

DROP TABLE IF EXISTS `people_aux`;

CREATE TABLE `people_aux` (
  `id` int(11) unsigned NOT NULL,
  `indegree` int(11) DEFAULT NULL,
  `outdegree` int(11) DEFAULT NULL,
  `num_chars` int(11) DEFAULT NULL,
  `indegree_alive` int(11) DEFAULT NULL,
  `outdegree_alive` int(11) DEFAULT NULL,
  `pagerank` double DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=MyISAM DEFAULT CHARSET=latin1;



# Dump of table people_redirections
# ------------------------------------------------------------

DROP TABLE IF EXISTS `people_redirections`;

CREATE TABLE `people_redirections` (
  `id` int(11) unsigned NOT NULL AUTO_INCREMENT,
  `title` varchar(255) NOT NULL DEFAULT '',
  `target_person` int(11) NOT NULL,
  PRIMARY KEY (`id`),
  KEY `title` (`title`),
  KEY `target_person` (`target_person`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;



# Dump of table people2categories
# ------------------------------------------------------------

DROP TABLE IF EXISTS `people2categories`;

CREATE TABLE `people2categories` (
  `person` int(11) NOT NULL,
  `category` int(11) NOT NULL,
  PRIMARY KEY (`person`,`category`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;



# Dump of table peoplecontent
# ------------------------------------------------------------

DROP TABLE IF EXISTS `peoplecontent`;

CREATE TABLE `peoplecontent` (
  `id` int(11) unsigned NOT NULL,
  `content` longtext,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 ROW_FORMAT=COMPRESSED KEY_BLOCK_SIZE=4;



# Dump of table year_people_experiments
# ------------------------------------------------------------

DROP TABLE IF EXISTS `year_people_experiments`;

CREATE TABLE `year_people_experiments` (
  `id` int(11) unsigned NOT NULL AUTO_INCREMENT,
  `person_id` int(11) NOT NULL,
  `year_id` int(11) NOT NULL,
  `experiment_name` varchar(100) NOT NULL DEFAULT '',
  `dataInt` int(11) DEFAULT NULL,
  `dataString` varchar(1000) DEFAULT NULL,
  `dataDouble` double DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `experiment_name` (`experiment_name`,`person_id`,`year_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;



# Dump of table years
# ------------------------------------------------------------

DROP TABLE IF EXISTS `years`;

CREATE TABLE `years` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `people_alive` int(11) DEFAULT NULL,
  `people_indeg1_alive` int(11) DEFAULT NULL,
  `people_born` int(11) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=MyISAM DEFAULT CHARSET=latin1;




/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;
/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
