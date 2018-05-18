'use strict';

var apiBaseUrl = "http://localhost:8080/";

(function () {
    function init() {
        var router = new Router([
            new Route('home', 'home.html', 'home.js', true),
            new Route('disease', 'disease.html'),
            new Route('diseases', 'diseases.html', 'diseases.js'),
            new Route('statistics', 'statistics.html', 'statistics.js'),
            new Route('api', 'api.html'),
            new Route('search', 'search.html', 'search.js')
        ]);
    }
    init();
}());