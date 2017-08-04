(function() {
    'use strict';

    angular
        .module('app')
        .controller('MultipleDateSelectionController', MultipleDateSelectionController);

    MultipleDateSelectionController.$inject = ['$uibModalInstance', 'datepickerOptions', 'selectedDates'];

    function MultipleDateSelectionController($uibModalInstance, datepickerOptions, selectedDates) {
        var vm = this;
        vm.selectedDates = _.clone(selectedDates);

        vm.datepickerOptions = datepickerOptions;

        vm.ok = function () {
            $uibModalInstance.close(vm.selectedDates);
        };

        vm.cancel = function () {
            $uibModalInstance.dismiss('cancel');
        };

        vm.addDate = function () {
            vm.selectedDates.push(vm.selectedDate);
            vm.selectedDates.sort(function(a,b){return a.getTime() - b.getTime();});
            vm.selectedDate = null;
        };

        vm.removeDate = function(dateToRemove) {
            _.pull(vm.selectedDates, dateToRemove);
        };

        vm.isAddDateDisabled = function() {
            return !_.isDate(vm.selectedDate) || containsDate(vm.selectedDates, vm.selectedDate);
        };

        function containsDate(selectedDates, date) {
            return _.isDate(_.find(selectedDates, function (o) {
                return o.getTime() === date.getTime();
            }));
        }
    }

})();

