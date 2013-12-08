-- populate table with expected age for people where we know only half the story
update people set year_from = year_to - 100 where year_from is null;
update people set year_to = year_from + 100 where year_to is null;

-- populate years table
update years
  set people_alive = (select count(*) from people where year_from is not null and years.id between year_from and year_to );