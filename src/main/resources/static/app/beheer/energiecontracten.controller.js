(function() {
    'use strict';

    angular
        .module('app')
        .controller('EnergieContractenController', EnergieContractenController);

    EnergieContractenController.$inject = ['$scope', '$log', 'EnergieContractenService', 'LoadingIndicatorService', 'ErrorMessageService'];

    function EnergieContractenController($scope, $log, EnergieContractenService, LoadingIndicatorService, ErrorMessageService) {

        function activate() {
            LoadingIndicatorService.startLoading();

            EnergieContractenService.query(
                function(data) {
                    $scope.kosten = data;
                    LoadingIndicatorService.stopLoading();
                },
                function(errorResponse) {
                    LoadingIndicatorService.stopLoading();
                    handleServiceError('Ophalen van gegevens is niet gelukt.', errorResponse);
                }
            );
        }

        activate();

        $scope.startEdit = function(kosten) {
            $scope.item = angular.copy(kosten);
            $scope.selectedId = $scope.item.id;
            $scope.detailsmode = 'edit';
            $scope.showDetails = true;
        };

        $scope.startAdd = function() {
            $scope.item = new EnergieContractenService({van: Date.today().getTime(), gasPerKuub: null, stroomPerKwh: null, leverancier: ''});
            $scope.detailsmode = 'add';
            $scope.showDetails = true;
        };

        function saveAdd() {
            $scope.item.$save(
                function(successResult) {
                    $scope.item.id = successResult.id;
                    $scope.kosten.push($scope.item);
                    $scope.cancelEdit();
                    LoadingIndicatorService.stopLoading();
                },
                function(errorResult) {
                    LoadingIndicatorService.stopLoading();
                    handleServiceError('Opslaan is niet gelukt.', errorResult);
                }
            );
        }

        function saveEdit() {
            $scope.item.$save(
                function(successResult) {
                    var index = getIndexOfItemWithId($scope.item.id, $scope.kosten);
                    angular.copy($scope.item, $scope.kosten[index]);
                    $scope.cancelEdit();
                    LoadingIndicatorService.stopLoading();
                },
                function(errorResult) {
                    LoadingIndicatorService.stopLoading();
                    handleServiceError('Opslaan is niet gelukt.', errorResult);
                }
            );
        }

        $scope.save = function() {
            LoadingIndicatorService.startLoading();

            $log.info('Save kosten: ' + JSON.stringify($scope.item));

            if ($scope.detailsmode == 'add') {
                saveAdd();
            } else if ($scope.detailsmode == 'edit') {
                saveEdit();
            } else {
                handleTechnicalError('Onverwachte waarde voor attribuut detailsmode: ' + $scope.detailsmode);
            }
        };

        $scope.cancelEdit = function() {
            $scope.selectedId = null;
            $scope.showDetails = false;
        };

        $scope.delete = function() {
            LoadingIndicatorService.startLoading();

            $log.info('Delete kosten: ' + JSON.stringify($scope.item));

            var index = getIndexOfItemWithId($scope.item.id, $scope.kosten);

            EnergieContractenService.delete({id: $scope.item.id},
                function(successResult) {
                    $scope.kosten.splice(index, 1);
                    $scope.cancelEdit();
                    LoadingIndicatorService.stopLoading();
                },
                function(errorResult) {
                    LoadingIndicatorService.stopLoading();
                    handleServiceError('Verwijderen is niet gelukt.', errorResult);
                }
            );
        };

        function getIndexOfItemWithId(id, items) {
            var result = null;
            for (var i = 0; i < items.length; i++) {
                if (items[i].id == id) {
                    result = i;
                }
            }
            return result;
        }

        function handleServiceError(message, errorResult) {
            if (errorResult.data && errorResult.data.code == 'UNIQUE_KEY_CONSTRAINT_VIOLATION') {
                var userMessage = 'Er bestaat al een rij met dezelfde vanaf datum. Kies een andere datum a.u.b.';
                ErrorMessageService.showMessage(userMessage);
            } else {
                $log.error(message + ' Cause=' + JSON.stringify(errorResult));
                ErrorMessageService.showMessage(message);
            }
        }

        function handleTechnicalError(details) {
            var message = 'Er is een onverwachte fout opgetreden.';
            $log.error(message + ' ' + details);
            ErrorMessageService.showMessage(message);
        }
    }
})();
