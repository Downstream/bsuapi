MATCH (api:OpenPipeConfig {name: 'api'})
SET api.assetPage = 0
SET api.topicPage = 0
SET api.topicRun = apoc.temporal.format( date(), 'YYYY-MM-dd')
SET api.thisRun = apoc.temporal.format( date(), 'YYYY-MM-dd')
RETURN "RESET api counts to prepare for new sync." as t LIMIT 1
;

CALL apoc.periodic.commit("
MERGE (script:Script {name: 'OPENPIPE_TOPICS'})
WITH script
MATCH (api:OpenPipeConfig {name: 'api'})
SET api.topicPage = (api.topicPage + 1)
SET script.page = api.topicPage
SET api.lastTopicUrl = api.allTopics + '?ps=' + api.assetsPerPage + '&p=' + api.topicPage
WITH api.lastTopicUrl as url
CALL apoc.load.json(url) YIELD value

UNWIND value.availableTopics AS topicType

UNWIND value[topicType].data AS topic

WITH url, apoc.text.capitalizeAll(topicType) AS topicType, topic, SPLIT(topic.guid,'/') as guidLong
WITH url, topicType, topic, COALESCE(guidLong[size(guidLong)-2], '0') + '/' + COALESCE(guidLong[size(guidLong)-1], '0') as guid

MERGE (t:Topic {guid:guid})
SET
  t.name = topic.value,
  t.biography=topic.biography

WITH url, t, topicType
CALL apoc.create.addLabels(t, [topicType]) YIELD node

RETURN COUNT(node) AS t LIMIT {limit}
"
,{limit: 1000}
) YIELD updates, batches, failedBatches
MATCH (api:OpenPipeConfig {name: 'api'})
RETURN "COMPLETED TOPIC IMPORT apoc.periodic.commit(apoc.load.json( "+ api.allTopics +" )) updates:" + updates + " batches:" + batches + " failedBatches:" + failedBatches as t LIMIT 1
;



