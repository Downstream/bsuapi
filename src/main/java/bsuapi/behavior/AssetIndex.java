package bsuapi.behavior;

import bsuapi.dbal.query.IndexQuery;

import java.util.Map;

public class AssetIndex extends IndexBehaviorBase
{
    public static final String indexName = "assetNameIndex";

    public AssetIndex(Map<String, String> config)
    throws BehaviorException
    {
        super(config);
    }

    @Override
    public String resultKey() {
        return "asset-results";
    }

    @Override
    public IndexQuery createQuery(String value) {
        return new IndexQuery(AssetIndex.indexName, value);
    }
}
