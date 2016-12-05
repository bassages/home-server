(function() {
    'use strict';

    angular
        .module('app')
        .controller('SchakelaarsController', SchakelaarsController);

    SchakelaarsController.$inject = ['$log', '$http', '$sce'];

    function SchakelaarsController($log, $http, $sce) {
        var vm = this;

        activate();

        function activate() {
            var schakelaarsAppUrl = 'http://raspberrypi:5001';
            vm.schakelaarsAppUrl = $sce.trustAsResourceUrl(schakelaarsAppUrl);
        }
    }
})();
