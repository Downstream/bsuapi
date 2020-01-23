import { err } from "./util.js"

let headers = new Headers();
headers.append('Authorization', 'Basic '+ btoa('neo4j:d0wnstr34m'));

const fetchOpts = {
    headers: headers
}

export default class Ajax {
    constructor ($root) {
        this.$root = $root
        this.url = $root.data('data-ajax-url')
    }

    static async getApiJson(url) {
        const response = await fetch(url, fetchOpts)
            .catch(err);
        return await response.json()
            .catch(err);
    }
}
