import Builder from "./builder.js"
import Ajax from "./ajax.js"
import { err } from "./util.js"

const selector = '#viewer'

export default class Loader {
    static singleInit

    $viewer
    builder
    url
    apiUrl

    constructor(containerSelector) {
        this.$viewer = $(document).find(containerSelector).first()
        this.load();
    }

    buildApiUrl() {
        let url = new URL(window.location.href)
        let path = url.pathname
        url.pathname = path.replace("/viewer/","/bsuapi/")
        return url
    }

    load() {
        err('loading')
        this.url = new URL(window.location.href)
        this.apiUrl = this.buildApiUrl()
        this.builder = new Builder(this.$viewer)
        Ajax.getApiJson(this.apiUrl)
            .then(this.loadApiData.bind(this))
            .catch(err)
        err('loaded')
    }

    loadApiData(data) {
        this.builder.render(data);
    }

    static init() {
        if (this.singleInit instanceof Loader) {
            this.singleInit.load()
        }

        this.singleInit= new Loader(selector);
    }
}