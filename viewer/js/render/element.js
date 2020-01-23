export default class Element {
    static plain(type, content, classname) {
        let el = $(`<${type}>${content}</{$type}>`)
        if (classname) {
            el.addClass(classname)
        }

        return el
    }

    static create(type, classname) {
        return Element.plain(type, '', classname)
    }

    static link(url, content, classname) {
        let link;

        if (content instanceof jQuery && content[0] instanceof HTMLElement) {
            link = Element.create('a',classname)
            link.attr('href',url)
            content.appendTo(link)
            return link
        }

        link = Element.plain('a',content, classname)
        link.attr('href',url)
        return link
    }

    static label(str, classname) {
        return Element.plain('span', str, classname)
    }

    static text(str) {
        return Element.plain('p',str)
    }

    static subtitle(str) {
        return Element.plain('h3',str)
    }

    static title(str) {
        return Element.plain('h2',str)
    }

    static img(src) {
        return Element.create('img').attr('src', src)
    }

    static columnGroup() {
        return Element.create('div','d-flex')
    }

    static column(classname) {
        return Element.create('div', 'flex-fill '+ classname)
    }
}