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
RETURN "Synced Folder Details" as t LIMIT 1;


MATCH (api:OpenPipeConfig {name: 'api'}) WITH api.lastFolderRun as lastRunDate
MATCH (f:Folder)-[r:FOLDER_ASSET]->(:Asset)
WHERE f.lastModified > lastRunDate
DELETE r
RETURN "Cleared assets from updated folders" as t LIMIT 1;


MATCH (api:OpenPipeConfig {name: 'api'}) WITH api.lastFolderRun as lastRunDate
MATCH (f:Folder)
WHERE f.lastModified > lastRunDate

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
RETURN value






MATCH (api:OpenPipeConfig {name: 'api'})
SET api.lastFolderRun = date()
RETURN "Folder Sync COMPLETE" as t LIMIT 1;

