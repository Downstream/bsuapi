# Simplified Documentation
### List of example URIs 
Includes at least one example for each available method.

All examples assume a base URI similar to: `https://bsu-openpipe.downstreamlabs.com/bsuapi`. e.g.: `/settings/globe`
becomes `https://bsu-openpipe.downstreamlabs.com/bsuapi/settings/globe`, to make it a fully functional URL.

* `/` Root - main self-documenting reference.
* `/settings` - lists all configurations and settings.
* `/settings/globe` - retrieves only globe mode settings.
* `/template` - lists 20 folders with template data.
* `/folder` - lists 20 folders
* `/folder?limit=100&page=2` folders 101 through 200 (those params are available everywhere, avoid limits > 1000)
* `/folder/someFolderGUID` - list 20 assets for that folder
* `/folder/someFolderGUID?limit=1000&hasGeo=true` - list the first 1000 assets with geographic position, for that folder.
* `/related/folder/someFolderGUID` - same results as `/folder/someFolderGuid`.
* `/template/someFolderGUID` - lists all assets for that folder, with positional details, excludes assets without positional data.
* `/timeline/folder/someFolderGUID` - assets in that folder, grouped into discrete time blocks.
* `/related/artist/someArtistGUID` - 20 assets, and 20 of each topic-type, for that artist.
* `/timeline/artist/someArtistGUID` - assets for that artist, grouped into discrete time blocks.
* `/topic-assets/artist/someArtistGUID` - 20 assets for that artist.
* `/related/artist/someArtistGUID?limit=100` - 100 assets, and 100 of each topic-type, for that artist.
* `/related/culture/someCultureGUID` - 20 assets, and 20 of each topic-type, for that culture.
* `/timeline/culture/someCultureGUID` - assets for that culture, grouped into discrete time blocks.
* `/related/culture/someCultureGUID?hasGeo=true` - 20 assets, and 20 of each topic-type, for that culture, which have geographic data.
* `/related/topic/anyGUID` - should be possible, but not yet implemented.
* `/timeline/topic/anyGUID` - should be possible, but not yet implemented.
* `/search/rome` - best matches (by guid or title) topics and assets.
* `/search-completion/rome` - best matches (by guid or title) topics and assets, but only returns the matched string (much faster, but no data, for search-completion).
* `/execute/someScriptName` - retrieve details about last, or current, execution.
* `/execute/someScriptName/start` - starts execution of that script.
* `/sync_status` - details of the configuration and last run of the OpenPipe sync.
 

