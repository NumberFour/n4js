(function(System) {
	'use strict';
	System.register([], function($n4Export) {
		var testStatusIntMap, aggregateTestStatuses;
		aggregateTestStatuses = function aggregateTestStatuses(testStatus1, testStatus2) {
			return testStatusIntMap.get(testStatus1) > testStatusIntMap.get(testStatus2) ? testStatus1 : testStatus2;
		};
		$n4Export('aggregateTestStatuses', aggregateTestStatuses);
		return {
			setters: [],
			execute: function() {
				testStatusIntMap = new Map([
					[
						'PASSED',
						0
					],
					[
						'SKIPPED_PRECONDITION',
						1
					],
					[
						'SKIPPED_IGNORE',
						2
					],
					[
						'SKIPPED',
						3
					],
					[
						'SKIPPED_NOT_IMPLEMENTED',
						4
					],
					[
						'SKIPPED_FIXME',
						5
					],
					[
						'FAILED',
						6
					],
					[
						'ERROR',
						7
					]
				]);
			}
		};
	});
})(typeof module !== 'undefined' && module.exports ? require('n4js-node/index').System(require, module) : System);
//# sourceMappingURL=TestStatus.map
