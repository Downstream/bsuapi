// copy-pasta into browser, or pipe into cypher-shell:
// cat metimport-4-TopicsRepresentative.cypher | cypher-shell -u [user] -p [password] --format plain > cypherscript.log 2>&1 &; tail -f cypherscript.log
// RUN previous imports first, including CREATE EMPTY ARTWORKS

// ARTIST self-portraits
MATCH (a:Artist)<-[r:BY]-(x:Artwork)
WITH a, r, x
SET r.representative =
(CASE WHEN x.title CONTAINS a.name THEN 10 ELSE 0 END)
+ (CASE WHEN x.title =~ '(?i)^.*self.*' THEN 6 ELSE 0 END)
+ (CASE WHEN x.title =~ '(?i)^.*portrait.*' THEN 4 ELSE 0 END)
+ (size([tag IN x.tags WHERE (tag='Self-portraits')]) * 10)
+ size([tag IN x.tags WHERE (tag='Artists' OR tag='Portraits')])
;

// set a searchable flag in the relationship
MATCH (a:Artist)<-[r:BY]-(:Artwork)
  WHERE r.representative > 0
WITH a, r ORDER BY r.representative DESC
WITH a, head(collect(r)) as prime
SET prime.prime = true
;

// NATION
MATCH (a:Nation)<-[r:ART_NATION]-(x:Artwork)
WITH a, r, x
SET r.representative =
(CASE WHEN x.title CONTAINS a.name THEN 10 ELSE 0 END)
+ (CASE WHEN x.title =~ '(?i)^.*country.*' THEN 6 ELSE 0 END)
+ (CASE WHEN x.title =~ '(?i)^.*nation.*' THEN 4 ELSE 0 END)
+ (size([tag IN x.tags WHERE (tag='Flags' OR tag='Landscapes' OR tag='Capitals')]) * 2)
+ size([tag IN x.tags WHERE (tag='Cities' OR tag='Towns' OR tag="Streets" OR tag="Buildings")])
;

MATCH (a:Nation)<-[r:ART_NATION]-(:Artwork)
  WHERE r.representative > 0
WITH a, r ORDER BY r.representative DESC
WITH a, head(collect(r)) as prime
SET prime.prime = true
;

// CULTURE
MATCH (a:Culture)<-[r:ART_CULTURE]-(x:Artwork)
WITH a, r, x
SET r.representative =
(CASE WHEN x.title CONTAINS a.name THEN 10 ELSE 0 END)
+ (CASE WHEN x.title =~ '(?i)^.*culture.*' THEN 6 ELSE 0 END)
+ (CASE WHEN x.title =~ '(?i)^.*nation.*' THEN 4 ELSE 0 END)
+ (CASE WHEN x.title =~ '(?i)^.*country.*' THEN 2 ELSE 0 END)
+ (size([tag IN x.tags WHERE (tag='Cities' OR tag='Towns')]) * 3)
+ (size([tag IN x.tags WHERE (tag='People' OR tag='Utilitarian Objects' OR tag='Bridges')]) * 2)
+ size([tag IN x.tags WHERE (tag='Landscapes' OR tag='Architecture' OR tag='Churches' OR tag='Battles' OR tag='Masks' OR tag='Games')])
;

MATCH (a:Culture)<-[r:ART_CULTURE]-(:Artwork)
  WHERE r.representative > 0
WITH a, r ORDER BY r.representative DESC
WITH a, head(collect(r)) as prime
SET prime.prime = true
;

// TAG
MATCH (a:Tag)<-[r:ART_TAG]-(x:Artwork)
WITH a, r, x
SET r.representative =
(CASE WHEN x.title CONTAINS a.name THEN 10 ELSE 0 END)
+ (CASE WHEN size(x.tags) = 1 THEN 4 ELSE 0 END)
;

MATCH (a:Tag)<-[r:ART_TAG]-(:Artwork)
  WHERE r.representative > 0
WITH a, r ORDER BY r.representative DESC
WITH a, head(collect(r)) as prime
SET prime.prime = true
;

// Classification
MATCH (a:Classification)<-[r:ART_CLASS]-(x:Artwork)
WITH a, r, x
SET r.representative =
(CASE WHEN trim(x.title) = a.name THEN 10 ELSE 0 END)
+ (CASE WHEN x.title CONTAINS a.name THEN 6 ELSE 0 END)
;

// set a searchable flag in the relationship
MATCH (a:Classification)<-[r:ART_CLASS]-(:Artwork)
  WHERE r.representative > 0
WITH a, r ORDER BY r.representative DESC
WITH a, head(collect(r)) as prime
SET prime.prime = true
;


// Set Topic images
MATCH (a:Topic)<-[r]-(x:Artwork)
  WHERE r.prime=true AND exists(x.primaryImageSmall)
SET a.smallImage = x.primaryImageSmall
;