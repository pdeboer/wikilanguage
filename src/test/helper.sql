select name from categories c inner join people2categories p2c on c.id=p2c.`category` and person=675;

select p.id, p.name, count(distinct year_id) as numYears, max(dataInt) as max_indegree
from `year_people_experiments` y inner join people p on y.person_id=p.id
group by y.person_id
order by count(distinct year_id)  desc;

-- http://en.wikipedia.org/w/index.php?title=Special:WhatLinksHere/John_Boehner&limit=500


create table tmp_valid_connections as
select person_from, person_to from connections c
   inner join people p1 on c.person_from = p1.id and p1.year_from is not null
   inner join people p2 on c.person_to = p2.id and p2.year_from is not null;


select person_from, person_to, 1 AS w from tmp_valid_connections
into outfile '/tmp/connectionswiki_full_year.txt3' fields terminated by ' ' optionally enclosed by '' lines terminated by '\n';