package bsuapi.behavior;

import bsuapi.dbal.query.IndexQuery;

import java.util.Map;

public class FolderIndex extends IndexBehaviorBase
{
    public static final String indexName = "folderNameIndex";

    public FolderIndex(Map<String, String> config)
    throws BehaviorException
    {
        super(config);
    }

    @Override
    public String resultKey() {
        return "folder-results";
    }

    @Override
    public IndexQuery createQuery(String value) {
        return new IndexQuery(FolderIndex.indexName, value);
    }
}
