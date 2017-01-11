(function() {
    'use strict';

    angular
        .module('app')
        .controller('BedienController', BedienController);

    BedienController.$inject = ['$log', '$http', '$sce'];

    function BedienController($log, $http, $sce) {
        var vm = this;

        activate();

        function activate() {
            var bedienAppUrl = 'http://raspberrypi:5001';
            vm.bedienAppUrl = $sce.trustAsResourceUrl(bedienAppUrl);
        }
    }
})();
