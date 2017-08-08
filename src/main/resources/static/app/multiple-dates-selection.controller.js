(function() {
    'use strict';

    angular
        .module('app')
        .controller('MultipleDateSelectionController', MultipleDateSelectionController);

    MultipleDateSelectionController.$inject = ['$filter', '$uibModalInstance', 'datepickerOptions', 'selectedDates', 'selectedDateFormat'];

    function MultipleDateSelectionController($filter, $uibModalInstance, datepickerOptions, selectedDates, selectedDateFormat) {
        var vm = this;

        vm.ok = ok;
        vm.cancel = cancel;
        vm.removeDate = removedate;
        vm.datepickerChange = datepickerChange;
        vm.formatSelectedDate = formatSelectedDate;

        vm.selectedDates = _.clone(selectedDates);
        vm.datepickerOptions = datepickerOptions;

        function ok() {
            $uibModalInstance.close(vm.selectedDates);
        }

        function cancel() {
            $uibModalInstance.dismiss('cancel');
        }

        function removedate(dateToRemove) {
            _.pull(vm.selectedDates, dateToRemove);
        }

        function datepickerChange() {
            if (_.isDate(vm.selectedDate) && !containsDate(vm.selectedDates, vm.selectedDate)) {
                addSelectedDateToSelectedDates();
                vm.selectedDate = null;
            }
        }

        function formatSelectedDate(selectedDate) {
            return $filter('date')(selectedDate, selectedDateFormat);
        }

        function addSelectedDateToSelectedDates() {
            vm.selectedDates.push(vm.selectedDate);
            vm.selectedDates.sort(function(a,b){return a.getTime() - b.getTime();});
        }

        function containsDate(selectedDates, date) {
            return _.isDate(_.find(selectedDates, function (o) {
                return o.getTime() === date.getTime();
            }));
        }
    }

})();