CALL apoc.periodic.commit("
MATCH (script:Script {name: 'OPENPIPE_SYNC'})
MATCH (canon:OpenPipeConfig {name: 'canonical'})
MATCH (api:OpenPipeConfig {name: 'api'})
SET api.assetPage = (api.assetPage + 1)
SET script.page = api.assetPage
SET api.lastUrl = api.allAssets + '?changeStart=' + api.lastRun + '&ps=' + api.assetsPerPage + '&p=' + api.assetPage
WITH canon, api.lastUrl as url
CALL apoc.load.json(url) YIELD value
UNWIND value.data AS asset

WITH asset, canon, value.total as pageAssetsCount,
  bsuapi.obj.singleClean(asset.openpipe_canonical.title) AS name,
	asset.openpipe_canonical.id AS guid,
	bsuapi.obj.singleCleanObj(asset.openpipe_canonical.date,[canon.date]) AS openpipe_date,
	toFloat(bsuapi.obj.singleClean(asset.openpipe_canonical.latitude)) AS openpipe_latitude,
	toFloat(bsuapi.obj.singleClean(asset.openpipe_canonical.longitude)) AS openpipe_longitude,
	bsuapi.obj.singleCleanObj(asset.openpipe_canonical.moment,['0']) AS openpipe_moment,
	bsuapi.obj.singleClean(asset.openpipe_canonical.biography) AS openpipe_bio,
	bsuapi.obj.singleClean(asset.openpipe_canonical.physicalDimensions) AS openpipe_dimensions

WITH asset, pageAssetsCount, name, guid,
  openpipe_date,
  openpipe_latitude,
  openpipe_longitude,
  openpipe_moment,
  openpipe_bio,
  openpipe_dimensions,
  CASE WHEN openpipe_dimensions CONTAINS ',' THEN [n IN split(openpipe_dimensions, ',') | toFloat(n)] ELSE null END AS dimensions,

	bsuapi.obj.openPipeCleanObj(asset.openpipe_canonical.artist, [canon.artist, '']) AS openpipe_artist,
	bsuapi.obj.openPipeCleanObj(asset.openpipe_canonical.classification, [canon.classification, '']) AS openpipe_classification,
	bsuapi.obj.openPipeCleanObj(asset.openpipe_canonical.culture, [canon.culture, '']) AS openpipe_culture,
	bsuapi.obj.openPipeCleanObj(asset.openpipe_canonical.genre, [canon.genre, '']) AS openpipe_genre,
	bsuapi.obj.openPipeCleanObj(asset.openpipe_canonical.medium, [canon.medium, '']) AS openpipe_medium,
	bsuapi.obj.openPipeCleanObj(asset.openpipe_canonical.nation, [canon.nation, '']) AS openpipe_nation,
	bsuapi.obj.openPipeCleanObj(asset.openpipe_canonical.city, [canon.city, '']) AS openpipe_city,
	bsuapi.obj.openPipeCleanObj(asset.openpipe_canonical.tags, [canon.tags, '']) AS openpipe_tags
    LIMIT {limit}
WHERE (asset.name <> '' AND guid IS NOT NULL AND NOT openpipe_date CONTAINS 'YYYY')

MERGE (x:Asset {guid: guid})

SET x.import = 0
SET x.type = 'Asset'
SET x.score_generated = 0
SET x.name = name
SET x.metaDataId = bsuapi.coll.singleClean(asset.metaDataId)
SET x.guid = guid
SET x.primaryImageSmall = bsuapi.obj.singleClean(asset.openpipe_canonical.smallImage)
SET x.primaryImageSmallDimensions = bsuapi.obj.singleClean(asset.openpipe_canonical.smallImageDimensions)
SET x.primaryImageLarge = bsuapi.obj.singleClean(asset.openpipe_canonical.largeImage)
SET x.primaryImageLargeDimensions = bsuapi.obj.singleClean(asset.openpipe_canonical.largeImageDimensions)
SET x.primaryImageFull = bsuapi.obj.singleClean(asset.openpipe_canonical.fullImage)
SET x.primaryImageFullDimensions = bsuapi.obj.singleClean(asset.openpipe_canonical.fullImageDimensions)

SET x.openpipe_latitude = openpipe_latitude
SET x.openpipe_longitude = openpipe_longitude
SET x.hasGeo = (openpipe_latitude IS NOT NULL AND openpipe_longitude IS NOT NULL)
SET x.latlong = CASE WHEN x.hasGeo THEN [openpipe_latitude, openpipe_longitude] ELSE null END
SET x.wgs = CASE WHEN x.hasGeo THEN point({x: openpipe_latitude, y: openpipe_longitude, crs: 'wgs-84'}) ELSE null END
SET x.openpipe_date = openpipe_date
SET x.date = date(bsuapi.obj.openPipeDateMap(x.openpipe_date))
SET x.moment = openpipe_moment
SET x.biography = openpipe_bio
SET x.openpipe_dimensions = openpipe_dimensions
SET x.dimensions = CASE WHEN dimensions CONTAINS ',' THEN [n IN split(dimensions, ',') | toFloat(n)] ELSE null END
SET x.source = bsuapi.obj.singleClean(asset.openpipe_canonical.source)

SET x.openpipe_artist = [k IN KEYS(openpipe_artist) | openpipe_artist[k]]
SET x.openpipe_culture = [k IN KEYS(openpipe_culture) | openpipe_culture[k]]
SET x.openpipe_classification = [k IN KEYS(openpipe_classification) | openpipe_classification[k]]
SET x.openpipe_genre = [k IN KEYS(openpipe_genre) | openpipe_genre[k]]
SET x.openpipe_medium = [k IN KEYS(openpipe_medium) | openpipe_medium[k]]
SET x.openpipe_city = [k IN KEYS(openpipe_city) | openpipe_city[k]]
SET x.openpipe_nation = [k IN KEYS(openpipe_nation) | openpipe_nation[k]]
SET x.openpipe_tags = [k IN KEYS(openpipe_tags) | openpipe_tags[k]]
SET x.openpipe_guid_artist = KEYS(openpipe_artist)
SET x.openpipe_guid_culture = KEYS(openpipe_culture)
SET x.openpipe_guid_classification = KEYS(openpipe_classification)
SET x.openpipe_guid_genre = KEYS(openpipe_genre)
SET x.openpipe_guid_medium = KEYS(openpipe_medium)
SET x.openpipe_guid_nation = KEYS(openpipe_nation)
SET x.openpipe_guid_city = KEYS(openpipe_city)
SET x.openpipe_guid_tags = KEYS(openpipe_tags)

RETURN max(pageAssetsCount) LIMIT 1
"
,{limit: 1000}
) YIELD updates, batches, failedBatches

MATCH (api:OpenPipeConfig {name: 'api'})
RETURN "COMPLETED IMPORT apoc.periodic.commit(apoc.load.json( "+ api.allAssets +" )) updates:" + updates + " batches:" + batches + " failedBatches:" + failedBatches as t LIMIT 1
;

MATCH (x:Asset {import: 0})
SET x.import = 1
WITH x
OPTIONAL MATCH (x)-[r]->()
DELETE r
RETURN "Wiped old asset relations for imported assets" as t LIMIT 1;

RETURN "STARTING Asset:Topic relationships" as t;

