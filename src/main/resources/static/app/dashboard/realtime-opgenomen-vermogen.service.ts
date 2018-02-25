(function() {
    'use strict';

    angular
        .module('app')
        .service('RealtimeOpgenomenVermogenService', RealtimeOpgenomenVermogenService);

    RealtimeOpgenomenVermogenService.$inject = ['$q', '$timeout', '$log'];

    function RealtimeOpgenomenVermogenService($q, $timeout, $log) {
        var service:any = {};
        var listener = $q.defer();

        var socket = { client: null, stomp: null };

        service.RECONNECT_TIMEOUT = 10000;
        service.SOCKET_URL = "/ws/opgenomen-vermogen";
        service.UPDATE_TOPIC = "/topic/opgenomen-vermogen";

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
                listener.notify(null);
                reconnect();
            };
        };

        connect();
        return service;
    }
})();
