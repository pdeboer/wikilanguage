# ************************************************************
# Sequel Pro SQL dump
# Version 4096
#
# http://www.sequelpro.com/
# http://code.google.com/p/sequel-pro/
#
# Host: 127.0.0.1 (MySQL 5.1.72-0ubuntu0.10.04.1)
# Database: wikilanguage2
# Generation Time: 2013-12-08 03:17:29 +0000
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
  KEY `person_from` (`person_from`)
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



# Dump of table years
# ------------------------------------------------------------

DROP TABLE IF EXISTS `years`;

CREATE TABLE `years` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `people_alive` int(11) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=MyISAM DEFAULT CHARSET=latin1;




/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;
/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
