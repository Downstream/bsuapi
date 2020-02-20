import {isAppendable, append} from "../util.js";

export default class Element {
    static plain(type, content, classname) {
        let el = $(`<${type}>${content}</{$type}>`);
        if (classname) {
            el.addClass(classname)
        }

        return el
    }

    static create(type, classname) {
        return Element.plain(type, '', classname)
    }

    static link(action, content, classname) {
        let link = Element.create('a',classname)

        if (typeof action === 'string' || action instanceof String) {
            link.attr('href',action)
        } else if (typeof action === "function")  {
            link.on('click', action)
        }

        if (isAppendable(content)) {
            append(content).to(link)
        }

        return link
    }

    static button(action, icon, text) {
        let but = Element.link(action,null, 'button')

        if (icon) {
            Element.icon(icon).appendTo(but)
        }

        if (text) {
            but.append(' ' + text)
        }

        return but
    }

    static icon(type) {
        return Element.create('i', 'fa fa-'+type)
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

    static modal(content, title) {
        let box = Element.create('div','modal');
        Element.button(()=>{ box.remove() }).addClass('modalClose').appendTo(box)

        if (title) {
            Element.plain('h4', title).appendTo(box)
        }

        if (isAppendable(content)) {
            append(content).to(box)
        }

        return box
    }
}