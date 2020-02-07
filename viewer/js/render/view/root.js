import Element from "../element.js";
import Topic from "../topic.js";

export default class RootView {
    data;
    constructor (apiData) {
        this.data = apiData
    }

    build () {

        let group = Element.columnGroup();
        for (let topicType in this.data.topics) {
            Topic.type(topicType, this.data.topics[topicType]).appendTo(group)
        }

        return [
            Element.title(this.data.title),
            Element.text(this.data.summary),
            group
        ];
    }

    static validData(data) {
        return !!data.topics
    }
}