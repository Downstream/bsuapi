//import toastr from './lib/toastr-2.1.4.min.js'

export const logErr = (status, message) => {
    console.log(`${status}: ${message}`)
}

export const uiErr = (status, message) => {
    logErr(status, message)
    //toastr.error(message, 'Graph API Error: '+status)
}

export const err = (message) => {
    if (message.stack) {
        uiErr('Exception', message.stack)
        return
    }

    uiErr('Communication Failure', message)
}