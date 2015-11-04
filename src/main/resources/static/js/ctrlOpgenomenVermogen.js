'use strict';

angular.module('appHomecontrol.opgenomenVermogenService', [])

    .service("RealTimeOpgenomenVermogenService", ['$q', '$timeout', '$log', function($q, $timeout, $log) {
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
    }]);

angular.module('appHomecontrol.opgenomenVermogenController', [])

    .controller('OpgenomenVermogenController', ['$scope', '$http', 'RealTimeOpgenomenVermogenService', function($scope, $http, RealTimeOpgenomenVermogenService) {
        // Turn off all leds
        for (var i = 0; i < 10; i++) {
            $scope['led'+i] = false;
        }

        $scope.huidigOpgenomenVermogen = '0';

        $http.get('rest/meterstanden/laatste')
            .success(function(data) {
                updateOpgenomenVermogen(data);
            }
        );

        RealTimeOpgenomenVermogenService.receive().then(null, null, function(jsonData) {
            updateOpgenomenVermogen(jsonData);
        });

        function updateOpgenomenVermogen(data) {
            var huidigOpgenomenVermogen = data.stroomOpgenomenVermogenInWatt;
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
        }
    }]);

