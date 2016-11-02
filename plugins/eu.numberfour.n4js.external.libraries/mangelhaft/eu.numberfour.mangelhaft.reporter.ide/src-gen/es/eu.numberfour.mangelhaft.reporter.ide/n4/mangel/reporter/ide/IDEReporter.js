(function(System) {
	'use strict';
	System.register([
		'eu.numberfour.mangelhaft/n4/mangel/mangeltypes/ITestReporter',
		'eu.numberfour.mangelhaft/n4/mangel/mangeltypes/TestSpy'
	], function($n4Export) {
		var ITestReporter, TestSpy, IDEReporter;
		IDEReporter = function IDEReporter(endpoint, timeoutBufferOverride) {
			this.endpointValue = "MISSING_REQUIRED_ENDPOINT";
			this.timeoutBuffer = 1000 * 30;
			this.fetch = fetch;
			this.spy = undefined;
			ITestReporter.$fieldInit(this, undefined, {
				endpointValue: undefined,
				timeoutBuffer: undefined,
				fetch: undefined,
				spy: undefined,
				endpoint: undefined
			});
			if (endpoint) {
				this.endpoint = endpoint.replace(/\/+$/, "");
			}
			if (timeoutBufferOverride !== undefined) {
				this.timeoutBuffer = timeoutBufferOverride;
			}
		};
		$n4Export('IDEReporter', IDEReporter);
		return {
			setters: [
				function($_import_eu_u002enumberfour_u002emangelhaft_n4_u002fmangel_u002fmangeltypes_u002fITestReporter) {
					ITestReporter = $_import_eu_u002enumberfour_u002emangelhaft_n4_u002fmangel_u002fmangeltypes_u002fITestReporter.ITestReporter;
				},
				function($_import_eu_u002enumberfour_u002emangelhaft_n4_u002fmangel_u002fmangeltypes_u002fTestSpy) {
					TestSpy = $_import_eu_u002enumberfour_u002emangelhaft_n4_u002fmangel_u002fmangeltypes_u002fTestSpy.TestSpy;
				}
			],
			execute: function() {
				$makeClass(IDEReporter, Object, [
					ITestReporter
				], {
					send: {
						value: function send___n4(uri, method, headers, body) {
							return $spawn(function*() {
								let ret, bodyStr;
								try {
									bodyStr = JSON.stringify(body, (function(key, value) {
										if (key === "description") {
											value = undefined;
										}
										return value;
									}).bind(this), 2);
									ret = (yield this.fetch.call(null, this.endpoint + uri, {
										method: method,
										headers: headers,
										body: bodyStr
									}));
								} catch(er) {
									let err = er;
									console.error(err);
									console.error(err.stack);
								}
								if (ret) {
									if (Math.floor(ret.status / 100) != 2) {
										console.error("STATUS:", ret.status, ret.statusText, uri);
										if (bodyStr) {
											console.error("BODY:" + bodyStr);
										}
									}
								}
								return ret;
							}.apply(this, arguments));
						}
					},
					register: {
						value: function register___n4() {
							return $spawn(function*() {
								let that = this, sessionId = null, inParameterized = false;
								;
								var handleTestingStart = function handleTestingStart(numAllGroups, sid, numAllTests) {
									return $spawn(function*() {
										sessionId = sid;
										let response = (yield that.send([
											"/n4.ide/testing/sessions",
											sessionId,
											"start"
										].join("/"), 'POST', {
											'Content-Type': "application/vnd.n4.ide.start_session_req.tm+json",
											Accept: "application/json"
										}, undefined));
										return response;
									}.apply(this, arguments));
								};
								this.spy.testingStarted.add(handleTestingStart);
								this.spy.parameterizedGroupsStarted.add((function(test) {
									return inParameterized = true;
								}).bind(this));
								var handleTestStart = function handleTestStart(groupName, testName, timeout) {
									return $spawn(function*() {
										if (inParameterized) {
											return;
										}
										if (!sessionId) {
											throw new Error("Test start sent before session start");
										}
										let response = (yield that.send([
											"/n4.ide/testing/sessions",
											sessionId,
											"tests",
											groupName + "%23" + testName,
											"start"
										].join("/"), 'POST', {
											'Content-Type': "application/vnd.n4.ide.start_test_req.tm+json",
											Accept: "application/json"
										}, {
											timeout: timeout + that.timeoutBuffer
										}));
									}.apply(this, arguments));
								};
								this.spy.testStarted.add((function(group, test) {
									return $spawn(function*() {
										(yield handleTestStart(group.name, test.name, test.timeout));
									}.apply(this, arguments));
								}).bind(this));
								var handleTestFinished = function handleTestFinished(groupName, testName, testResult) {
									return $spawn(function*() {
										if (inParameterized) {
											return;
										}
										if (!sessionId) {
											throw new Error("Test end sent outside active session");
										}
										let response = (yield that.send([
											"/n4.ide/testing/sessions",
											sessionId,
											"tests",
											groupName + "%23" + testName,
											"end"
										].join("/"), 'POST', {
											'Content-Type': "application/vnd.n4.ide.end_test_req.tm+json",
											Accept: "application/json"
										}, testResult));
									}.apply(this, arguments));
								};
								this.spy.testFinished.add((function(group, test, testResult) {
									return $spawn(function*() {
										(yield handleTestFinished(group.name, test.name, testResult));
									}.apply(this, arguments));
								}).bind(this));
								this.spy.parameterizedGroupsFinished.add((function(resultGroups) {
									return $spawn(function*() {
										inParameterized = false;
										let resultGroup = resultGroups.aggregate();
										for(let testResult of resultGroup.testResults) {
											(yield handleTestStart(resultGroup.description, testResult.description, 100));
											(yield handleTestFinished(resultGroup.description, testResult.description, testResult));
										}
									}.apply(this, arguments));
								}).bind(this));
								var handleTestingFinished = function handleTestingFinished(resultGroups) {
									return $spawn(function*() {
										let response = (yield that.send([
											"/n4.ide/testing/sessions",
											sessionId,
											"end"
										].join("/"), 'POST', {
											'Content-Type': "application/vnd.n4.ide.end_session_req.tm+json",
											Accept: "application/json"
										}, undefined));
										return response;
									}.apply(this, arguments));
								};
								this.spy.testingFinished.add(handleTestingFinished);
								return this;
							}.apply(this, arguments));
						}
					},
					endpoint: {
						get: function getEndpoint___n4() {
							return this.endpointValue;
						},
						set: function setEndpoint___n4(endpoint) {
							this.endpointValue = endpoint.replace(/\/+$/, "");
						}
					},
					endpointValue: {
						value: undefined,
						writable: true
					},
					timeoutBuffer: {
						value: undefined,
						writable: true
					},
					fetch: {
						value: undefined,
						writable: true
					},
					spy: {
						value: undefined,
						writable: true
					}
				}, {}, function(instanceProto, staticProto) {
					var metaClass = new N4Class({
						name: 'IDEReporter',
						origin: 'eu.numberfour.mangelhaft.reporter.ide',
						fqn: 'n4.mangel.reporter.ide.IDEReporter.IDEReporter',
						n4superType: N4Object.n4type,
						allImplementedInterfaces: [
							'n4.mangel.mangeltypes.ITestReporter.ITestReporter'
						],
						ownedMembers: [
							new N4DataField({
								name: 'endpointValue',
								isStatic: false,
								annotations: []
							}),
							new N4Accessor({
								name: 'endpoint',
								getter: true,
								isStatic: false,
								annotations: []
							}),
							new N4Accessor({
								name: 'endpoint',
								getter: false,
								isStatic: false,
								annotations: []
							}),
							new N4DataField({
								name: 'timeoutBuffer',
								isStatic: false,
								annotations: []
							}),
							new N4DataField({
								name: 'fetch',
								isStatic: false,
								annotations: []
							}),
							new N4DataField({
								name: 'spy',
								isStatic: false,
								annotations: [
									new N4Annotation({
										name: 'Inject',
										details: []
									})
								]
							}),
							new N4Method({
								name: 'send',
								isStatic: false,
								jsFunction: instanceProto['send'],
								annotations: []
							}),
							new N4Method({
								name: 'constructor',
								isStatic: false,
								jsFunction: instanceProto['constructor'],
								annotations: []
							}),
							new N4Method({
								name: 'register',
								isStatic: false,
								jsFunction: instanceProto['register'],
								annotations: []
							})
						],
						consumedMembers: [],
						annotations: []
					});
					return metaClass;
				});
				Object.defineProperty(IDEReporter, '$di', {
					value: {
						fieldsInjectedTypes: [
							{
								name: 'spy',
								type: TestSpy
							}
						]
					}
				});
			}
		};
	});
})(typeof module !== 'undefined' && module.exports ? require('n4js-node/index').System(require, module) : System);
//# sourceMappingURL=IDEReporter.map