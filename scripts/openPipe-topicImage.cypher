// ARTIST self-portraits
MATCH (a:Artist)<-[r:BY]-(x:Asset)
WITH a, r, x
SET r.representative =
(CASE WHEN x.name CONTAINS a.name THEN 10 ELSE 0 END)
+ (CASE WHEN x.name =~ '(?i)^.*self.*' THEN 6 ELSE 0 END)
+ (CASE WHEN x.name =~ '(?i)^.*portrait.*' THEN 4 ELSE 0 END)
+ (size([tag IN x.tags WHERE (tag='Self-portraits')]) * 10)
+ size([tag IN x.tags WHERE (tag='Artists' OR tag='Portraits')]);

MATCH (a:Artist)<-[r:BY]-(:Asset)
  WHERE r.representative > 0
WITH a, r ORDER BY r.representative DESC
WITH a, head(collect(r)) as prime
SET prime.prime = true
;

// CULTURE
MATCH (a:Culture)<-[r:ASSET_CULTURE]-(x:Asset)
WITH a, r, x
SET r.representative =
(CASE WHEN x.name CONTAINS a.name THEN 10 ELSE 0 END)
+ (CASE WHEN x.name =~ '(?i)^.*culture.*' THEN 6 ELSE 0 END)
+ (CASE WHEN x.name =~ '(?i)^.*nation.*' THEN 4 ELSE 0 END)
+ (CASE WHEN x.name =~ '(?i)^.*country.*' THEN 2 ELSE 0 END)
+ (size([tag IN x.tags WHERE (tag='Cities' OR tag='Towns')]) * 3)
+ (size([tag IN x.tags WHERE (tag='People' OR tag='Utilitarian Objects' OR tag='Bridges')]) * 2)
+ size([tag IN x.tags WHERE (tag='Landscapes' OR tag='Architecture' OR tag='Churches' OR tag='Battles' OR tag='Masks' OR tag='Games')]);

MATCH (a:Culture)<-[r:ASSET_CULTURE]-(:Asset)
  WHERE r.representative > 0
WITH a, r ORDER BY r.representative DESC
WITH a, head(collect(r)) as prime
SET prime.prime = true
;

// CLASS
MATCH (a:Classification)<-[r:ASSET_CLASS]-(x:Asset)
WITH a, r, x
SET r.representative =
(CASE WHEN trim(x.name) = a.name THEN 10 ELSE 0 END)
+ (CASE WHEN x.name CONTAINS a.name THEN 6 ELSE 0 END);

MATCH (a:Classification)<-[r:ASSET_CLASS]-(:Asset)
  WHERE r.representative > 0
WITH a, r ORDER BY r.representative DESC
WITH a, head(collect(r)) as prime
SET prime.prime = true
;

// GENRE
MATCH (a:Genre)<-[r:ASSET_GENRE]-(x:Asset)
WITH a, r, x
SET r.representative =
(CASE WHEN trim(x.name) = a.name THEN 10 ELSE 0 END)
+ (CASE WHEN x.name CONTAINS a.name THEN 6 ELSE 0 END);

MATCH (a:Genre)<-[r:ASSET_GENRE]-(:Asset)
  WHERE r.representative > 0
WITH a, r ORDER BY r.representative DESC
WITH a, head(collect(r)) as prime
SET prime.prime = true
;

// MEDIUM
MATCH (a:Medium)<-[r:ASSET_MEDIUM]-(x:Asset)
WITH a, r, x
SET r.representative =
(CASE WHEN trim(x.name) = a.name THEN 10 ELSE 0 END)
+ (CASE WHEN x.name CONTAINS a.name THEN 6 ELSE 0 END);

MATCH (a:Medium)<-[r:ASSET_MEDIUM]-(:Asset)
  WHERE r.representative > 0
WITH a, r ORDER BY r.representative DESC
WITH a, head(collect(r)) as prime
SET prime.prime = true
;

// NATION
MATCH (a:Nation)<-[r:ASSET_NATION]-(x:Asset)
WITH a, r, x
SET r.representative =
(CASE WHEN x.name CONTAINS a.name THEN 10 ELSE 0 END)
+ (CASE WHEN x.name =~ '(?i)^.*country.*' THEN 6 ELSE 0 END)
+ (CASE WHEN x.name =~ '(?i)^.*nation.*' THEN 4 ELSE 0 END)
+ (size([tag IN x.tags WHERE (tag='Flags' OR tag='Landscapes' OR tag='Capitals')]) * 2)
+ size([tag IN x.tags WHERE (tag='Cities' OR tag='Towns' OR tag="Streets" OR tag="Buildings")])
;

MATCH (a:Nation)<-[r:ASSET_NATION]-(:Asset)
  WHERE r.representative > 0
WITH a, r ORDER BY r.representative DESC
WITH a, head(collect(r)) as prime
SET prime.prime = true
;

// CITY
MATCH (a:City)<-[r:ASSET_CITY]-(x:Asset)
WITH a, r, x
SET r.representative =
(CASE WHEN x.name CONTAINS a.name THEN 10 ELSE 0 END)
+ (CASE WHEN x.name =~ '(?i)^.*city.*' THEN 6 ELSE 0 END)
+ (CASE WHEN x.name =~ '(?i)^.*town.*' THEN 4 ELSE 0 END)
+ (size([tag IN x.tags WHERE (tag='Flags' OR tag='Landscapes' OR tag='Capitals')]) * 2)
+ size([tag IN x.tags WHERE (tag='Cities' OR tag='Towns' OR tag="Streets" OR tag="Buildings")])
;

MATCH (a:City)<-[r:ASSET_CITY]-(:Asset)
  WHERE r.representative > 0
WITH a, r ORDER BY r.representative DESC
WITH a, head(collect(r)) as prime
SET prime.prime = true
;

// TAG
MATCH (a:Tag)<-[r:ASSET_TAG]-(x:Asset)
WITH a, r, x
SET r.representative =
(CASE WHEN x.name CONTAINS a.name THEN 10 ELSE 0 END)
+ (CASE WHEN size(x.tags) = 1 THEN 4 ELSE 0 END)
;

MATCH (a:Tag)<-[r:ASSET_TAG]-(:Asset)
  WHERE r.representative > 0
WITH a, r ORDER BY r.representative DESC
WITH a, head(collect(r)) as prime
SET prime.prime = true
;

// Set Topic images
MATCH (a:Topic)<-[r]-(x:Asset)
  WHERE r.prime=true AND exists(x.primaryImageSmall)
SET a.smallImage = x.primaryImageSmall
;

// Find Topics without an image, and pick one randomly
MATCH (a:Topic)
WHERE NOT EXISTS(a.smallImage)
MATCH (a)<-[]-(x:Asset)
WITH a, head(collect(x)) as asset
SET a.smallImage = asset.primaryImageSmall
