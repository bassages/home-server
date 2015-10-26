var module = angular.module('appHomecontrol', ['ngRoute', 'ngAnimate']);

module.config(function($routeProvider) {
    $routeProvider

    // route for the home page
    .when('/', {
        templateUrl : 'dashboard.html'
    })

    // route for the about page
    .when('/grafieken/:type', {
        templateUrl : 'grafiek.html'
    });
});

module.directive('formatteddate', function() {
    return {
        restrict: 'A',
        require: 'ngModel',
        link: function(scope, element, attr, ngModel) {
            function fromUser(text) {
                return Date.parse(text);
            }
            function toUser(date) {
                return formatDate(date);
            }
            ngModel.$parsers.push(fromUser);
            ngModel.$formatters.push(toUser);
        }
    };
});

module.controller('AfvalController', function ($scope, $http) {
    $http.get('rest/afvalinzameling/volgende/').success(function(data) {
        if (data) {
            var afvaltypes = [];
            for (var i=0; i<data.afvalTypes.length; i++) {
                afvaltypes.push({
                    'type': data.afvalTypes[i],
                    'omschrijving': getAfvalIconTitel(data.afvalTypes[i])});
            }
            $scope.afvalinzamelingdatum = formatDateWithdayname(new Date(data.datum));
            $scope.separator = ": ";
            $scope.afvaltypes = afvaltypes;
        } else {
            $scope.separator = "Kon niet worden bepaald door een technische fout";
        }
    })
});

module.controller("OpgenomenVermogenController", function($scope, $timeout, RealTimeOpgenomenVermogenService) {

    // Turn off all leds
    for (i = 0; i < 10; i++) {
        $scope['led'+i] = false;
    }

    $scope.huidigOpgenomenVermogen = '0';

    RealTimeOpgenomenVermogenService.receive().then(null, null, function(jsonData) {
        var huidigOpgenomenVermogen = jsonData.opgenomenVermogenInWatt;
        $scope.huidigOpgenomenVermogen = huidigOpgenomenVermogen;

        var step = 150;

        $scope.led0 = huidigOpgenomenVermogen > 0;
        $scope.led1 = huidigOpgenomenVermogen >= (1 * step);
        $scope.led2 = huidigOpgenomenVermogen >= (2 * step);
        $scope.led3 = huidigOpgenomenVermogen >= (3 * step);
        $scope.led4 = huidigOpgenomenVermogen >= (4 * step);
        $scope.led5 = huidigOpgenomenVermogen >= (5 * step);
        $scope.led6 = huidigOpgenomenVermogen >= (6 * step);
        $scope.led7 = huidigOpgenomenVermogen >= (7 * step);
        $scope.led8 = huidigOpgenomenVermogen >= (8 * step);
        $scope.led9 = huidigOpgenomenVermogen >= (9 * step);
    });
});

