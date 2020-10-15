package bsuapi.dbal;

public class Topic extends Element
{
    public static final String labelParam = "topicLabel";
    public static final String keyParam = "topicKey";

    public Topic(String labelName, String nodeKey)
    {
        super(NodeType.match(labelName), nodeKey);
    }
}
