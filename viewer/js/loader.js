import Builder from "./builder.js"
import Ajax from "./ajax.js"
import { err } from "./util.js"
import Element from "./render/element.js"

const selector = '#viewer'

export default class Loader {
    static singleInit
    static viewerApi

    $viewer
    builder
    url
    apiUrl

    constructor(containerSelector) {
        this.$viewer = $(document).find(containerSelector).first()
        Loader.viewerApi = this.$viewer.data('api')
        Loader.viewerApi = (Loader.viewerApi) ? Loader.viewerApi : 'bsu.downstreamlabs.com'
        this.load()
    }

    buildApiUrl() {
        /*
            href: "http://boise-viewer.local/viewer/index.html"
            origin: "http://boise-viewer.local"
            protocol: "http:"
            username: ""
            password: ""
            host: "boise-viewer.local"
            hostname: "boise-viewer.local"
            port: ""
            pathname: "/viewer/index.html"
         */
        let url = new URL(window.location.href)
        url.host=Loader.viewerApi
        url.protocol='https'
        url.pathname = url.pathname.replace('/viewer/','/bsuapi/')
        return url
    }

    static urlApiToViewer(apiUrl) {
        let url = new URL(window.location.href)
        let api = new URL(apiUrl)
        url.pathname = api.pathname.replace('/bsuapi/','/viewer/')
        return url
    }

    load() {
        this.url = new URL(window.location.href)
        this.apiUrl = this.buildApiUrl()
        this.builder = new Builder(this.$viewer)

        this.addNavRawdataLink()

        Ajax.getApiJson(this.apiUrl)
            .then(this.loadApiData.bind(this))
            .catch(err)
    }

    addNavRawdataLink() {
        $('ul.navbar-nav').first().append(
            Element.navItem(this.apiUrl.href, 'code', 'Source Data')
        );
    }

    loadApiData(data) {
        this.builder.render(data)
    }

    static init() {
        if (this.singleInit instanceof Loader) {
            this.singleInit.load()
        }

        this.singleInit= new Loader(selector)
    }
}