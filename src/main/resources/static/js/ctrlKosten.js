'use strict';

angular.module('appHomecontrol.kostenService', [])

    .factory('Kosten', function($resource) {
        return $resource('/homecontrol/rest/kosten/:id');
    });

angular.module('appHomecontrol.kostenController', [])

    .controller('KostenController', ['$scope', '$resource', 'Kosten', function($scope, $resource, Kosten) {
        $scope.kosten = Kosten.query(function() {});
    }]);
