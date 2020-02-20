//import toastr from './lib/toastr-2.1.4.min.js'

import Asset from "./render/asset.js";

export const logErr = (status, message) => {
    console.log(`${status}: ${message}`)
};

export const uiErr = (status, message) => {
    logErr(status, message)
    //toastr.error(message, 'Graph API Error: '+status)
};

export const err = (message) => {
    if (message.stack) {
        uiErr('Exception', message.stack);
        return
    }

    uiErr('Communication Failure', message)
};

export const isJquery = (element) => {
    return element instanceof jQuery && element[0] instanceof HTMLElement
}

export const isHtml = (element) => {
    return element instanceof HTMLElement
}

export const isString = (element) => {
    return typeof element === 'string' || element instanceof String
}

export const isAppendable = (element) => {
    return isJquery(element) || isHtml(element) || isString(element) || element instanceof Asset
}

export const append = (child) => {
    if (isJquery(child) || child instanceof Asset) {
        return {to: (parent) => { child.appendTo(parent) }}
    }

    let toF = (parent) => {
        if (isJquery(parent)) {
            parent.append(child)
        } else if (isHtml(parent)) {
            parent.appendChild(child)
        }
    }

    if (isHtml(child)) {
        // nothing to do here
    } else if (isString(child)) {
        child = document.createTextNode(child)
    } else {
        toF = (parent) => {
            throw TypeError(`Viewer.Util.append Exception: expected child to be String, HTMLElement, or JqueryElement.  Found unsupported type: ${typeof child}.`)
        }
    }

    return {
        to: toF
    }
}