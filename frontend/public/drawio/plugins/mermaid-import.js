/**
 * Easy Draw Mermaid import bridge.
 */
Draw.loadPlugin(function(ui)
{
	if (window.__easyDrawMermaidImport)
	{
		return;
	}

	window.__easyDrawMermaidImport = true;

	// ========================================
	// Configuration
	// ========================================
	
	var CONFIG = {
		MAX_MESSAGE_SIZE: 1024 * 1024, // 1MB
		PARSE_TIMEOUT: 10000,           // 10 seconds
		DEBUG_MODE: false,              // Debug mode
		ALLOWED_ORIGINS: ['*']          // Allowed origins ('*' means all)
	};

	// Error codes
	var ERROR_CODES = {
		INVALID_FORMAT: 'INVALID_FORMAT',
		EMPTY_MERMAID: 'EMPTY_MERMAID',
		PARSE_ERROR: 'PARSE_ERROR',
		UNSUPPORTED_TYPE: 'UNSUPPORTED_TYPE',
		TIMEOUT: 'TIMEOUT',
		INSERT_FAILED: 'INSERT_FAILED',
		ORIGIN_DENIED: 'ORIGIN_DENIED',
		SIZE_EXCEEDED: 'SIZE_EXCEEDED',
		XSS_DETECTED: 'XSS_DETECTED',
		UNSUPPORTED_BROWSER: 'UNSUPPORTED_BROWSER'
	};

	// ========================================
	// Style Modification Constants
	// ========================================
	
	// Style modification error codes
	var STYLE_ERROR_CODES = {
		INVALID_FORMAT: 'INVALID_FORMAT',
		INVALID_TARGET: 'INVALID_TARGET',
		NO_TARGET_CELLS: 'NO_TARGET_CELLS',
		INVALID_PROPERTY: 'INVALID_PROPERTY',
		INVALID_VALUE: 'INVALID_VALUE',
		INVALID_OPERATION: 'INVALID_OPERATION',
		UNSUPPORTED_OPERATION: 'UNSUPPORTED_OPERATION',
		ORIGIN_DENIED: 'ORIGIN_DENIED'
	};

	// Valid target selectors
	var VALID_TARGETS = ['selected', 'edges', 'vertices', 'all'];

	// Valid operation types
	var VALID_OPERATIONS = ['set', 'increase', 'decrease', 'multiply'];

	// Color properties (only support absolute value setting)
	var COLOR_PROPERTIES = [
		'fillColor',
		'strokeColor',
		'fontColor',
		'gradientColor',
		'labelBackgroundColor',
		'labelBorderColor',
		'shadowColor',
		'swimlaneFillColor'
	];

	// Numeric properties (support relative operations)
	var NUMERIC_PROPERTIES = [
		'strokeWidth',
		'fontSize',
		'opacity',
		'fillOpacity',
		'strokeOpacity',
		'textOpacity',
		'arcSize',
		'startSize',
		'endSize',
		'spacing',
		'spacingTop',
		'spacingBottom',
		'spacingLeft',
		'spacingRight',
		'rotation'
	];

	// Boolean properties (0 or 1)
	var BOOLEAN_PROPERTIES = [
		'dashed',
		'rounded',
		'curved',
		'shadow',
		'startFill',
		'endFill',
		'editable',
		'movable',
		'resizable',
		'rotatable',
		'deletable',
		'bendable',
		'foldable'
	];

	// Enum properties with valid values
	var ENUM_PROPERTIES = {
		align: ['left', 'center', 'right'],
		verticalAlign: ['top', 'middle', 'bottom'],
		labelPosition: ['left', 'center', 'right'],
		verticalLabelPosition: ['top', 'middle', 'bottom'],
		startArrow: ['none', 'classic', 'classicThin', 'block', 'blockThin', 'open', 'openThin', 'oval', 'diamond', 'diamondThin'],
		endArrow: ['none', 'classic', 'classicThin', 'block', 'blockThin', 'open', 'openThin', 'oval', 'diamond', 'diamondThin']
	};

	// Edge-only properties (only apply to edges)
	var EDGE_ONLY_PROPERTIES = [
		'startArrow',
		'endArrow',
		'startSize',
		'endSize',
		'startFill',
		'endFill',
		'curved'
	];

	// Value constraints for properties
	var VALUE_CONSTRAINTS = {
		// Opacity properties: 0-100
		opacity: { min: 0, max: 100 },
		fillOpacity: { min: 0, max: 100 },
		strokeOpacity: { min: 0, max: 100 },
		textOpacity: { min: 0, max: 100 },
		shadowOpacity: { min: 0, max: 100 },
		
		// Stroke width: minimum 0
		strokeWidth: { min: 0 },
		
		// Font size: minimum 1
		fontSize: { min: 1 },
		
		// Arc size: 0-100
		arcSize: { min: 0, max: 100 },
		
		// Rotation angle: 0-360
		rotation: { min: 0, max: 360 },
		
		// Arrow sizes: minimum 0
		startSize: { min: 0 },
		endSize: { min: 0 }
	};

	// Initialize configuration from URL parameters and global config
	(function initConfig() {
		try {
			// Read from URL parameters
			if (typeof URLSearchParams !== 'undefined' && typeof window !== 'undefined') {
				var urlParams = new URLSearchParams(window.location.search);
				
				var maxMessageSize = urlParams.get('maxMessageSize');
				if (maxMessageSize !== null) {
					var size = parseInt(maxMessageSize, 10);
					if (!isNaN(size) && size > 0) {
						CONFIG.MAX_MESSAGE_SIZE = size;
					}
				}
				
				var parseTimeout = urlParams.get('parseTimeout');
				if (parseTimeout !== null) {
					var timeout = parseInt(parseTimeout, 10);
					if (!isNaN(timeout) && timeout > 0) {
						CONFIG.PARSE_TIMEOUT = timeout;
					}
				}
				
				var debugMode = urlParams.get('debugMode');
				if (debugMode !== null) {
					CONFIG.DEBUG_MODE = debugMode === 'true' || debugMode === '1';
				}
				
				var allowedOrigins = urlParams.get('allowedOrigins');
				if (allowedOrigins !== null && allowedOrigins.length > 0) {
					CONFIG.ALLOWED_ORIGINS = allowedOrigins.split(',').map(function(origin) {
						return origin.trim();
					}).filter(function(origin) {
						return origin.length > 0;
					});
				}
			}

			// Read from global config (overrides URL params)
			if (typeof window !== 'undefined' && window.DRAWIO_CONFIG) {
				var globalConfig = window.DRAWIO_CONFIG;
				
				if (typeof globalConfig.maxMessageSize === 'number' && globalConfig.maxMessageSize > 0) {
					CONFIG.MAX_MESSAGE_SIZE = globalConfig.maxMessageSize;
				}
				
				if (typeof globalConfig.parseTimeout === 'number' && globalConfig.parseTimeout > 0) {
					CONFIG.PARSE_TIMEOUT = globalConfig.parseTimeout;
				}
				
				if (typeof globalConfig.debugMode === 'boolean') {
					CONFIG.DEBUG_MODE = globalConfig.debugMode;
				}
				
				if (Array.isArray(globalConfig.allowedOrigins) && globalConfig.allowedOrigins.length > 0) {
					CONFIG.ALLOWED_ORIGINS = globalConfig.allowedOrigins;
				}
			}
		} catch (e) {
			console.warn('[MermaidImport] Failed to initialize config:', e);
		}
	})();

	// ========================================
	// Logging Functions
	// ========================================
	
	/**
	 * Log a message with level and context
	 * @param {string} level - Log level (error, warn, info, debug)
	 * @param {string} message - Log message
	 * @param {*} context - Additional context (optional)
	 */
	function log(level, message, context) {
		// Skip debug logs if debug mode is off
		if (!CONFIG.DEBUG_MODE && level === 'debug') {
			return;
		}
		
		var timestamp = new Date().toISOString();
		var logMessage = '[' + timestamp + '] [MermaidImport] [' + level.toUpperCase() + '] ' + message;
		
		if (level === 'error') {
			console.error(logMessage, context || '');
		} else if (level === 'warn') {
			console.warn(logMessage, context || '');
		} else {
			console.log(logMessage, context || '');
		}
	}

	// ========================================
	// Performance Monitoring Functions
	// ========================================
	
	/**
	 * Measure the execution time of an operation
	 * @param {string} operationName - Name of the operation being measured
	 * @param {Function} fn - Function to execute and measure
	 * @returns {*} The result of the function execution
	 */
	function measurePerformance(operationName, fn) {
		// Only measure performance in debug mode
		if (!CONFIG.DEBUG_MODE) {
			return fn();
		}

		var startTime = performance.now();
		var result;
		var error;
		var hasError = false;

		try {
			result = fn();
			return result;
		} catch (e) {
			error = e;
			hasError = true;
			throw e;
		} finally {
			var endTime = performance.now();
			var duration = endTime - startTime;

			if (hasError) {
				log('debug', 'Performance: ' + operationName + ' failed after ' + duration.toFixed(2) + 'ms', {
					operation: operationName,
					duration: duration,
					error: error
				});
			} else {
				log('debug', 'Performance: ' + operationName + ' completed in ' + duration.toFixed(2) + 'ms', {
					operation: operationName,
					duration: duration
				});
			}
		}
	}

	/**
	 * Measure the execution time of an async operation
	 * @param {string} operationName - Name of the operation being measured
	 * @param {Function} fn - Async function to execute and measure
	 * @returns {Promise<*>} Promise that resolves with the result of the function execution
	 */
	function measurePerformanceAsync(operationName, fn) {
		// Only measure performance in debug mode
		if (!CONFIG.DEBUG_MODE) {
			return fn();
		}

		var startTime = performance.now();

		return fn()
			.then(function(result) {
				var endTime = performance.now();
				var duration = endTime - startTime;

				log('debug', 'Performance: ' + operationName + ' completed in ' + duration.toFixed(2) + 'ms', {
					operation: operationName,
					duration: duration
				});

				return result;
			})
			.catch(function(error) {
				var endTime = performance.now();
				var duration = endTime - startTime;

				log('debug', 'Performance: ' + operationName + ' failed after ' + duration.toFixed(2) + 'ms', {
					operation: operationName,
					duration: duration,
					error: error
				});

				throw error;
			});
	}

	// ========================================
	// Browser Compatibility Functions
	// ========================================
	
	/**
	 * Check if browser supports required features
	 * @returns {{supported: boolean, error?: string, errorCode?: string}}
	 */
	function checkBrowserCompatibility() {
		// Check if Promise is available
		if (typeof Promise === 'undefined') {
			return {
				supported: false,
				error: 'Browser does not support Promise API',
				errorCode: ERROR_CODES.UNSUPPORTED_BROWSER
			};
		}

		// Check if structuredClone is available
		// Note: structuredClone is used by Mermaid parsing in draw.io
		if (typeof structuredClone === 'undefined') {
			return {
				supported: false,
				error: 'Browser does not support structuredClone API',
				errorCode: ERROR_CODES.UNSUPPORTED_BROWSER
			};
		}

		return { supported: true };
	}

	// ========================================
	// Validation Functions
	// ========================================
	
	/**
	 * Check if origin is allowed
	 * @param {string} origin - Origin to check
	 * @returns {boolean} True if origin is allowed
	 */
	function isOriginAllowed(origin) {
		if (CONFIG.ALLOWED_ORIGINS.indexOf('*') !== -1) {
			return true;
		}
		return CONFIG.ALLOWED_ORIGINS.indexOf(origin) !== -1;
	}

	/**
	 * Check if message contains XSS attack vectors
	 * @param {string} text - Text to check
	 * @returns {boolean} True if XSS detected
	 */
	function containsXSS(text) {
		var xssPatterns = [
			/<script[\s\S]*?>[\s\S]*?<\/script>/gi,
			/javascript:/gi,
			/on\w+\s*=/gi,  // onclick, onload, etc.
			/<iframe/gi
		];
		
		for (var i = 0; i < xssPatterns.length; i++) {
			if (xssPatterns[i].test(text)) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Validate message format and security
	 * @param {MessageEvent} evt - Message event
	 * @param {Object} data - Parsed message data
	 * @returns {{valid: boolean, error?: string, errorCode?: string}}
	 */
	function validateMessage(evt, data) {
		return measurePerformance('Message Validation', function() {
			// Validate origin
			if (!evt || !evt.origin) {
				log('warn', 'Message received without origin');
				return {
					valid: false,
					error: 'Message origin is missing',
					errorCode: ERROR_CODES.ORIGIN_DENIED
				};
			}

			if (!isOriginAllowed(evt.origin)) {
				log('warn', 'Message from disallowed origin: ' + evt.origin);
				return {
					valid: false,
					error: 'Origin not allowed: ' + evt.origin,
					errorCode: ERROR_CODES.ORIGIN_DENIED
				};
			}

			// Validate message format
			if (!data || typeof data !== 'object') {
				return {
					valid: false,
					error: 'Invalid message format',
					errorCode: ERROR_CODES.INVALID_FORMAT
				};
			}

			if (data.action !== 'generateMermaid') {
				return {
					valid: false,
					error: 'Invalid action',
					errorCode: ERROR_CODES.INVALID_FORMAT
				};
			}

			if (typeof data.mermaid !== 'string') {
				return {
					valid: false,
					error: 'Missing or invalid mermaid field',
					errorCode: ERROR_CODES.INVALID_FORMAT
				};
			}

			// Validate mermaid content
			if (data.mermaid.trim() === '') {
				return {
					valid: false,
					error: 'Mermaid text is empty',
					errorCode: ERROR_CODES.EMPTY_MERMAID
				};
			}

			// Check message size
			var messageSize = JSON.stringify(data).length;
			if (messageSize > CONFIG.MAX_MESSAGE_SIZE) {
				log('warn', 'Message size exceeded: ' + messageSize + ' bytes');
				return {
					valid: false,
					error: 'Message size exceeds limit (' + CONFIG.MAX_MESSAGE_SIZE + ' bytes)',
					errorCode: ERROR_CODES.SIZE_EXCEEDED
				};
			}

			// Check for XSS
			if (containsXSS(data.mermaid)) {
				log('warn', 'XSS attack detected in mermaid text');
				return {
					valid: false,
					error: 'Potential XSS attack detected',
					errorCode: ERROR_CODES.XSS_DETECTED
				};
			}

			return { valid: true };
		});
	}

	/**
	 * Validate style modification message
	 * @param {Object} data - Message data
	 * @returns {{valid: boolean, error?: string, errorCode?: string}}
	 */
	function validateStyleMessage(data) {
		// Validate target field exists
		if (!data.target) {
			return {
				valid: false,
				error: 'Missing target field',
				errorCode: STYLE_ERROR_CODES.INVALID_FORMAT
			};
		}

		// Validate target value is valid
		if (VALID_TARGETS.indexOf(data.target) === -1) {
			return {
				valid: false,
				error: 'Invalid target: ' + data.target,
				errorCode: STYLE_ERROR_CODES.INVALID_TARGET
			};
		}

		// Validate that at least styles or operations exists
		if (!data.styles && !data.operations) {
			return {
				valid: false,
				error: 'Missing styles or operations field',
				errorCode: STYLE_ERROR_CODES.INVALID_FORMAT
			};
		}

		return { valid: true };
	}

	/**
	 * Clamp value within valid range for a property
	 * @param {string} property - Property name
	 * @param {number} value - Value to clamp
	 * @returns {number} Clamped value
	 */
	function clampValue(property, value) {
		var constraint = VALUE_CONSTRAINTS[property];
		if (!constraint) {
			return value;
		}
		
		if (constraint.min !== undefined && value < constraint.min) {
			return constraint.min;
		}
		
		if (constraint.max !== undefined && value > constraint.max) {
			return constraint.max;
		}
		
		return value;
	}

	// ========================================
	// Target Selector Functions
	// ========================================
	
	/**
	 * Get target cells based on target selector
	 * @param {string} target - Target selector ('selected', 'edges', 'vertices', 'all')
	 * @param {mxGraph} graph - Graph object
	 * @returns {{cells: Array, error?: string, errorCode?: string}}
	 */
	function getTargetCells(target, graph) {
		try {
			var parent = graph.getDefaultParent();
			var cells = [];

			switch (target) {
				case 'selected':
					cells = graph.getSelectionCells();
					break;
				case 'edges':
					cells = graph.getChildEdges(parent);
					break;
				case 'vertices':
					cells = graph.getChildVertices(parent);
					break;
				case 'all':
					cells = graph.getChildCells(parent);
					break;
				default:
					return {
						cells: [],
						error: 'Invalid target: ' + target,
						errorCode: STYLE_ERROR_CODES.INVALID_TARGET
					};
			}

			// Check if cells array is empty
			if (!cells || cells.length === 0) {
				return {
					cells: [],
					error: 'No target cells found',
					errorCode: STYLE_ERROR_CODES.NO_TARGET_CELLS
				};
			}

			return { cells: cells };
		} catch (e) {
			log('error', 'Failed to get target cells', e);
			return {
				cells: [],
				error: 'Failed to get target cells: ' + (e.message || String(e)),
				errorCode: STYLE_ERROR_CODES.INVALID_TARGET
			};
		}
	}

	// ========================================
	// Style Application Functions
	// ========================================
	
	/**
	 * Filter cells for property application (edge-only properties)
	 * @param {Array} cells - Array of cells to filter
	 * @param {string} property - Property name
	 * @param {mxGraph} graph - Graph object
	 * @returns {Array} Filtered array of cells
	 */
	function filterCellsForProperty(cells, property, graph) {
		// Check if property is edge-only
		if (EDGE_ONLY_PROPERTIES.indexOf(property) !== -1) {
			// Filter to only include edges
			var filteredCells = [];
			for (var i = 0; i < cells.length; i++) {
				var cell = cells[i];
				if (graph.getModel().isEdge(cell)) {
					filteredCells.push(cell);
				}
			}
			return filteredCells;
		}
		
		// For non-edge-only properties, return all cells
		return cells;
	}

	/**
	 * Validate enum property value
	 * @param {string} property - Property name
	 * @param {*} value - Value to validate
	 * @returns {{valid: boolean, error?: string, errorCode?: string}}
	 */
	function validateEnumValue(property, value) {
		// Check if property is an enum property
		if (ENUM_PROPERTIES.hasOwnProperty(property)) {
			var validValues = ENUM_PROPERTIES[property];
			if (validValues.indexOf(value) === -1) {
				return {
					valid: false,
					error: 'Invalid value for ' + property + ': ' + value + '. Valid values: ' + validValues.join(', '),
					errorCode: STYLE_ERROR_CODES.INVALID_VALUE
				};
			}
		}
		return { valid: true };
	}

	/**
	 * Apply absolute style values to cells
	 * @param {Array} cells - Array of cells to apply styles to
	 * @param {Object} styles - Object containing style properties and values
	 * @param {mxGraph} graph - Graph object
	 * @returns {{success: boolean, errors: Array}}
	 */
	function applyAbsoluteStyles(cells, styles, graph) {
		var errors = [];
		
		try {
			// Iterate through all style properties
			for (var key in styles) {
				if (styles.hasOwnProperty(key)) {
					try {
						var value = styles[key];
						
						// Validate enum values
						var enumValidation = validateEnumValue(key, value);
						if (!enumValidation.valid) {
							errors.push({
								property: key,
								error: enumValidation.error,
								errorCode: enumValidation.errorCode
							});
							continue;
						}
						
						// Filter cells for edge-only properties
						var filteredCells = filterCellsForProperty(cells, key, graph);
						
						// Apply style if there are cells to apply to
						if (filteredCells.length > 0) {
							graph.setCellStyles(key, value, filteredCells);
						}
					} catch (e) {
						errors.push({
							property: key,
							error: e.message || String(e),
							errorCode: STYLE_ERROR_CODES.INVALID_PROPERTY
						});
					}
				}
			}
			
			return {
				success: errors.length === 0,
				errors: errors
			};
		} catch (e) {
			log('error', 'Failed to apply absolute styles', e);
			return {
				success: false,
				errors: [{
					property: 'unknown',
					error: e.message || String(e),
					errorCode: STYLE_ERROR_CODES.INVALID_FORMAT
				}]
			};
		}
	}

	/**
	 * Apply relative operations to cells
	 * @param {Array} cells - Array of cells to apply operations to
	 * @param {Object} operations - Object containing property operations
	 * @param {mxGraph} graph - Graph object
	 * @returns {{success: boolean, errors: Array}}
	 */
	function applyRelativeOperations(cells, operations, graph) {
		var errors = [];
		
		try {
			// Iterate through all operation properties
			for (var key in operations) {
				if (operations.hasOwnProperty(key)) {
					try {
						var op = operations[key];
						
						// Validate operation structure
						if (!op || typeof op !== 'object' || !op.op || op.value === undefined) {
							errors.push({
								property: key,
								error: 'Invalid operation structure',
								errorCode: STYLE_ERROR_CODES.INVALID_OPERATION
							});
							continue;
						}
						
						// Validate operation type
						if (VALID_OPERATIONS.indexOf(op.op) === -1) {
							errors.push({
								property: key,
								error: 'Invalid operation type: ' + op.op,
								errorCode: STYLE_ERROR_CODES.INVALID_OPERATION
							});
							continue;
						}
						
						// Check if property is a color property
						// Color properties only support 'set' operation
						if (COLOR_PROPERTIES.indexOf(key) !== -1 && op.op !== 'set') {
							errors.push({
								property: key,
								error: 'Color properties only support set operation',
								errorCode: STYLE_ERROR_CODES.UNSUPPORTED_OPERATION
							});
							continue;
						}
						
						// Apply the operation to cells
						applyOperationToCells(cells, key, op.op, op.value, graph);
						
					} catch (e) {
						errors.push({
							property: key,
							error: e.message || String(e),
							errorCode: STYLE_ERROR_CODES.INVALID_OPERATION
						});
					}
				}
			}
			
			return {
				success: errors.length === 0,
				errors: errors
			};
		} catch (e) {
			log('error', 'Failed to apply relative operations', e);
			return {
				success: false,
				errors: [{
					property: 'unknown',
					error: e.message || String(e),
					errorCode: STYLE_ERROR_CODES.INVALID_FORMAT
				}]
			};
		}
	}

	/**
	 * Apply operation to cells (relative operations)
	 * @param {Array} cells - Array of cells to apply operation to
	 * @param {string} property - Property name
	 * @param {string} operation - Operation type ('set', 'increase', 'decrease', 'multiply')
	 * @param {number} value - Value for the operation
	 * @param {mxGraph} graph - Graph object
	 */
	function applyOperationToCells(cells, property, operation, value, graph) {
		try {
			// Filter cells for edge-only properties
			var filteredCells = filterCellsForProperty(cells, property, graph);
			
			for (var i = 0; i < filteredCells.length; i++) {
				var cell = filteredCells[i];
				
				// Get current property value
				var currentStyle = graph.getCellStyle(cell);
				var currentValue = parseFloat(currentStyle[property]) || 0;
				var newValue;
				
				// Calculate new value based on operation type
				switch (operation) {
					case 'set':
						newValue = value;
						break;
					case 'increase':
						newValue = currentValue + value;
						break;
					case 'decrease':
						newValue = currentValue - value;
						break;
					case 'multiply':
						newValue = currentValue * value;
						break;
					default:
						throw new Error('Invalid operation: ' + operation);
				}
				
				// Apply value range constraints
				newValue = clampValue(property, newValue);
				
				// Apply the new value to the cell
				graph.setCellStyles(property, newValue, [cell]);
			}
		} catch (e) {
			log('error', 'Failed to apply operation to cells', {
				property: property,
				operation: operation,
				value: value,
				error: e
			});
			throw e;
		}
	}

	/**
	 * Apply style modifications to cells (batch operation with undo support)
	 * @param {Array} cells - Array of cells to modify
	 * @param {Object} styles - Absolute style values (optional)
	 * @param {Object} operations - Relative operations (optional)
	 * @param {mxGraph} graph - Graph object
	 * @returns {{success: boolean, modifiedCount: number, errors: Array}}
	 */
	function applyStyleModifications(cells, styles, operations, graph) {
		var model = graph.getModel();
		var modifiedCount = 0;
		var errors = [];
		
		// Use beginUpdate/endUpdate to wrap all operations
		// This ensures:
		// 1. Only one view update is triggered
		// 2. All changes are atomic (can be undone in one operation)
		model.beginUpdate();
		try {
			// Step 1: Apply absolute styles first
			if (styles) {
				log('debug', 'Applying absolute styles', { styles: styles, cellCount: cells.length });
				var stylesResult = applyAbsoluteStyles(cells, styles, graph);
				
				// Track errors from style application
				if (stylesResult.errors && stylesResult.errors.length > 0) {
					errors = errors.concat(stylesResult.errors);
				}
				
				// Count modified cells (at least one style was applied)
				if (stylesResult.success || stylesResult.errors.length < Object.keys(styles).length) {
					modifiedCount = cells.length;
				}
			}
			
			// Step 2: Apply relative operations second
			if (operations) {
				log('debug', 'Applying relative operations', { operations: operations, cellCount: cells.length });
				var operationsResult = applyRelativeOperations(cells, operations, graph);
				
				// Track errors from operations
				if (operationsResult.errors && operationsResult.errors.length > 0) {
					errors = errors.concat(operationsResult.errors);
				}
				
				// Update modified count if operations were applied
				if (operationsResult.success || operationsResult.errors.length < Object.keys(operations).length) {
					modifiedCount = Math.max(modifiedCount, cells.length);
				}
			}
			
			log('debug', 'Style modifications completed', {
				modifiedCount: modifiedCount,
				errorCount: errors.length
			});
			
		} finally {
			// Always call endUpdate, even if an error occurred
			// This ensures the model state is consistent
			model.endUpdate();
		}
		
		return {
			success: errors.length === 0,
			modifiedCount: modifiedCount,
			errors: errors
		};
	}

	// ========================================
	// Response Functions
	// ========================================
	
	/**
	 * Send response message back to sender
	 * @param {MessageEvent} evt - Original message event
	 * @param {boolean} success - Whether operation was successful
	 * @param {string} error - Error message (optional)
	 * @param {string} errorCode - Error code (optional)
	 * @param {Object} data - Additional data (optional)
	 */
	function sendResponse(evt, success, error, errorCode, data) {
		try {
			if (!evt || !evt.source) {
				log('error', 'Cannot send response: invalid event or source');
				return;
			}

			var response = {
				event: 'generateMermaid',
				status: success ? 'ok' : 'error'
			};

			if (!success) {
				response.error = error || 'Unknown error';
				response.errorCode = errorCode || ERROR_CODES.PARSE_ERROR;
			}

			if (data) {
				response.data = data;
			}

			evt.source.postMessage(JSON.stringify(response), '*');

			if (success) {
				log('debug', 'Success response sent', response);
			} else {
				log('error', 'Error response sent', response);
			}
		} catch (e) {
			log('error', 'Failed to send response', e);
		}
	}

	/**
	 * Send style modification response message back to sender
	 * @param {MessageEvent} evt - Original message event
	 * @param {boolean} success - Whether operation was successful
	 * @param {string} error - Error message (optional)
	 * @param {string} errorCode - Error code (optional)
	 * @param {Object} data - Additional data (optional)
	 */
	function sendStyleResponse(evt, success, error, errorCode, data) {
		try {
			if (!evt || !evt.source) {
				log('error', 'Cannot send style response: invalid event or source');
				return;
			}

			var response = {
				event: 'modifyStyle',
				status: success ? 'ok' : 'error'
			};

			if (!success) {
				response.error = error || 'Unknown error';
				response.errorCode = errorCode || STYLE_ERROR_CODES.INVALID_FORMAT;
			}

			if (data) {
				response.data = data;
			}

			evt.source.postMessage(JSON.stringify(response), '*');

			if (success) {
				log('debug', 'Style response sent', response);
			} else {
				log('error', 'Style error response sent', response);
			}
		} catch (e) {
			log('error', 'Failed to send style response', e);
		}
	}

	// ========================================
	// Mermaid Parsing Functions
	// ========================================
	
	/**
	 * Parse Mermaid text with timeout
	 * @param {string} mermaidText - Mermaid text to parse
	 * @param {number} timeout - Timeout in milliseconds
	 * @param {boolean} editable - If true, convert to native Draw.io shapes; if false, keep as Mermaid data
	 * @returns {Promise<string>} Promise that resolves with XML data
	 */
	function parseMermaidWithTimeout(mermaidText, timeout, editable) {
		return measurePerformanceAsync('Mermaid Parsing', function() {
			return new Promise(function(resolve, reject) {
				var timer = setTimeout(function() {
					reject({
						message: 'Parse timeout after ' + timeout + 'ms',
						code: ERROR_CODES.TIMEOUT
					});
				}, timeout);

				// Use enableParser parameter to control output type:
				// - true: converts to native Draw.io shapes (editable)
				// - false: keeps as Mermaid data (not editable, but preserves Mermaid source)
				var enableParser = editable !== false; // default to true (editable)
				
				ui.parseMermaidDiagram(mermaidText, null,
					function(xml) {
						clearTimeout(timer);
						resolve(xml);
					},
					function(err) {
						clearTimeout(timer);
						reject({
							message: err && err.message ? err.message : String(err),
							code: ERROR_CODES.PARSE_ERROR
						});
					},
					null,  // parseErrorHandler
					enableParser
				);
			});
		});
	}

	// ========================================
	// Canvas Insertion Functions
	// ========================================
	
	/**
	 * Insert diagram into canvas
	 * @param {string} xml - XML data to insert
	 * @param {Object} options - Insertion options
	 * @returns {{cellCount: number}} Result object
	 */
	function insertDiagram(xml, options) {
		return measurePerformance('Canvas Insertion', function() {
			options = options || {};
			
			var graph = ui.editor.graph;
			var model = graph.getModel();
			
			// Calculate position
			var dx = options.position && typeof options.position.x === 'number' ? options.position.x : 20;
			var dy = options.position && typeof options.position.y === 'number' ? options.position.y : 20;
			
			// Begin update for batch operations
			model.beginUpdate();
			try {
				// Insert XML
				var cells = ui.importXml(xml, dx, dy, true);
				
				if (!cells || cells.length === 0) {
					throw {
						message: 'No cells were inserted',
						code: ERROR_CODES.INSERT_FAILED
					};
				}
				
				// Unlock cells to make them editable
				// Get all cells including descendants
				var allCells = [];
				for (var i = 0; i < cells.length; i++) {
					allCells.push(cells[i]);
					var descendants = model.getDescendants(cells[i]);
					if (descendants && descendants.length > 0) {
						allCells = allCells.concat(descendants);
					}
				}
				
				// Set editable styles on all cells
				if (allCells.length > 0) {
					graph.setCellStyles('locked', '0', allCells);
					graph.setCellStyles(mxConstants.STYLE_MOVABLE, '1', allCells);
					graph.setCellStyles(mxConstants.STYLE_RESIZABLE, '1', allCells);
					graph.setCellStyles(mxConstants.STYLE_EDITABLE, '1', allCells);
					graph.setCellStyles(mxConstants.STYLE_DELETABLE, '1', allCells);
					graph.setCellStyles(mxConstants.STYLE_ROTATABLE, '1', allCells);
					graph.setCellStyles('connectable', '1', allCells);
				}
				
				// Apply scale if specified
				if (options.scale && typeof options.scale === 'number') {
					var scale = Math.max(0.1, Math.min(10.0, options.scale));
					graph.scaleCells(cells, scale, scale);
				}
				
				// Select cells if requested (default true)
				if (options.select !== false) {
					graph.setSelectionCells(cells);
				}
				
				// Center view if requested
				if (options.center) {
					graph.scrollCellToVisible(cells[0]);
				}
				
				// Mark as modified
				if (ui.editor && typeof ui.editor.setModified === 'function') {
					ui.editor.setModified(true);
				}
				
				return { cellCount: cells.length };
			} finally {
				model.endUpdate();
			}
		});
	}

	// ========================================
	// Main Handler for modifyStyle
	// ========================================
	
	/**
	 * Handle modifyStyle action
	 * @param {MessageEvent} evt - Message event
	 * @param {Object} data - Parsed message data
	 */
	function handleModifyStyle(evt, data) {
		log('debug', 'Handling modifyStyle action');

		// 1. Validate message format
		var validation = validateStyleMessage(data);
		if (!validation.valid) {
			log('error', 'Style message validation failed', validation);
			sendStyleResponse(evt, false, validation.error, validation.errorCode);
			return;
		}

		log('debug', 'Style message validation passed');

		// 2. Get target cells
		var graph = ui.editor.graph;
		var targetResult = getTargetCells(data.target, graph);
		
		if (targetResult.error) {
			log('error', 'Failed to get target cells', targetResult);
			sendStyleResponse(evt, false, targetResult.error, targetResult.errorCode);
			return;
		}

		var cells = targetResult.cells;
		log('debug', 'Target cells retrieved', { cellCount: cells.length });

		// 3. Apply style modifications
		try {
			log('debug', 'Applying style modifications', {
				styles: data.styles,
				operations: data.operations,
				cellCount: cells.length
			});

			var result = applyStyleModifications(cells, data.styles, data.operations, graph);

			// 4. Mark document as modified
			if (result.modifiedCount > 0) {
				if (ui.editor && typeof ui.editor.setModified === 'function') {
					ui.editor.setModified(true);
				}
			}

			// 5. Send response message
			if (result.success) {
				log('info', 'Style modification completed successfully', {
					modifiedCount: result.modifiedCount
				});
				
				sendStyleResponse(evt, true, null, null, {
					modifiedCount: result.modifiedCount
				});
			} else {
				// Partial success - some properties failed but some succeeded
				log('warn', 'Style modification completed with errors', {
					modifiedCount: result.modifiedCount,
					errors: result.errors
				});
				
				sendStyleResponse(evt, true, null, null, {
					modifiedCount: result.modifiedCount,
					errors: result.errors
				});
			}

		} catch (e) {
			log('error', 'Style modification failed', e);
			sendStyleResponse(evt, false, e.message || 'Style modification failed', STYLE_ERROR_CODES.INVALID_FORMAT);
		}
	}

	// ========================================
	// Main Handler for generateMermaid
	// ========================================
	
	/**
	 * Handle generateMermaid action
	 * @param {MessageEvent} evt - Message event
	 * @param {Object} data - Parsed message data
	 */
	function handleGenerateMermaid(evt, data) {
		var overallStartTime = CONFIG.DEBUG_MODE ? performance.now() : 0;
		
		log('debug', 'Handling generateMermaid action');

		// 1. Check browser compatibility
		var compatibility = checkBrowserCompatibility();
		if (!compatibility.supported) {
			log('error', 'Browser compatibility check failed', compatibility);
			sendResponse(evt, false, compatibility.error, compatibility.errorCode);
			return;
		}

		log('debug', 'Browser compatibility check passed');

		// 2. Validate message
		var validation = validateMessage(evt, data);
		if (!validation.valid) {
			log('error', 'Validation failed', validation);
			sendResponse(evt, false, validation.error, validation.errorCode);
			return;
		}

		log('debug', 'Message validation passed');

		// 3. Parse Mermaid
		var mermaid = data.mermaid;
		var options = data.options || {};
		
		// editable option: true = native Draw.io shapes, false = Mermaid data
		// Default is true (editable/native shapes)
		var editable = options.editable !== false;

		log('debug', 'Starting Mermaid parsing', {
			mermaidLength: mermaid.length,
			options: options,
			editable: editable
		});

		parseMermaidWithTimeout(mermaid, CONFIG.PARSE_TIMEOUT, editable)
			.then(function(xml) {
				log('debug', 'Mermaid parsing successful');

				// 4. Insert diagram into canvas
				try {
					log('debug', 'Inserting diagram into canvas', options);
					
					var result = insertDiagram(xml, options);
					
					log('info', 'Diagram inserted successfully', {
						cellCount: result.cellCount
					});

					// Log overall performance
					if (CONFIG.DEBUG_MODE) {
						var overallDuration = performance.now() - overallStartTime;
						log('debug', 'Performance: Total operation completed in ' + overallDuration.toFixed(2) + 'ms', {
							operation: 'Total generateMermaid',
							duration: overallDuration
						});
					}

					// 5. Send success response
					sendResponse(evt, true, null, null, {
						cellCount: result.cellCount
					});

				} catch (insertError) {
					log('error', 'Failed to insert diagram', insertError);
					sendResponse(
						evt,
						false,
						insertError.message || 'Failed to insert diagram',
						insertError.code || ERROR_CODES.INSERT_FAILED
					);
				}
			})
			.catch(function(parseError) {
				log('error', 'Mermaid parsing failed', parseError);
				
				var errorCode = parseError.code || ERROR_CODES.PARSE_ERROR;
				var errorMessage = parseError.message || 'Failed to parse Mermaid diagram';
				
				sendResponse(evt, false, errorMessage, errorCode);
			});
	}

	// ========================================
	// Legacy Functions (Backward Compatibility)
	// ========================================
	
	/**
	 * Legacy response function for importMermaid and insertMermaid
	 * @param {MessageEvent} evt - Message event
	 * @param {string} eventName - Event name
	 * @param {boolean} ok - Success flag
	 * @param {string} err - Error message
	 */
	function postResult(evt, eventName, ok, err)
	{
		try
		{
			if (evt != null && evt.source != null)
			{
				evt.source.postMessage(JSON.stringify({
					event: eventName,
					success: ok,
					error: err || null,
				}), '*');
			}
		}
		catch (e)
		{
			// Ignore postMessage errors.
		}
	}

	/**
	 * Legacy handler for importMermaid and insertMermaid actions
	 * @param {MessageEvent} evt - Message event
	 * @param {Object} action - Action data
	 * @param {string} eventName - Event name
	 * @param {Function} applyFn - Function to apply XML
	 */
	function handleMermaid(evt, action, eventName, applyFn)
	{
		var mermaid = action.mermaid || action.data || '';
		var options = action.options || {};
		var editable = options.editable === true;

		if (typeof mermaid !== 'string' || mermaid.trim() === '')
		{
			postResult(evt, eventName, false, 'Missing mermaid payload');
			return;
		}

		ui.parseMermaidDiagram(mermaid, null, function(xml)
		{
			try
			{
				applyFn(xml);
				if (ui.editor && typeof ui.editor.setModified === 'function')
				{
					ui.editor.setModified(true);
				}
				postResult(evt, eventName, true, null);
			}
			catch (e)
			{
				postResult(evt, eventName, false, e && e.message ? e.message : String(e));
			}
		}, function(err)
		{
			postResult(evt, eventName, false, err && err.message ? err.message : String(err));
		}, null, editable);
	}

	// ========================================
	// Message Router
	// ========================================
	
	/**
	 * Main message handler - routes to appropriate action handler
	 * @param {MessageEvent} evt - Message event
	 */
	function handleMessage(evt)
	{
		var data = evt.data;

		// Parse JSON if needed
		if (typeof data === 'string')
		{
			try
			{
				data = JSON.parse(data);
			}
			catch (e)
			{
				return;
			}
		}

		// Validate basic message structure
		if (data == null || data.action == null)
		{
			return;
		}

		// Route to modifyStyle handler (new action)
		if (data.action === 'modifyStyle')
		{
			handleModifyStyle(evt, data);
			return;
		}

		// Route to generateMermaid handler (new action)
		if (data.action === 'generateMermaid')
		{
			handleGenerateMermaid(evt, data);
			return;
		}

		// Preserve existing importMermaid action
		if (data.action === 'importMermaid')
		{
			handleMermaid(evt, data, 'importMermaid', function(xml)
			{
				ui.setFileData(xml);
			});
			return;
		}

		// Preserve existing insertMermaid action
		if (data.action === 'insertMermaid')
		{
			handleMermaid(evt, data, 'insertMermaid', function(xml)
			{
				var cells = ui.importXml(xml, 20, 20, true);
				if (cells != null && ui.editor && ui.editor.graph)
				{
					ui.editor.graph.setSelectionCells(cells);
				}
			});
			return;
		}
	}

	// ========================================
	// Initialization
	// ========================================
	
	// Log plugin loading immediately
	console.log('[MermaidImport] Plugin loading...');
	
	// Register message listener
	window.addEventListener('message', handleMessage);
	
	// Notify parent window that plugin is ready
	try
	{
		if (window.parent)
		{
			window.parent.postMessage(JSON.stringify({event: 'mermaid-import-ready'}), '*');
		}
	}
	catch (e)
	{
		// Ignore postMessage errors.
	}

	console.log('[MermaidImport] Plugin initialized successfully');
	log('info', 'Mermaid import plugin initialized', {
		config: CONFIG
	});
});
