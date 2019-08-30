// Each of these are useful single-commands for copy-pasta into cypher-shell

// my personal fav
CALL apoc.meta.graph()

// all properties for a label
CALL apoc.meta.data() YIELD label, property
WHERE label = 'Artwork'
RETURN property

// all distinct values of a property
MATCH (x:Artwork {import:2})
RETURN DISTINCT x.classification;

// count occurrences of each property
CALL apoc.meta.data() YIELD label, property
WHERE label = 'Artwork'
WITH property
MATCH (x:Artwork {import:2})
RETURN property, count(x[property])

// count occurrences of each value in of property
// this one is important, it shows how dirty the data is
MATCH (x:Artwork {import:2})
WHERE x.period IS NOT null
WITH DISTINCT x.period as p, count(x) as c
WHERE c > 2 AND (p CONTAINS 'Edo' OR p CONTAINS 'Qing')
RETURN p, c
ORDER BY c DESC
//period
//country
//culture
//artistDisplayBio