CALL apoc.periodic.iterate("MATCH (x:Asset {import: 1}) RETURN x","
  UNWIND x.openpipe_guid_artist as guid MERGE (t:Artist {guid: guid})
  MERGE (x)-[:BY]->(t)
  WITH t,
    CASE WHEN t.dateStart IS NULL OR t.dateStart > x.date THEN x.date ELSE t.dateStart END AS dateStart,
    CASE WHEN t.dateEnd IS NULL OR t.dateEnd < x.date THEN x.date ELSE t.dateEnd END AS dateEnd
  SET t.dateStart = dateStart, t.dateEnd = dateEnd, t.hasGeo = null
  SET t :Topic
",
  {batchSize:10000, iterateList:true, parallel:false}
) YIELD operations
RETURN "BUILDING Topics, ASSET:ARTIST relationships complete - committed:"+ operations.committed +" failed:"+ operations.failed as t LIMIT 1
;

CALL apoc.periodic.iterate("MATCH (x:Asset {import: 1}) RETURN x","
  UNWIND x.openpipe_guid_culture as guid MERGE (t:Culture {guid: guid})
  MERGE (x)-[:ASSET_CULTURE]->(t)
  WITH t,
    CASE WHEN t.dateStart IS NULL OR t.dateStart > x.date THEN x.date ELSE t.dateStart END AS dateStart,
    CASE WHEN t.dateEnd IS NULL OR t.dateEnd < x.date THEN x.date ELSE t.dateEnd END AS dateEnd
  SET t.dateStart = dateStart, t.dateEnd = dateEnd, t.hasGeo = null
  SET t :Topic
",
{batchSize:10000, iterateList:true, parallel:false}
) YIELD operations
RETURN "BUILDING Topics, ASSET:CULTURE relationships complete - committed:"+ operations.committed +" failed:"+ operations.failed as t LIMIT 1
;

CALL apoc.periodic.iterate("MATCH (x:Asset {import: 1}) RETURN x","
  UNWIND x.openpipe_guid_classification as guid MERGE (t:Classification {guid: guid})
  MERGE (x)-[:ASSET_CLASS]->(t)
  WITH t,
    CASE WHEN t.dateStart IS NULL OR t.dateStart > x.date THEN x.date ELSE t.dateStart END AS dateStart,
    CASE WHEN t.dateEnd IS NULL OR t.dateEnd < x.date THEN x.date ELSE t.dateEnd END AS dateEnd
  SET t.dateStart = dateStart, t.dateEnd = dateEnd, t.hasGeo = null
  SET t :Topic
",
{batchSize:10000, iterateList:true, parallel:false}
) YIELD operations
RETURN "BUILDING Topics, ASSET:CLASS relationships complete - committed:"+ operations.committed +" failed:"+ operations.failed as t LIMIT 1
;

CALL apoc.periodic.iterate("MATCH (x:Asset {import: 1}) RETURN x","
  UNWIND x.openpipe_guid_genre as guid MERGE (t:Genre {guid: guid})
  MERGE (x)-[:ASSET_GENRE]->(t)
  WITH t,
    CASE WHEN t.dateStart IS NULL OR t.dateStart > x.date THEN x.date ELSE t.dateStart END AS dateStart,
    CASE WHEN t.dateEnd IS NULL OR t.dateEnd < x.date THEN x.date ELSE t.dateEnd END AS dateEnd
  SET t.dateStart = dateStart, t.dateEnd = dateEnd, t.hasGeo = null
  SET t :Topic
",
{batchSize:10000, iterateList:true, parallel:false}
) YIELD operations
RETURN "BUILDING Topics, ASSET:GENRE relationships complete - committed:"+ operations.committed +" failed:"+ operations.failed as t LIMIT 1
;

CALL apoc.periodic.iterate("MATCH (x:Asset {import: 1}) RETURN x","
  UNWIND x.openpipe_guid_medium as guid MERGE (t:Medium {guid: guid})
  MERGE (x)-[:ASSET_MEDIUM]->(t)
  WITH t,
    CASE WHEN t.dateStart IS NULL OR t.dateStart > x.date THEN x.date ELSE t.dateStart END AS dateStart,
    CASE WHEN t.dateEnd IS NULL OR t.dateEnd < x.date THEN x.date ELSE t.dateEnd END AS dateEnd
  SET t.dateStart = dateStart, t.dateEnd = dateEnd, t.hasGeo = null
  SET t :Topic
",
{batchSize:10000, iterateList:true, parallel:false}
) YIELD operations
RETURN "BUILDING Topics, ASSET:MEDIUM relationships complete - committed:"+ operations.committed +" failed:"+ operations.failed as t LIMIT 1
;

