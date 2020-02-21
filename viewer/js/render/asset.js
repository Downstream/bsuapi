import Element from "./element.js"
import {append, isAppendable} from "../util.js";

export default class Asset {
    data
    el
    assetName
    assetTitle
    modalButton

    constructor (assetData) {
        this.data = assetData

        this.assetName = this.data.objectName
            || this.data.type
            || this.data.classification
            || 'Asset' ;

        this.assetTitle = this.data.title
            || this.assetName ;

        if (this.data.artistDisplayName) {
            this.assetTitle += this.data.artistDisplayName
        }
    }

    static validData(data) {
        return !!data.primaryImageSmall
    }

    static small(assetData) {
        let a = new Asset(assetData).buildEl('asset-small')
        if (a.data.title) {
            Element.label(a.data.title, 'asset-label').appendTo(a.el)
        }

        Element.label(`${a.assetName} - ${a.data.objecDate}` ).appendTo(a.el)
        Element.label(`${a.data.artistDisplayName}` ).appendTo(a.el)
        Element.label(`${a.data.artistDisplayBio}` ).appendTo(a.el)
        Element.label(`${a.data.dimensions}` ).appendTo(a.el)

        return a
    }

    static large(assetData) {
        let a = new Asset(assetData).buildEl('asset-large')
        let info = Element.create('div','assetInfo').appendTo(a.el)
        let table = Element.create('table').appendTo(info)

        Object.entries(a.data).forEach(([key, value]) => {
            let row = Element.create('tr').appendTo(table)
            Element.plain('td', key).appendTo(row)
            Element.plain('td', value).appendTo(row)
        })

        return a
    }

    appendTo(obj) {
        if (this.el && isAppendable(obj)) {
            this.el.appendTo(obj)
        }

        return this
    }

    withModal() {
        if (this.modalButton) {
            this.modalButton.on('click',this.modal.bind(this))
            this.modalButton.addClass('withModal')
        }

        return this
    }

    buildEl(className) {
        this.el = Element.create('div', className)

        if (this.data.primaryImageSmall) {
            this.modalButton = Element.create('div', 'assetImage')
            Element.img(this.data.primaryImageSmall).appendTo(this.modalButton)
            this.modalButton.appendTo(this.el)
        }

        return this
    }

    modal() {
        Element.modal(Asset.large(this.data).el, this.assetName +': '+this.assetTitle).appendTo(this.el)
    }
}