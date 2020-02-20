import RootView from "./render/view/root.js";
import RelatedView from "./render/view/related.js";
import ErrorView from "./render/view/error.js";
import {append} from "./util.js";

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

        const root = this.$root
        view.build().forEach((el) => {append(el).to(root)})
    }
}