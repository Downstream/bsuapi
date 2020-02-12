package bsuapi.dbal.query;

import bsuapi.dbal.Cypher;
import bsuapi.dbal.CypherException;
import bsuapi.dbal.Node;
import org.apache.commons.lang3.StringUtils;
import org.json.JSONArray;
import org.json.JSONObject;
import org.neo4j.graphdb.Result;

import java.util.Map;
import java.util.regex.Pattern;

public class SearchPredictQuery extends IndexQuery
{
    public SearchPredictQuery(String indexName, String query)
    {
        super(indexName, query);
    }

    protected String cleanCommand(String query)
    {
        if (SearchPredictQuery.alnum.matcher(query).matches()) {
            query += "*";
        } else {
            query = IndexQuery.sanitizeQuery(query);
        }

        // DANGER! injection potential
        return
            "CALL db.index.fulltext.queryNodes(\""+ this.indexName+"\", \""+query+"\") " +
            "YIELD node " +
            "RETURN node.name AS match "
            ;
    }

    @Override
    public JSONArray exec(Cypher c)
    throws CypherException
    {
        try (
            Result r = c.execute(this.getCommand())
        ) {
            while ( r.hasNext()) {
                Map<String,Object> row = r.next();
                for ( Map.Entry<String,Object> column : row.entrySet() ) {
                    Object value = column.getValue();
                    if (column.getKey().equals("match")) {
                        this.addEntry(value);
                        this.resultCount++;
                    }
                }
            }
            return this.results;

        } catch (Exception e) {
            throw new CypherException("Cypher command failed: "+this.toString(), e);
        }
    }

    protected static Pattern alnum = Pattern.compile("[^a-zA-Z0-9]");
}
