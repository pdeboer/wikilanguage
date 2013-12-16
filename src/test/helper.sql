select name from categories c inner join people2categories p2c on c.id=p2c.`category` and person=675;

select p.id, p.name, count(distinct year_id) as numYears, max(dataInt) as max_indegree
from `year_people_experiments` y inner join people p on y.person_id=p.id
group by y.person_id
order by count(distinct year_id)  desc;

-- http://en.wikipedia.org/w/index.php?title=Special:WhatLinksHere/John_Boehner&limit=500