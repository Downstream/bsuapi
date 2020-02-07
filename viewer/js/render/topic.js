import Element from "./element.js"

export default class Topic {
    data;
    constructor (topicData) {
        this.data = topicData
    }

    static validData(data) {
        return (
               data.type
            && data.smallImage
            && data.name
            && data.linkRelated
        )
    }

    // grouping of topics: Artist, Classification, Nation, etc.
    static type(label, data) {
        let column = Element.column('topic-column');
        Element.plain('h4',label).appendTo(column);
        data.forEach((t) => {
            Topic.small(t).appendTo(column)
        });

        return column
    }

    // single Topic: nation:France, artist:Thomas+Rowlandson, etc.
    static small(topicData) {
        return (new Topic(topicData)).build()
    }

    build() {
        let el = Element.create('div', 'topic');
        let urlRelated = Topic.urlApiToViewer(this.data.linkRelated);

        Element.label(this.data.name, 'topic-label').appendTo(el);
        Element.img(this.data.smallImage)
            .attr('onerror', 'this.parentElement.parentElement.style.display = \'none\';')
            .appendTo(el);

        return Element.link(urlRelated, el, 'w-100')
    }

    static urlApiToViewer(apiUrl) {
        return apiUrl.replace('/bsuapi/','/viewer/')
    }
}