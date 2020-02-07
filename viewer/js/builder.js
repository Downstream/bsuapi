import RootView from "./render/view/root.js";
import RelatedView from "./render/view/related.js";
import ErrorView from "./render/view/error.js";

export default class Builder {
    $root;

    constructor(root) {
        this.$root = $(root)
    }

    render(data) {
        let view = new ErrorView();

        if (RootView.validData(data)) {
            view = new RootView(data);
        } else if (RelatedView.validData(data)) {
            view = new RelatedView(data)
        }

        view.build().forEach(this.addElement.bind(this))
    }

    addElement(element) {
        if (element instanceof jQuery && element[0] instanceof HTMLElement) {
            element.appendTo(this.$root)
        } else if (element instanceof HTMLElement) {
            this.$root.appendChild(element)
        } else {
            console.log(element);
            throw TypeError(`Builder addElement of type ${typeof element} is not an HTML element`)
        }
    }
}