CALL apoc.periodic.iterate("MATCH (x:Asset {import: 1}) RETURN x","
  UNWIND x.openpipe_guid_nation as guid MERGE (t:Nation {guid: guid})
  MERGE (x)-[:ASSET_NATION]->(t)
  WITH t,
    CASE WHEN t.dateStart IS NULL OR t.dateStart > x.date THEN x.date ELSE t.dateStart END AS dateStart,
    CASE WHEN t.dateEnd IS NULL OR t.dateEnd < x.date THEN x.date ELSE t.dateEnd END AS dateEnd
  SET t.dateStart = dateStart, t.dateEnd = dateEnd, t.hasGeo = null
  SET t :Topic
",
{batchSize:10000, iterateList:true, parallel:false}
) YIELD operations
RETURN "BUILDING Topics, ASSET:NATION relationships complete - committed:"+ operations.committed +" failed:"+ operations.failed as t LIMIT 1
;

CALL apoc.periodic.iterate("MATCH (x:Asset {import: 1}) RETURN x","
  UNWIND x.openpipe_guid_city as guid MERGE (t:City {guid: guid})
  MERGE (x)-[:ASSET_CITY]->(t)
  WITH t,
    CASE WHEN t.dateStart IS NULL OR t.dateStart > x.date THEN x.date ELSE t.dateStart END AS dateStart,
    CASE WHEN t.dateEnd IS NULL OR t.dateEnd < x.date THEN x.date ELSE t.dateEnd END AS dateEnd
  SET t.dateStart = dateStart, t.dateEnd = dateEnd, t.hasGeo = null
  SET t :Topic
",
{batchSize:10000, iterateList:true, parallel:false}
) YIELD operations
RETURN "BUILDING Topics, ASSET:CITY relationships complete - committed:"+ operations.committed +" failed:"+ operations.failed as t LIMIT 1
;

CALL apoc.periodic.iterate("MATCH (x:Asset {import: 1}) RETURN x","
  UNWIND x.openpipe_guid_tags as guid MERGE (t:Tag {guid: guid})
  MERGE (x)-[:ASSET_TAG]->(t)
  WITH t,
    CASE WHEN t.dateStart IS NULL OR t.dateStart > x.date THEN x.date ELSE t.dateStart END AS dateStart,
    CASE WHEN t.dateEnd IS NULL OR t.dateEnd < x.date THEN x.date ELSE t.dateEnd END AS dateEnd
  SET t.dateStart = dateStart, t.dateEnd = dateEnd, t.hasGeo = null
  SET t :Topic
",
{batchSize:10000, iterateList:true, parallel:false}
) YIELD operations
RETURN "BUILDING Topics, ASSET:TAG relationships complete - committed:"+ operations.committed +" failed:"+ operations.failed as t LIMIT 1
;

CALL apoc.periodic.iterate("MATCH (:Asset {hasGeo: true})-[]->(t:Topic) RETURN t","
  SET t.hasGeo = true
",
{batchSize:10000, iterateList:true, parallel:false}
) YIELD operations
RETURN "SET Topic hasGeo from Assets - committed:"+ operations.committed +" failed:"+ operations.failed as t LIMIT 1
;


RETURN "STARTING Topic MetaGraph" as t;

CALL apoc.periodic.iterate(
"
      MATCH (a:Artist)<--(:Asset)-->(b:Topic)
      RETURN a, b
","
      MERGE (a)<-[r:ARTIST]-(b)
      ON CREATE SET r.strength = 1
      ON MATCH SET r.strength = r.strength + 1
", {batchSize:10000, iterateList:true, parallel:false}
) YIELD operations
RETURN "BUILDING Topic MetaGraph, ARTIST relationships complete - committed:"+ operations.committed +" failed:"+ operations.failed as t LIMIT 1
;

CALL apoc.periodic.iterate(
"
      MATCH (a:Culture)<--(:Asset)-->(b:Topic)
      RETURN a, b
","
      MERGE (a)<-[r:CULTURE]-(b)
      ON CREATE SET r.strength = 1
      ON MATCH SET r.strength = r.strength + 1
", {batchSize:10000, iterateList:true, parallel:false}
) YIELD operations
RETURN "BUILDING Topic MetaGraph, CULTURE relationships complete - committed:"+ operations.committed +" failed:"+ operations.failed as t LIMIT 1
;

CALL apoc.periodic.iterate(
"
      MATCH (a:Genre)<--(:Asset)-->(b:Topic)
      RETURN a, b
","
      MERGE (a)<-[r:GENRE]-(b)
      ON CREATE SET r.strength = 1
      ON MATCH SET r.strength = r.strength + 1
", {batchSize:10000, iterateList:true, parallel:false}
) YIELD operations
RETURN "BUILDING Topic MetaGraph, GENRE relationships complete - committed:"+ operations.committed +" failed:"+ operations.failed as t LIMIT 1
;

