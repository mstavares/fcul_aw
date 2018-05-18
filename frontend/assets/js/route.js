'use stict';

function Route(name, htmlName, jsName, defaultRoute) {
    try {
        if(!name || !htmlName) {
            throw 'error: name and htmlName params are mandatories';
        }
        this.constructor(name, htmlName, jsName, defaultRoute);
    } catch (e) {
        console.error(e);
    }
}

Route.prototype = {
    name: undefined,
    htmlName: undefined,
    jsName: undefined,
    default: undefined,
    constructor: function (name, htmlName, jsName, defaultRoute) {
        this.name = name;
        this.htmlName = htmlName;
        this.jsName = jsName;
        this.default = defaultRoute;
    },
    isActiveRoute: function (hashedPath) {
        return hashedPath.split('=')[0].replace('#', '') === this.name; 
    }
}