module.controller("GrafiekController", function ($scope, $timeout, $http, $log) {
    $scope.chart = null;
    $scope.config={};

    // By default, today is selected
    $scope.from = new Date();
    $scope.from.setHours(0,0,0,0);

    $scope.to = new Date($scope.from);
    $scope.to.setDate($scope.from.getDate() + 1);

    $('.datepicker').datepicker({
        autoclose: true,
        todayBtn: "linked",
        calendarWeeks: true,
        todayHighlight: true
    }).on('changeDate', function(e) {
        $scope.$apply(function() {
            $scope.from = new Date(e.date);
            $scope.showGraph();
        });
    });

    $scope.previousDay = function() {
        var previous = new Date($scope.from);
        previous.setDate($scope.from.getDate() - 1);
        $scope.from = previous;
        $scope.showGraph();
    };

    $scope.nextDay = function() {
        var next = new Date($scope.from);
        next.setDate($scope.from.getDate() + 1);
        $scope.from = next;
        $scope.showGraph();
    };

    $scope.dateChanged = function() {
        log.info("Date changed");
        $scope.showGraph();
    };

    $scope.showGraph = function() {
        $scope.to = new Date($scope.from);
        $scope.to.setDate($scope.from.getDate() + 1);

        $log.info('From: ' + $scope.from + ' To: ' + $scope.to);

        var numberOfHoursInDay = (($scope.to-$scope.from)/1000)/60/60;
        $log.info('numberOfHoursInDay: ' + numberOfHoursInDay);

        var tickValues = [];
        for (var i=0; i<= numberOfHoursInDay; i++) {
            var tickValue = $scope.from.getTime() + (i * 60 * 60 * 1000);
            tickValues.push(tickValue);
        }

        $http.get('rest/elektriciteit/opgenomenVermogenHistorie/' + $scope.from.getTime() + "/" + $scope.to.getTime() ).success(function(data) {
            if (data) {
                for (var i=0; i<data.length; i++) {
                    //data[i].datumtijd = data[i].datumtijd + 450000;
                }

            } else {

            }
            var graphConfig = {};
            graphConfig.bindto = '#chart';

            graphConfig.data = {};
            graphConfig.data.keys = {x: "datumtijd", value: ["opgenomenVermogenInWatt"]};
            graphConfig.data.json = data;
            graphConfig.data.types={"opgenomenVermogenInWatt": "area-step"};
            graphConfig.data.empty = {label: {text: "Gegevens worden opgehaald..."}};

            graphConfig.axis = {};
            graphConfig.axis.x = {type: "timeseries", tick: {format: "%H:%M", values: tickValues, rotate: -90}, min: $scope.from, max: $scope.to, padding: {left: 0, right:20}};
            graphConfig.axis.y = {label: {text: "Watt", position: "outer-middle"}};

            graphConfig.legend = {show: false};
            graphConfig.bar = {width: {ratio: 1}};
            graphConfig.point = { show: false};
            graphConfig.transition = { duration: 0};
            graphConfig.grid = {y: {show: true}};
            graphConfig.tooltip = {show: true};
            graphConfig.padding = {top: 0, right: 50, bottom: 40, left: 50};

            $scope.chart = c3.generate(graphConfig);
        });
    }
});

module.service("RealTimeOpgenomenVermogenService", function($q, $timeout, $log) {
    var service = {};
    var listener = $q.defer();
    var socket = {
        client: null,
        stomp: null
    };

    service.RECONNECT_TIMEOUT = 10000;
    service.SOCKET_URL = "/homecontrol/ws/elektra";
    service.UPDATE_TOPIC = "/topic/elektriciteit/opgenomenVermogen";

    service.receive = function() {
        return listener.promise;
    };

    var reconnect = function() {
        $log.info("Trying to reconnect in " + service.RECONNECT_TIMEOUT + " ms.");
        $timeout(connect, service.RECONNECT_TIMEOUT);
    };

    var startListener = function() {
        socket.stomp.subscribe(service.UPDATE_TOPIC, function(data) {
            listener.notify(JSON.parse(data.body));
        });
    };

    var connect = function() {
        socket.client = new SockJS(service.SOCKET_URL);
        socket.stomp = Stomp.over(socket.client);
        socket.stomp.connect({}, startListener);

        socket.client.onclose = function() {
            reconnect();
        };
    };

    connect();
    return service;
});

// Returns a formatted date. Example: zondag 21-10-2015
function formatDateWithdayname(dateToFormat) {
    return weekday[dateToFormat.getDay()] + " " + pad2(dateToFormat.getDate()) + "-" + pad2(dateToFormat.getMonth()+1) + "-" + pad2(dateToFormat.getFullYear());
}

// Returns a formatted date. Example: 21-10-2015
function formatDate(dateToFormat) {
    return pad2(dateToFormat.getDate()) + "-" + pad2(dateToFormat.getMonth()+1) + "-" + pad2(dateToFormat.getFullYear());
}

function getAfvalIconTitel(afvalcode) {
    if (afvalcode == "REST") {
        return "Grijze container";
    } else if (afvalcode == "GFT") {
        return "Groene container";
    } else if (afvalcode == "SALLCON") {
        return "Papier, glas";
    } else if (afvalcode == "PLASTIC") {
        return "Oranje container (PMD)";
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