CALL apoc.periodic.iterate(
"
      MATCH (a:Medium)<--(:Asset)-->(b:Topic)
      RETURN a, b
","
      MERGE (a)<-[r:Medium]-(b)
      ON CREATE SET r.strength = 1
      ON MATCH SET r.strength = r.strength + 1
", {batchSize:10000, iterateList:true, parallel:false}
) YIELD operations
RETURN "BUILDING Topic MetaGraph, MEDIUM relationships complete - committed:"+ operations.committed +" failed:"+ operations.failed as t LIMIT 1
;

CALL apoc.periodic.iterate(
"
      MATCH (a:Nation)<--(:Asset)-->(b:Topic)
      RETURN a, b
","
      MERGE (a)<-[r:NATION]-(b)
      ON CREATE SET r.strength = 1
      ON MATCH SET r.strength = r.strength + 1
", {batchSize:10000, iterateList:true, parallel:false}
) YIELD operations
RETURN "BUILDING Topic MetaGraph, NATION relationships complete - committed:"+ operations.committed +" failed:"+ operations.failed as t LIMIT 1
;


CALL apoc.periodic.iterate(
"
      MATCH (a:Classification)<--(:Asset)-->(b:Topic)
      RETURN a, b
","
      MERGE (a)<-[r:CLASS]-(b)
      ON CREATE SET r.strength = 1
      ON MATCH SET r.strength = r.strength + 1
", {batchSize:10000, iterateList:true, parallel:false}
) YIELD operations
RETURN "BUILDING Topic MetaGraph, CLASSIFICATION relationships complete - committed:"+ operations.committed +" failed:"+ operations.failed as t LIMIT 1
;


CALL apoc.periodic.iterate(
"
      MATCH (a:City)<--(:Asset)-->(b:Topic)
      RETURN a, b
","
      MERGE (a)<-[r:CITY]-(b)
      ON CREATE SET r.strength = 1
      ON MATCH SET r.strength = r.strength + 1
", {batchSize:10000, iterateList:true, parallel:false}
) YIELD operations
RETURN "BUILDING Topic MetaGraph, CITY relationships complete - committed:"+ operations.committed +" failed:"+ operations.failed as t LIMIT 1
;

CALL apoc.periodic.iterate(
"
      MATCH (a:Tag)<--(:Asset)-->(b:Topic)
      RETURN a, b
","
      MERGE (a)<-[r:TAG]-(b)
      ON CREATE SET r.strength = 1
      ON MATCH SET r.strength = r.strength + 1
", {batchSize:10000, iterateList:true, parallel:false}
) YIELD operations
RETURN "BUILDING Topic MetaGraph, TAG relationships complete - committed:"+ operations.committed +" failed:"+ operations.failed as t LIMIT 1
;


MATCH (a:Artist)<-[r]-(:Asset) WITH a, count(r) as c SET a.artCount = c
RETURN "SET artCount for ARTIST" as t;

MATCH (a:Classification)<-[r]-(:Asset) WITH a, count(r) as c SET a.artCount = c
RETURN "SET artCount for CLASSIFICATION" as t;

MATCH (a:Culture)<-[r]-(:Asset) WITH a, count(r) as c SET a.artCount = c
RETURN "SET artCount for CULTURE" as t;

MATCH (a:Genre)<-[r]-(:Asset) WITH a, count(r) as c SET a.artCount = c
RETURN "SET artCount for GENRE" as t;

MATCH (a:Medium)<-[r]-(:Asset) WITH a, count(r) as c SET a.artCount = c
RETURN "SET artCount for MEDIUM" as t;

MATCH (a:Nation)<-[r]-(:Asset) WITH a, count(r) as c SET a.artCount = c
RETURN "SET artCount for NATION" as t;

MATCH (a:City)<-[r]-(:Asset) WITH a, count(r) as c SET a.artCount = c
RETURN "SET artCount for CITY" as t;

MATCH (a:Tag)<-[r]-(:Asset) WITH a, count(r) as c SET a.artCount = c
RETURN "SET artCount for TAG" as t;

MATCH (api:OpenPipeConfig {name: 'api'})
SET api.lastRun = api.thisRun
RETURN "Sync COMPLETE" as t;

MATCH (api:OpenPipeConfig {name: 'api'})

