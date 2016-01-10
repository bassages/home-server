'use strict';

angular.module('appHomecontrol.meterstandService', [])

    .service("RealTimeMeterstandService", ['$q', '$timeout', '$log', function($q, $timeout, $log) {
        var service = {};
        var listener = $q.defer();
        var socket = {
            client: null,
            stomp: null
        };

        service.RECONNECT_TIMEOUT = 10000;
        service.SOCKET_URL = "/homecontrol/ws/meterstand";
        service.UPDATE_TOPIC = "/topic/meterstand";

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
    }]);

angular.module('appHomecontrol.stroomMeterstandController', [])

    .controller('StroomMeterstandController', ['$scope', '$http', 'RealTimeMeterstandService', function($scope, $http, RealTimeMeterstandService) {
        // Turn off all leds
        for (var i = 0; i < 10; i++) {
            $scope['led'+i] = false;
        }

        $scope.huidigOpgenomenVermogen = '0';

        $http.get('rest/meterstanden/meestrecente')
            .success(function(data) {
                update(data);
            }
        );

        RealTimeMeterstandService.receive().then(null, null, function(jsonData) {
            update(jsonData);
        });

        function update(data) {
            $scope.t1 = data.stroomTarief1;
            $scope.t2 = data.stroomTarief2;
            $scope.huidigOpgenomenVermogen = data.stroomOpgenomenVermogenInWatt;

            var step = 150;
            $scope.led0 = data.stroomOpgenomenVermogenInWatt > 0;
            $scope.led1 = data.stroomOpgenomenVermogenInWatt >= (1 * step);
            $scope.led2 = data.stroomOpgenomenVermogenInWatt >= (2 * step);
            $scope.led3 = data.stroomOpgenomenVermogenInWatt >= (3 * step);
            $scope.led4 = data.stroomOpgenomenVermogenInWatt >= (4 * step);
            $scope.led5 = data.stroomOpgenomenVermogenInWatt >= (5 * step);
            $scope.led6 = data.stroomOpgenomenVermogenInWatt >= (6 * step);
            $scope.led7 = data.stroomOpgenomenVermogenInWatt >= (7 * step);
            $scope.led8 = data.stroomOpgenomenVermogenInWatt >= (8 * step);
            $scope.led9 = data.stroomOpgenomenVermogenInWatt >= (9 * step);
        }
    }]);

