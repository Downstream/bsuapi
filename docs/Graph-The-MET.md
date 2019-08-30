# The Met Open Access

This tool is intended to work with specialized graph structures for curated art collections.  

Initial graph representations of The Met's Open Access data were key in producing this work,
and would not have possible without The Met's commitment to keeping public works publicly accessible.

To learn more, visit: 
* [Github: metmuseum/openaccess](https://github.com/metmuseum/openaccess)
* [The Met's opan access policies](https://www.metmuseum.org/about-the-met/policies-and-documents/image-resources)

### Prebuilt graph of The Met's data

1. Verify Requirements
    * [APOC](https://github.com/neo4j-contrib/neo4j-apoc-procedures) installed
    * neo4j.conf `apoc.import.file.enabled=true`
2. Download [graphTheMet.gml](data/graphTheMet.gml) to `$NEO4J_HOME/import/graphTheMet.gml`
3. Run the following command via a Neo4j Browser, or cypher-shell. 

```
CALL apoc.import.graphml('exportfile.gml', {batchSize: 10000, readLabels: true, storeNodeIds: false, defaultRelationshipType:'RELATED'});
```

### Artifacts from conversion to graph
Note: Artwork has a special property: `import`
This is used to denote how much was able to be extracted from The Met's API for that artwork. 
* ` -1` ObjectID was recorded in a Department list of objects, but the API did not respond to a request for that object. 
* ` 0` Object details have not yet been requested from The Met API
* ` 1` Detailed information downloaded, but the object is missing an image, or a reliable name for the node.  
* ` 2` Object complete, metamer :Topics (Artist, Culture, Tag, etc.) created from this node, and it has an image. 

Because of this, you may siginificantly speed up queries by deleting all Artwork with `import` < 2:
```
MATCH (x:Artwork)
WHERE x.import < 2
DETATCH DELETE x;
```

# Useful Starting Points

### Visualize the graph
```
CALL apoc.meta.graph()
```

### All properties available on :Artwork
```
CALL apoc.meta.data() YIELD label, property
WHERE label = 'Artwork'
RETURN property
```

### All distinct values of a property
```
MATCH (x:Artwork {import: 2})
RETURN DISTINCT x.classification;
```

### Count occurrences of each property
```
CALL apoc.meta.data() YIELD label, property
WHERE label = 'Artwork'
WITH property
MATCH (x:Artwork {import:2})
RETURN property, count(x[property])
```

### count occurrences of each value in of property
```
MATCH (x:Artwork {import:2})
WHERE x.period IS NOT null
WITH DISTINCT x.period as p, count(x) as c
WHERE c > 2 AND (p CONTAINS 'Edo' OR p CONTAINS 'Qing')
RETURN p, c
ORDER BY c DESC
```