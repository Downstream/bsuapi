MERGE (x:OpenPipeConfig {name: 'api'})
SET x.canonical='http://mec402.boisestate.edu/cgi-bin/dataAccess/getCanonicalMetaTags.py'
SET x.allAssets='http://mec402.boisestate.edu/cgi-bin/dataAccess/getAllAssetsWithGUID.py'
SET x.changedAssets='http://mec402.boisestate.edu/cgi-bin/dataAccess/getAllAssetsWithGUID.py'
SET x.folders='http://mec402.boisestate.edu/cgi-bin/dataAccess/getCollections.py'
SET x.singleAsset='http://mec402.boisestate.edu/cgi-bin/openpipe/data/asset/'
SET x.singleFolder='http://mec402.boisestate.edu/cgi-bin/openpipe/data/folder/'
SET x.settings='http://mec402.boisestate.edu/cgi-bin/dataAccess/settings/getWallAppSettings.py'
SET x.lastRun = '2020-01-01'
SET x.lastFolderRun = '2020-01-01'
SET x.assetsPerPage = '100'
;
