## Useful Cypher Queries

### Search a specific Topic
For a specific Topic type (Artist), find all nodes containing a string (Monet).

```
MATCH (a:Artist)
WHERE a.name contains "Degas"
RETURN a.name, a.artCount 
ORDER BY a.artCount DESC
```

### Find most related Topics
For a specific Topic (Artist: Edgar Degas), and a target Topic type (Tag), 
list related Topic by most shared Topics.

e.g.: This returns 10 tags assigend to Degas' work, which share the most connected Topics with Degas.
```
MATCH (a:Artist {name:"Edgar Degas"})
MATCH (a)-[r:TAG]->(t:Tag)
MATCH p=(a)-[]->(:Topic)-[:TAG]->(t)
WITH a, t, count(p) as n
RETURN a, t, n
ORDER BY n DESC
LIMIT 10;
```

### Find most related of the same Topic
For a specific Topic (Artist: Edgar Degas), find the most related of the same topic (Artist).

e.g.: This returns 10 artists which share the most connected Topics with Degas.
```
MATCH (a:Artist {name:"Edgar Degas"})
MATCH p=(a)-[]->(:Topic)-[:ARTIST]->(b:Artist)
WHERE b <> a
WITH a, b, count(p) as n
RETURN a, b, n
ORDER BY n DESC
LIMIT 10;
```