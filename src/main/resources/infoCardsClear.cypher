MATCH (info:Info)
DETACH DELETE info
RETURN "DELETED " + count(info) + " :Info nodes." as t