WITH api.folders + '?collectionId=all' as url, api.guidUri as guidUri
CALL apoc.load.json(url) YIELD value
UNWIND value.data AS folder
WITH folder, "200/" + bsuapi.coll.singleClean(folder.id) AS guid, bsuapi.coll.singleClean(folder.id) as openpipe_id
MERGE (f:Folder {guid:guid})
SET f.openpipe_id = openpipe_id
SET f.title = bsuapi.coll.singleClean(folder.name)
SET f.name = bsuapi.coll.singleClean(folder.name)
SET f.smallImage = bsuapi.coll.singleClean(folder.image)
SET f.layoutType = bsuapi.coll.singleClean(folder.layoutType)
SET f.insertTime = bsuapi.coll.singleClean(folder.insertTime)
SET f.lastModified = bsuapi.coll.singleClean(folder.lastModified)
WITH "Synced Folder Details" as t RETURN t;


OPTIONAL MATCH (f:Folder)<-[r:FOLDER_ASSET]-(:Asset)
DELETE r
WITH "Cleared assets from updated folders" as t RETURN t;


MATCH (f:Folder)
MATCH (api:OpenPipeConfig {name: 'api'})
WITH f, api.guidUri + f.guid as uri
CALL apoc.load.json(uri) YIELD value
UNWIND value.assets as assetEntry
WITH f,
     assetEntry.geometry as geometry,
     assetEntry.wall as wall,
     split(assetEntry.geometry, ' ') as geoSplit,
     split(assetEntry.guid,'/') as guidLong
WITH f, geometry, wall, geoSplit, guidLong,
     COALESCE(guidLong[size(guidLong)-2], '0') + '/' + COALESCE(guidLong[size(guidLong)-1], '0') as guid

MATCH (a:Asset {guid: guid})
WITH f, a, geometry, wall, geoSplit,
    CASE WHEN f.dateStart IS NULL OR f.dateStart > a.date THEN a.date ELSE f.dateStart END AS dateStart,
    CASE WHEN f.dateEnd IS NULL OR f.dateEnd < a.date THEN a.date ELSE f.dateEnd END AS dateEnd
SET f.dateStart = dateStart, f.dateEnd = dateEnd
WITH f, a, geometry, wall, geoSplit
MERGE (f)<-[r:FOLDER_ASSET]-(a)
WITH f, r, geometry, wall, geoSplit

CALL apoc.do.case([
  geometry IS NOT NULL AND size(geoSplit)>6, "
      SET r.geometry = geometry
  	  SET r.wall = wall
      SET r.size = [geoSplit[0],geoSplit[2]]
      SET r.position = [geoSplit[3] + geoSplit[4], geoSplit[5] + geoSplit[6]]
      SET f.hasLayout = true
      RETURN 1 as c
    "
],
"RETURN 1 AS c",
{f: f, r: r, geometry: geometry, wall: wall, geoSplit: geoSplit}
) YIELD value
WITH sum(value.c) AS assetCount LIMIT 1
WITH "Synced all folders and connected "+ assetCount +" assets and positional data." as t RETURN t;



CALL apoc.periodic.iterate(
"
      MATCH (f:Folder)<--(:Asset)-->(b:Topic)
      RETURN f, b
","
      MERGE (f)-[r:FOLDER_TOPIC]->(b)
      ON CREATE SET r.strength = 1
      ON MATCH SET r.strength = r.strength + 1
", {batchSize:10000, iterateList:true, parallel:false}
) YIELD operations
WITH "FOLDER:TOPIC relationships complete - committed:"+ operations.committed +" failed:"+ operations.failed as t RETURN t
;

CALL apoc.periodic.iterate("MATCH (:Asset {hasGeo: true})-[]->(f:Folder) RETURN f","
  SET f.hasGeo = true
",
{batchSize:10000, iterateList:true, parallel:false}
) YIELD operations
WITH "SET Folder hasGeo from Assets - committed:"+ operations.committed +" failed:"+ operations.failed as t RETURN t
;


MATCH (api:OpenPipeConfig {name: 'api'})
SET api.lastFolderRun = date()
WITH "Folder Sync COMPLETE" as t RETURN t;



OPTIONAL MATCH (group:OpenPipeSetting) DETACH DELETE group
RETURN "Cleared all old settings" as t;

MATCH (api:OpenPipeConfig {name: 'api'})
CALL apoc.load.json(api.settings) YIELD value
UNWIND value.data AS setting

MERGE (group:OpenPipeSetting {name: setting.groupName})
WITH group, setting.key as key, coalesce(group[setting.key], []) + setting.value as vals
UNWIND vals AS val
WITH group, key, COLLECT(distinct val) as newvals
CALL apoc.create.setProperty(group, key, newvals) YIELD node

RETURN "Loaded new Settings" as t;

MATCH (group:OpenPipeSetting)
UNWIND group.preset AS entry
WITH group, entry, split(entry, ' ') as byType
WITH group, byType[2] as type,
  CASE WHEN byType[2] IS NOT NULL AND size(byType[2])>0 THEN split(byType[0],'/') ELSE split(entry,'/') END AS guidLong
