(function() {
    'use strict';

    angular
        .module('app')
        .service('KlimaatHistorieService', KlimaatHistorieService);

    KlimaatHistorieService.$inject = ['BaseHistorieService'];

    function KlimaatHistorieService(BaseHistorieService) {
        angular.extend(KlimaatHistorieService.prototype, BaseHistorieService);

    }
})();
