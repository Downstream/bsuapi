CREATE (a:Asset {
  type:"Asset",
  date:"0000-01-01",
  guid: "A",
  name: "A"
})

CREATE (b:Asset {
  type:"Asset",
  date:"0950-01-01",
  guid: "B",
  name: "B",
  moment: "CE950"
})

CREATE (c:Asset {
  type:"Asset",
  date:"2020-01-01",
  guid: "C",
  name: "C"
})

CREATE (f:Folder {
  type: "Folder",
  name: "Folder Name",
  title: "Folder Title",
  guid: "F",
  artCount: 3,
  dateStart: "0000-01-01",
  dateEnd: "2020-01-01"
})

CREATE (a)-[:FOLDER_ASSET]->(f)
CREATE (b)-[:FOLDER_ASSET]->(f)
CREATE (c)-[:FOLDER_ASSET]->(f)
