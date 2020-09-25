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

RETURN "Loaded new Settings - Settings Sync COMPLETE" as t;
