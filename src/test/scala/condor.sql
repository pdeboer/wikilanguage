-- Create syntax for TABLE 'condor_chars'
CREATE TABLE  IF NOT EXISTS  `condor_chars` (
  `datasetId` int(11) NOT NULL DEFAULT '0',
  `mailaddress` varchar(250) NOT NULL DEFAULT '',
  `organization` varchar(50) NOT NULL DEFAULT '',
  `domain` varchar(50) NOT NULL DEFAULT '',
  `name` varchar(250) NOT NULL DEFAULT '',
  UNIQUE KEY `datasetId_mailaddress` (`datasetId`,`mailaddress`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

-- Create syntax for TABLE 'condor_comm'
CREATE TABLE  IF NOT EXISTS `condor_comm` (
  `datasetId` int(11) NOT NULL DEFAULT '0',
  `comm_id` int(11) NOT NULL DEFAULT '0',
  `comm_from` varchar(250) NOT NULL DEFAULT '',
  `comm_subject` varchar(250) NOT NULL DEFAULT '',
  `comm_time` datetime NOT NULL DEFAULT '0000-00-00 00:00:00',
  `comm_content` longtext,
  `timestamp` datetime DEFAULT NULL,
  `messageId` varchar(250) NOT NULL DEFAULT '',
  PRIMARY KEY (`datasetId`,`comm_id`),
  UNIQUE KEY `datasetId_from_time` (`comm_from`,`comm_time`,`datasetId`),
  UNIQUE KEY `messageIdIdx` (`datasetId`,`messageId`),
  KEY `datasetId_comm_time` (`datasetId`,`comm_time`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

-- Create syntax for TABLE 'condor_comm_target'
CREATE TABLE  IF NOT EXISTS `condor_comm_target` (
  `datasetId` int(11) NOT NULL DEFAULT '0',
  `comm_id` int(11) NOT NULL DEFAULT '0',
  `comm_to` varchar(250) NOT NULL DEFAULT '',
  `tag` varchar(5) NOT NULL DEFAULT '',
  `messageId` varchar(250) NOT NULL DEFAULT '',
  UNIQUE KEY `datasetId_comm_tag` (`comm_id`,`comm_to`,`datasetId`,`tag`),
  UNIQUE KEY `messageIdIdx_To` (`datasetId`,`messageId`,`comm_to`),
  KEY `datasetId` (`datasetId`),
  KEY `comm_id` (`comm_id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

-- Create syntax for TABLE 'condor_datasets'
CREATE TABLE  IF NOT EXISTS `condor_datasets`(
  `datasetId` tinyint(4) NOT NULL AUTO_INCREMENT,
  `dataset` varchar(125) NOT NULL DEFAULT '',
  PRIMARY KEY (`datasetId`),
UNIQUE KEY `dataset` (`dataset`)
) ENGINE=InnoDB AUTO_INCREMENT=8 DEFAULT CHARSET=latin1;

-- create dataset
INSERT INTO condor_datasets (datasetId, dataset) VALUES(1, 'wiki years -3500 to 2013');

INSERT INTO condor_chars (datasetId, mailaddress, organization, domain, name)
    SELECT 1 as dataset, name, '', '', name FROM people where year_from is not null;

drop table if exists condor_comm_tmp;

create table condor_comm_tmp as
  SELECT 1 as datasetId, c.id as comm_id, p.name as comm_from, concat('year from ', c.year_from) AS comm_subject,
         CAST(concat(c.year_from + 3500+2000, RIGHT(from_unixtime(ceil(rand()*365*60*60*23)), 15)) AS DATETIME)  as comm_time,
         concat('year from ', c.year_from) as comm_content, MAKEDATE(c.year_from + 3500+2000, 1) as timestamp, c.id as messageId
  FROM connections c INNER JOIN people p ON c.person_from = p.id
    INNER JOIN tmp_people_toinclude tf on  c.person_from = tf.id
    INNER JOIN tmp_people_toinclude tt on  c.person_to = tt.id
  WHERE c.year_from is not null;


drop table if exists tmpidstochange;
create table tmpidstochange as  select min(comm_id) as commid, rand() as rnd
                                from (select comm_id,  comm_from, comm_time from condor_comm_tmp) a
                                group by comm_from, comm_time
                                having count(*) > 1;

alter table tmpidstochange add primary key (commid);

update condor_comm_tmp, tmpidstochange set comm_time = CAST(concat(year(comm_time) + 3500+2000, RIGHT(from_unixtime(ceil(rnd*365*60*60*23)), 15)) AS DATETIME) where comm_id = commid;


insert into condor_comm select * from condor_comm_tmp;

INSERT INTO condor_comm_target (datasetId, comm_id, comm_to, tag, messageId)
    SELECT 1 as dataset, c.id as commId, p.name as commto, c.year_from as tag, c.id as messageId
    FROM connections c INNER JOIN people p ON c.person_to = p.id
      INNER JOIN tmp_people_toinclude tf on  c.person_from = tf.id
      INNER JOIN tmp_people_toinclude tt on  c.person_to = tt.id
    WHERE c.year_from is not null;



mysqldump -u root wikilanguage2 condor_chars condor_comm condor_comm_target condor_datasets | pv | pigz > condor.sql.gz