WITH group, type,
  COALESCE(guidLong[size(guidLong)-2], '0') + '/' + COALESCE(guidLong[size(guidLong)-1], '0') as guid
CALL apoc.do.case(
  [
  type IS NOT NULL AND size(type)>0 AND guid STARTS WITH "200/", "
    	MATCH (a:Folder {guid: guid})
      MERGE (group)<-[:SETTING_OPTION {byType: type}]-(a)
      RETURN 1 as c
    ",
  type IS NOT NULL AND size(type)>0, "
    	MATCH (a:Topic {guid: guid})
      MERGE (group)<-[:SETTING_OPTION {byType: type}]-(a)
      RETURN 1 as c
    ",
  guid STARTS WITH "200/", "
    	MATCH (a:Folder {guid: guid})
      MERGE (group)<-[:SETTING_OPTION]-(a)
      RETURN 1 as c
    "
  ],
    "
    	MATCH (a:Topic {guid: guid})
      MERGE (group)<-[:SETTING_OPTION]-(a)
      RETURN 1 as c
    ",
{group: group, guid: guid, type:type}
) YIELD value
RETURN "Mapped topic/folders " + sum(value.c) + " options from openpipe settings" as t;




MATCH (a:Artist)<-[r:BY]-(x:Asset)
WITH a, r, x
SET r.representative =
(CASE WHEN x.name CONTAINS a.name THEN 10 ELSE 0 END)
+ (CASE WHEN x.name =~ '(?i)^.*self.*' THEN 6 ELSE 0 END)
+ (CASE WHEN x.name =~ '(?i)^.*portrait.*' THEN 4 ELSE 0 END)
+ (size([tag IN x.openpipe_tags WHERE (tag='Self-portraits')]) * 10)
+ size([tag IN x.openpipe_tags WHERE (tag='Artists' OR tag='Portraits')])
+ (CASE WHEN size(x.openpipe_medium) >0 THEN 1 ELSE 0 END)
+ (CASE WHEN size(x.openpipe_classification) >0 THEN 1 ELSE 0 END)
+ (CASE WHEN size(x.openpipe_culture) >0 THEN 1 ELSE 0 END)
RETURN "Indentifying Artist representative images" AS t
;

OPTIONAL MATCH (a:Artist)<-[r:BY]-(:Asset)
  WHERE r.representative > 0
WITH a, r ORDER BY r.representative DESC
WITH a, head(collect(r)) as prime
SET prime.prime = true
RETURN "SET Artist representative image" AS t
;

MATCH (a:Culture)<-[r:ASSET_CULTURE]-(x:Asset)
WITH a, r, x
SET r.representative =
(CASE WHEN x.name CONTAINS a.name THEN 10 ELSE 0 END)
+ (CASE WHEN x.name =~ '(?i)^.*culture.*' THEN 6 ELSE 0 END)
+ (CASE WHEN x.name =~ '(?i)^.*nation.*' THEN 4 ELSE 0 END)
+ (CASE WHEN x.name =~ '(?i)^.*country.*' THEN 2 ELSE 0 END)
+ (size([tag IN x.openpipe_tags WHERE (tag='Cities' OR tag='Towns')]) * 3)
+ (size([tag IN x.openpipe_tags WHERE (tag='People' OR tag='Utilitarian Objects' OR tag='Bridges')]) * 2)
+ size([tag IN x.openpipe_tags WHERE (tag='Landscapes' OR tag='Architecture' OR tag='Churches' OR tag='Battles' OR tag='Masks' OR tag='Games')])
RETURN "Indentifying Culture representative images" AS t
;

OPTIONAL MATCH (a:Culture)<-[r:ASSET_CULTURE]-(:Asset)
  WHERE r.representative > 0
WITH a, r ORDER BY r.representative DESC
WITH a, head(collect(r)) as prime
SET prime.prime = true
RETURN "SET Culture representative image" AS t
;

MATCH (a:Classification)<-[r:ASSET_CLASS]-(x:Asset)
WITH a, r, x
SET r.representative =
(CASE WHEN x.name = a.name THEN 10 ELSE 0 END)
+ (CASE WHEN x.name CONTAINS a.name THEN 6 ELSE 0 END)
RETURN "Indentifying Classification representative images" AS t
;

OPTIONAL MATCH (a:Classification)<-[r:ASSET_CLASS]-(:Asset)
  WHERE r.representative > 0
WITH a, r ORDER BY r.representative DESC
WITH a, head(collect(r)) as prime
SET prime.prime = true
RETURN "SET Classification representative image" AS t
;

