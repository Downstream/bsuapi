MATCH (api:OpenPipeConfig {name: 'api'})

WITH api.folders + '?collectionId=all' as url, api.singleFolder as singleFolderUrl
CALL apoc.load.json(url) YIELD value
UNWIND value.data AS folder

MERGE (f:Folder {openpipe_id:bsuapi.coll.singleClean(folder.id)})
SET f.guid = singleFolderUrl + f.openpipe_id
SET f.title = bsuapi.coll.singleClean(folder.name)
SET f.name = bsuapi.coll.singleClean(folder.name)
SET f.smallImage = bsuapi.coll.singleClean(folder.image)
SET f.layoutType = bsuapi.coll.singleClean(folder.layoutType)
SET f.insertTime = bsuapi.coll.singleClean(folder.insertTime)
SET f.lastModified = bsuapi.coll.singleClean(folder.lastModified)
WITH "Synced Folder Details" as t RETURN t;


MATCH (f:Folder)-[r:FOLDER_ASSET]->(:Asset)
DELETE r
WITH "Cleared assets from updated folders" as t RETURN t;


MATCH (f:Folder)

CALL apoc.load.json(f.guid) YIELD value
UNWIND value.assets as assetEntry
MATCH (a:Asset {guid: assetEntry.guid})
WITH f, a, assetEntry,
     CASE WHEN f.dateStart IS NULL OR f.dateStart > a.date THEN a.date ELSE f.dateStart END AS dateStart,
     CASE WHEN f.dateEnd IS NULL OR f.dateEnd < a.date THEN a.date ELSE f.dateEnd END AS dateEnd
SET f.dateStart = dateStart, f.dateEnd = dateEnd
WITH f, a, assetEntry.geometry as geometry, assetEntry.wall as wall, split(assetEntry.geometry, ' ') as geoSplit
MERGE (f)<-[r:FOLDER_ASSET]-(a)
WITH f, r, geometry, wall, geoSplit
CALL apoc.do.when( geometry IS NOT NULL AND size(geoSplit)>6 ,
"
  SET r.geometry = geometry
  SET r.wall = wall
  SET r.size = [geoSplit[0],geoSplit[2]]
  SET r.position = [geoSplit[3] + geoSplit[4], geoSplit[5] + geoSplit[6]]
  SET f.hasLayout = true
",
"",
{f: f, r: r, geometry: geometry, wall: wall, geoSplit: geoSplit}
) YIELD value
WITH "Synced folders and connected assets and positional data." as t RETURN t;



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

