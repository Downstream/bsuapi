# Experimental Neo4j JSON API Plugin
Intended to be included as a Neo4j plugin, on Ubuntu 18, with Nginx control of access. 

### Latest [bsuapi-1.7.2.jar](releases/bsuapi-1.7.2.jar)
* Access the API [bsuapi/](bsuapi)
* All Releases [releases/](releases)
* [Simple Viewer](viewer) from [Downstream:bsu_viewer.git](https://github.com/Downstream/bsu_viewer)

### Sample GraphDB
* [Graph The MET](pages/Graph-The-MET.md)
* [Cypher](pages/cypher.md) useful queries

### Requirements
* Neo4j 3.5.8

### Reccommend
* Ubuntu 18.04
* openjdk-8-jre
* Nginx 1.16.1
* Neo4j 3.5.8
  * Run as neo4j (only via `systemctl`)
  * Roll your own auth (Not yet implemented here)
  * Only accessible via localhost
  * Nginx Reverse proxy ONLY to the methods intended to be public

Plugin provides a JSON API at http://localhost:7474/bsuapi

# Installation
* Stop the db
* Drop [a jar](releases) into `$NEO4j_HOME/plugins`
* Adjust neo4j.conf
    * `dbms.unmanaged_extension_classes=bsuapi.resource=/bsuapi`
* Start the db
* http://localhost:7474/bsuapi 


# Ubuntu Server Provisioning
* Install openjdk-8-jre, nginx, and neo4j
* Adjust neo4j service def
* Install bsuapi plugin
* Configure neo4j
* Configure nginx
* Start neo4j and nginx
* http://yourdomain.com/bsuapi

### Adjust neo4j service def
Neo4j doesn't yet fully support Ubuntu 18, so an adjustment is needed for the service definition.

comment this line (those ENV vars are [TL;DR:bad])

 `/lib/systemd/system/neo4j.service`
```
# Environment="NEO4J_CONF=/etc/neo4j" "NEO4J_HOME=/var/lib/neo4j"
```

### Install bsuapi plugin
`/var/lib/neo4j/plugins`

### Configure neo4j
`/etc/neo4j/neo4j.conf`
```
dbms.unmanaged_extension_classes=bsuapi.resource=/bsuapi
dbms.active_database=graph.db
dbms.memory.heap.max_size=48G
dbms.connectors.default_listen_address=127.0.0.1
dbms.connectors.default_advertised_address=yourdomain.com
```

### Configure nginx
`/etc/nginx/conf.d/yourdomain.conf`
```nginx
server { 
    
    server_name yourdomain.com
    
    # magic

    location /bsuapi {
        proxy_redirect off;
        proxy_connect_timeout 30s;
        proxy_set_header Accept-Encoding "";
        proxy_set_header Host $host;
        proxy_pass http://127.0.0.1:7474;
    }
}
```

### Start neo4j and nginx
```
sudo systemctl start neo4j
sudo systemctl start nginx 
```

### OpenPipeline Sync
See api home: `methods.execute`

Pulling data from openPipeline is a multi-step process, which includes 2 stages:

* Reset `/execute/OPENPIPE_RESET` - clears entire database, recreates config and indices, and sets sync to pull from assets changed after 2020-01-01.
* Info `/execute/INFO` - regenerates Info Cards. See api home: `methods.info` and `/info`.
* Sync - pulls new assets, folders, and settings, that have changed since the last time Sync was ran. Should be ran in sequence: 
   * `/execute/OPENPIPE_SYNC` - pulls all changed assets, and extrapolates topics and meta-data, creating most of the meta-graph.
   * `/execute/OPENPIPE_FOLDERS` - pulls all folders and their assets, including template positioning where present.
   * `/execute/OPENPIPE_TOPICIMG` - attempts to select a best asset to use as the image representing a topic.
   * `/execute/OPENPIPE_SETTINGS` - pulls all mode settings from openpipe and connects them to their respectively selected folders/topics. Defines the preset options available for each mode.
   
