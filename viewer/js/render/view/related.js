import Element from "../element.js";
import Topic from "../topic.js";
import Asset from "../asset.js";

export default class RelatedView {
    data
    constructor (apiData) {
        this.data = apiData.data
    }

    build () {
        return [
            this.buildOverview(),
            this.buildAssets(),
            this.buildRelated(),
        ];
    }

    buildOverview() {
        let overview = Element.create('div', 'topic-card')
        if (this.data.node.smallImage) {
            Element.img(this.data.node.smallImage).appendTo(overview)
        }
        Element.title(this.data.node.name).appendTo(overview)
        Element.text(this.data.node.type).appendTo(overview)

        return overview
    }

    buildAssets() {
        let assets = Element.create('div', 'topic-assets')
        Element.subtitle('Assets').appendTo(assets)
        let group = Element.create('div', 'gallery').appendTo(assets)

        for (let assetKey in this.data.assets) {
            let assetData = this.data.assets[assetKey]
            if (Asset.validData(assetData)) {
                Asset.small(assetData).appendTo(group)
            }
        }

        return assets
    }

    buildRelated() {
        let related = Element.create('div', 'topic-related')
        Element.subtitle('Related Topics').appendTo(related)
        let group = Element.columnGroup().appendTo(related)
        for (let topicType in this.data.related) {
            Topic.type(topicType, this.data.related[topicType]).appendTo(group)
        }

        return related
    }

    static validData(data) {
        return (
               data.data
            && data.data.node
            && data.data.related
        )
    }
}