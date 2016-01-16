'use strict';

angular.module('appHomecontrol.kostenService', [])

    .factory('Kosten', function($resource) {
        return $resource('/homecontrol/rest/kosten/:id');
    });

angular.module('appHomecontrol.kostenController', [])

    .controller('KostenController', ['$scope', '$resource', 'Kosten', function($scope, $resource, Kosten) {
        $scope.kosten = Kosten.query(function() {});

        $scope.startEdit = function(kosten) {
            $scope.selectedItem = kosten;
            $scope.item = angular.copy(kosten);
            $scope.detailsmode = 'edit';
            $scope.showDetails = true;
        };

        $scope.startAdd = function() {
            $scope.selectedItem = null;
            $scope.item = {van: (new Date()).getTime(), totEnMet: null, gasPerKuub: null, stroomPerkWh: null, omschrijving: ''};
            $scope.showDetails = true;
            $scope.detailsmode = 'add';
        };

        $scope.save = function() {
            angular.copy($scope.item, $scope.selectedItem);
            $scope.showDetails = false;
        };

        $scope.cancelEdit = function() {
            console.log('cancelEdit');
            $scope.showDetails = false;
        }
    }]);
