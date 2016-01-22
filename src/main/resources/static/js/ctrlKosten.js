'use strict';

angular.module('appHomecontrol.kostenController', [])

    .controller('KostenController', ['$scope', '$resource', '$log', 'Kosten', function($scope, $resource, $log, Kosten) {
        $scope.kosten = Kosten.query(function() {});

        $scope.startEdit = function(kosten) {
            $scope.selectedItem = kosten;
            $scope.item = angular.copy(kosten);
            $scope.detailsmode = 'edit';
            $scope.showDetails = true;
            // TODO: set focus of leverancier field
        };

        $scope.startAdd = function() {
            $scope.selectedItem = null;
            $scope.item = new Kosten({van: (new Date()).getTime(), totEnMet: 0, gasPerKuub: null, stroomPerKwh: null, leverancier: ''});
            $scope.detailsmode = 'add';
            $scope.showDetails = true;
            // TODO: set focus of leverancier field
        };

        $scope.save = function() {
            if ($scope.detailsmode == 'add') {
                $scope.item.$save(
                    function(successResult) {
                        $scope.item.id = successResult.id;
                        $scope.kosten.push($scope.item);
                    },
                    function(errorResult) {
                        handleServiceError('Opslaan is niet gelukt.', errorResult);
                    }
                );
            } else if ($scope.detailsmode == 'edit') {
                $scope.item.$save(
                    function(successResult) {
                        angular.copy($scope.item, $scope.selectedItem);
                    },
                    function(errorResult) {
                        handleServiceError('Opslaan is niet gelukt.', errorResult);
                    }
                );
            } else {
                handleTechnicalError('Onverwachte waarde voor attribuut detailsmode: ' + $scope.detailsmode);
            }
            $scope.cancelEdit();
        };

        $scope.cancelEdit = function() {
            $scope.selectedItem = null;
            $scope.showDetails = false;
        };

        $scope.delete = function() {
            for (var i = 0; i < $scope.kosten.length; i++) {
                if ($scope.kosten[i].id == $scope.selectedItem.id) {
                    Kosten.delete({id: $scope.selectedItem.id},
                        function(successResult) {
                            $scope.kosten.splice(i, 1);
                        },
                        function(errorResult) {
                            handleServiceError('Verwijderen is niet gelukt.', errorResult);
                        }
                    );
                    break;
                }
            }
            $scope.cancelEdit();
        };

        function handleServiceError(message, errorResult) {
            // TODO add when defined + ' (' + errorResult.data.message + ')' + ' Path=' + errorResult.data.path
            $log.error(message + ' Cause=' + errorResult.status);
            alert(message);
        };

        function handleTechnicalError(details) {
            var message = 'Er is een onverwachte fout opgetreden.';
            $log.error(message + ' ' + details);
            alert(message);
        }

    }]);
