import Element from "./element.js"

export default class Asset {
    data
    constructor (assetData) {
        this.data = assetData
    }

    static validData(data) {
        return !!data.primaryImageSmall
    }

    static small(assetData) {
        return (new Asset(assetData)).build()
    }

    build() {
        let el = Element.create('div', 'asset-small')
        Element.img(this.data.primaryImageSmall).appendTo(el)

        if (this.data.title) {
            Element.label(this.data.title, 'asset-label').appendTo(el)
        }

        const assetName =  this.data.objectName     ? this.data.objectName
                        :( this.data.type           ? this.data.type
                        :( this.data.classification ? this.data.classification
                        : 'Asset'
                )); // null coalesce

        Element.text(`${assetName} - ${this.data.objecDate}` ).appendTo(el)
        Element.text(`${this.data.artistDisplayName}` ).appendTo(el)
        Element.text(`${this.data.artistDisplayBio}` ).appendTo(el)
        Element.text(`${this.data.dimensions}` ).appendTo(el)

        return el
    }
}