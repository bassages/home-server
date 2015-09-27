var app = angular.module('appHomecontrol', []);

app.controller('AfvalController', function ($scope, $http) {
    $http.get('rest/afvalinzameling/volgende/').success(function(data) {
        if (data) {
            var afvaltypes = [];
            for (var i=0; i<data.afvalTypes.length; i++) {
                afvaltypes.push({
                    'type': data.afvalTypes[i],
                    'omschrijving': getAfvalIconTitel(data.afvalTypes[i])});
            }
            $scope.afvalinzamelingdatum = formatDate(new Date(data.datum));
            $scope.separator = ": ";
            $scope.afvaltypes = afvaltypes;
        } else {
            $scope.separator = "Kon niet worden bepaald door een technische fout";
        }
    })
});

app.controller("OpgenomenVermogenController", function($scope, RealTimeOpgenomenVermogenService) {
    RealTimeOpgenomenVermogenService.receive().then(null, null, function(jsonData) {
        $scope.huidigOpgenomenVermogen = jsonData.opgenomenVermogenInWatt;
    });
});

app.service("RealTimeOpgenomenVermogenService", function($q, $timeout) {
    var service = {}, listener = $q.defer(), socket = {
        client: null,
        stomp: null
    };

    service.RECONNECT_TIMEOUT = 30000;
    service.SOCKET_URL = "/homecontrol/ws/elektra";
    service.UPDATE_TOPIC = "/topic/elektriciteit/opgenomenVermogen";

    service.receive = function() {
        return listener.promise;
    };

    var reconnect = function() {
        $timeout(function() {
            initialize();
        }, this.RECONNECT_TIMEOUT);
    };

    var startListener = function() {
        socket.stomp.subscribe(service.UPDATE_TOPIC, function(data) {
            listener.notify(JSON.parse(data.body));
        });
    };

    var initialize = function() {
        socket.client = new SockJS(service.SOCKET_URL);
        socket.stomp = Stomp.over(socket.client);
        socket.stomp.connect({}, startListener);
        socket.stomp.onclose = reconnect;
    };

    initialize();
    return service;
});

function formatDate(dateToFormat) {
    return weekday[dateToFormat.getDay()] + " " + pad2(dateToFormat.getDate()) + "-" + pad2(dateToFormat.getMonth()+1) + "-" + pad2(dateToFormat.getFullYear());
}

function getAfvalIconTitel(afvalcode) {
    if (afvalcode == "REST") {
        return "Grijze container";
    } else if (afvalcode == "GFT") {
        return "Groene container";
    } else if (afvalcode == "SALLCON") {
        return "Papier, glas, blik, drankkarton";
    } else if (afvalcode == "PLASTIC") {
        return "Oranje container";
    }
}

function pad2(number) {
    return (number < 10 ? '0' : '') + number;
}

var weekday = new Array(7);
weekday[0]=  "zondag";
weekday[1] = "maandag";
weekday[2] = "dinsdag";
weekday[3] = "woensdag";
weekday[4] = "donderdag";
weekday[5] = "vrijdag";
weekday[6] = "zaterdag";
