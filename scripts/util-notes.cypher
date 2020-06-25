// Each of these are useful single-commands for copy-pasta into cypher-shell

// my personal fav
CALL apoc.meta.graph()

// all properties for a label
CALL apoc.meta.data() YIELD label, property
WHERE label = 'Asset'
RETURN property

// all distinct values of a property
MATCH (x:Asset {import:2})
RETURN DISTINCT x.classification;

// count occurrences of each property
CALL apoc.meta.data() YIELD label, property
WHERE label = 'Asset'
WITH property
MATCH (x:Asset {import:2})
RETURN property, count(x[property])

// count occurrences of each value in of property
// this one is important, it shows how dirty the data is
MATCH (x:Asset {import:2})
WHERE x.period IS NOT null
WITH DISTINCT x.period as p, count(x) as c
WHERE c > 2 AND (p CONTAINS 'Edo' OR p CONTAINS 'Qing')
RETURN p, c
ORDER BY c DESC
//period
//country
//culture
//artistDisplayBio


// periods and counts
MATCH (x:Asset)
WITH DISTINCT x.period as period, coalesce(x.artistEndDate) as artDate, count(x) as cnt
WITH DISTINCT period, head(collect(artDate)) as randDate, sum(cnt) as s
RETURN period, randDate, s ORDER BY s DESC


// intersections
MATCH (a:Artist)-[]->(x)<-[]-(t:Tag)
WHERE a.name = 'Edgar Degas' and t.name = 'Women'
WITH x
MATCH (x)-[]->(c:Classification)
WITH c.name as n, count(x) as cnt
RETURN n, cnt
ORDER BY cnt desc

MATCH (a:Nation)-[]->(x)<-[]-(t:Tag)
  WHERE
  a.name = 'Greek' and t.name = 'Men'
WITH x
MATCH (c:Classification)-[]->(x)
  WHERE c.name = 'Sculpture'
RETURN filter(l in labels(x) WHERE l<>'Topic')[0], x.name, x.smallImage

MATCH (a:Culture)<-[:ART_CULTURE]-(x:Asset)-[:ART_TAG]->(b:Tag)
  WHERE
  a.name = 'Greek' and b.name = 'Men'
WITH a,b,x
MATCH (c:Classification)<-[r:ART_CLASS]-(x:Asset)
  WHERE c.name CONTAINS 'Sculpture'
RETURN a,b,c,x,r


// TopicAssets Query
MATCH (a:Artist {name: 'Edgar Degas'})<-[:BY]-(x:Asset)
RETURN x.name
ORDER BY x.score_generated DESC, x.openpipe_id ASC
