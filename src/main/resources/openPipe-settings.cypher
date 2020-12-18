MATCH (group:OpenPipeSetting) DETACH DELETE group
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
WITH group, entry, split(entry, ' ') as splitEntry
WITH group, entry, splitEntry[0] as splitGuid, splitEntry[2] as splitByType
CALL apoc.do.case([
  splitByType IS NOT NULL AND size(splitByType)>0 AND splitGuid CONTAINS "/folder/", "
    	MATCH (a:Folder {guid: splitGuid})
      MERGE (group)<-[:SETTING_OPTION {byType: splitByType}]-(a)
      RETURN 1 as cnt
    ",
  splitByType IS NOT NULL AND size(splitByType)>0 AND splitGuid CONTAINS "/openpipe/", "
    	MATCH (a:Topic {guid: splitGuid})
      MERGE (group)<-[:SETTING_OPTION {byType: splitByType}]-(a)
      RETURN 1 as cnt
    ",
  entry CONTAINS "/folder/", "
    	MATCH (a:Folder {guid: entry})
      MERGE (group)<-[:SETTING_OPTION]-(a)
      RETURN 1 as cnt
    ",
  entry CONTAINS "/openpipe/", "
    	MATCH (a:Topic {guid: entry})
      MERGE (group)<-[:SETTING_OPTION]-(a)
      RETURN 1 as cnt
    "
],
"RETURN count(c) AS message",
{group: group, entry: entry, splitGuid: splitGuid, splitByType: splitByType}
) YIELD value
RETURN "Mapped topic/folders " + sum(value.c) + " options from openpipe settings" as t;
