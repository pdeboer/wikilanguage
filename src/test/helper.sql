select name from categories c inner join people2categories p2c on c.id=p2c.`category` and person=675;

select p.id, p.name, count(distinct year_id) as numYears, max(dataInt) as max_indegree
from `year_people_experiments` y inner join people p on y.person_id=p.id
group by y.person_id
order by count(distinct year_id)  desc;

-- http://en.wikipedia.org/w/index.php?title=Special:WhatLinksHere/John_Boehner&limit=500


create table tmp_valid_connections_years as
select person_from, person_to, p1.year_from as from_birth, p1.year_to as from_death, p2.year_from as to_birth,
   p2.year_to as to_death, c.year_from as conn_from, c.year_to as conn_to
from connections c
   inner join people p1 on c.person_from = p1.id and p1.year_from is not null
   inner join people p2 on c.person_to = p2.id and p2.year_from is not null;

create index connFromTmp on tmp_valid_connections_years (conn_from);
create index connToTmp on tmp_valid_connections_years (conn_to);



select person_from, person_to, 1 AS w from tmp_valid_connections
into outfile '/tmp/connectionswiki_full_year.txt3' fields terminated by ' ' optionally enclosed by '' lines terminated by '\n';



-- clean up
update connections set year_from = (year_from*-1)/100 where year_from < -3500





select name, indegree, `dataDouble`, year_id from people p inner join people_aux a on p.id=a.id
  inner join `year_people_experiments` y on p.id=y.person_id order by `dataDouble` desc, indegree desc