MATCH (a:Genre)<-[r:ASSET_GENRE]-(x:Asset)
WITH a, r, x
SET r.representative =
(CASE WHEN x.name = a.name THEN 10 ELSE 0 END)
+ (CASE WHEN x.name CONTAINS a.name THEN 6 ELSE 0 END)
RETURN "Indentifying Genre representative images" AS t
;

OPTIONAL MATCH (a:Genre)<-[r:ASSET_GENRE]-(:Asset)
  WHERE r.representative > 0
WITH a, r ORDER BY r.representative DESC
WITH a, head(collect(r)) as prime
SET prime.prime = true
RETURN "SET Genre representative image" AS t
;

MATCH (a:Medium)<-[r:ASSET_MEDIUM]-(x:Asset)
WITH a, r, x
SET r.representative =
(CASE WHEN x.name = a.name THEN 10 ELSE 0 END)
+ (CASE WHEN x.name CONTAINS a.name THEN 6 ELSE 0 END)
RETURN "Indentifying Medium representative images" AS t
;

OPTIONAL MATCH (a:Medium)<-[r:ASSET_MEDIUM]-(:Asset)
  WHERE r.representative > 0
WITH a, r ORDER BY r.representative DESC
WITH a, head(collect(r)) as prime
SET prime.prime = true
RETURN "SET Medium representative image" AS t
;

MATCH (a:Nation)<-[r:ASSET_NATION]-(x:Asset)
WITH a, r, x
SET r.representative =
(CASE WHEN x.name CONTAINS a.name THEN 10 ELSE 0 END)
+ (CASE WHEN x.name =~ '(?i)^.*country.*' THEN 6 ELSE 0 END)
+ (CASE WHEN x.name =~ '(?i)^.*nation.*' THEN 4 ELSE 0 END)
+ (size([tag IN x.openpipe_tags WHERE (tag='Flags' OR tag='Landscapes' OR tag='Capitals')]) * 2)
+ size([tag IN x.openpipe_tags WHERE (tag='Cities' OR tag='Towns' OR tag="Streets" OR tag="Buildings")])
RETURN "Indentifying Nation representative images" AS t
;

OPTIONAL MATCH (a:Nation)<-[r:ASSET_NATION]-(:Asset)
  WHERE r.representative > 0
WITH a, r ORDER BY r.representative DESC
WITH a, head(collect(r)) as prime
SET prime.prime = true
RETURN "SET Nation representative image" AS t
;

MATCH (a:City)<-[r:ASSET_CITY]-(x:Asset)
WITH a, r, x
SET r.representative =
(CASE WHEN x.name CONTAINS a.name THEN 10 ELSE 0 END)
+ (CASE WHEN x.name =~ '(?i)^.*city.*' THEN 6 ELSE 0 END)
+ (CASE WHEN x.name =~ '(?i)^.*town.*' THEN 4 ELSE 0 END)
+ (size([tag IN x.openpipe_tags WHERE (tag='Flags' OR tag='Landscapes' OR tag='Capitals')]) * 2)
+ size([tag IN x.openpipe_tags WHERE (tag='Cities' OR tag='Towns' OR tag="Streets" OR tag="Buildings")])
RETURN "Indentifying City representative images" AS t
;

OPTIONAL MATCH (a:City)<-[r:ASSET_CITY]-(:Asset)
  WHERE r.representative > 0
WITH a, r ORDER BY r.representative DESC
WITH a, head(collect(r)) as prime
SET prime.prime = true
RETURN "SET City representative image" AS t
;

MATCH (a:Tag)<-[r:ASSET_TAG]-(x:Asset)
WITH a, r, x
SET r.representative =
(CASE WHEN x.name CONTAINS a.name THEN 10 ELSE 0 END)
+ (CASE WHEN size(x.openpipe_tags) = 1 THEN 4 ELSE 0 END)
RETURN "Indentifying Tag representative images" AS t
;

OPTIONAL MATCH (a:Tag)<-[r:ASSET_TAG]-(:Asset)
  WHERE r.representative > 0
WITH a, r ORDER BY r.representative DESC
WITH a, head(collect(r)) as prime
SET prime.prime = true
RETURN "SET Tag representative image" AS t
;

OPTIONAL MATCH (a:Topic)<-[r]-(x:Asset)
  WHERE r.prime=true AND exists(x.primaryImageSmall)
SET a.smallImage = x.primaryImageSmall
RETURN "SET representative image to topic.smallImage property" AS t
;

OPTIONAL MATCH (a:Topic)
  WHERE NOT EXISTS(a.smallImage)
OPTIONAL MATCH (a)<-[]-(x:Asset)
WITH a, head(collect(x)) as asset
SET a.smallImage = asset.primaryImageSmall
RETURN "SET a random image to topic.smallImage property where no representative image was found." AS t;

WITH "FULLSYNC COMPLETE" as t RETURN t;
