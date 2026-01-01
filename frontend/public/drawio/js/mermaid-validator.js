/**
 * Message validator for Mermaid iframe integration
 * Provides validation functions for postMessage security and format checking
 */

(function() {
	'use strict';

	// Default configuration
	var DEFAULT_CONFIG = {
		MAX_MESSAGE_SIZE: 1024 * 1024, // 1MB
		PARSE_TIMEOUT: 10000,           // 10 seconds
		DEBUG_MODE: false,              // Debug mode
		ALLOWED_ORIGINS: ['*']          // Allowed origins ('*' means all)
	};

	/**
	 * Read configuration from URL parameters
	 * @returns {Object} Configuration object from URL parameters
	 */
	function readConfigFromURL() {
		var config = {};
		
		try {
			// Check if URLSearchParams is available
			if (typeof URLSearchParams === 'undefined' || typeof window === 'undefined') {
				return config;
			}

			var urlParams = new URLSearchParams(window.location.search);

			// Read MAX_MESSAGE_SIZE
			var maxMessageSize = urlParams.get('maxMessageSize');
			if (maxMessageSize !== null) {
				var size = parseInt(maxMessageSize, 10);
				if (!isNaN(size) && size > 0) {
					config.MAX_MESSAGE_SIZE = size;
				}
			}

			// Read PARSE_TIMEOUT
			var parseTimeout = urlParams.get('parseTimeout');
			if (parseTimeout !== null) {
				var timeout = parseInt(parseTimeout, 10);
				if (!isNaN(timeout) && timeout > 0) {
					config.PARSE_TIMEOUT = timeout;
				}
			}

			// Read DEBUG_MODE
			var debugMode = urlParams.get('debugMode');
			if (debugMode !== null) {
				config.DEBUG_MODE = debugMode === 'true' || debugMode === '1';
			}

			// Read ALLOWED_ORIGINS
			var allowedOrigins = urlParams.get('allowedOrigins');
			if (allowedOrigins !== null && allowedOrigins.length > 0) {
				// Split by comma and trim whitespace
				config.ALLOWED_ORIGINS = allowedOrigins.split(',').map(function(origin) {
					return origin.trim();
				}).filter(function(origin) {
					return origin.length > 0;
				});
			}
		} catch (e) {
			console.warn('[MermaidValidator] Failed to read URL parameters:', e);
		}

		return config;
	}

	/**
	 * Read configuration from global window object
	 * @returns {Object} Configuration object from global config
	 */
	function readConfigFromGlobal() {
		var config = {};

		try {
			// Check if window and DRAWIO_CONFIG exist
			if (typeof window === 'undefined' || !window.DRAWIO_CONFIG) {
				return config;
			}

			var globalConfig = window.DRAWIO_CONFIG;

			// Read MAX_MESSAGE_SIZE
			if (typeof globalConfig.maxMessageSize === 'number' && globalConfig.maxMessageSize > 0) {
				config.MAX_MESSAGE_SIZE = globalConfig.maxMessageSize;
			}

			// Read PARSE_TIMEOUT
			if (typeof globalConfig.parseTimeout === 'number' && globalConfig.parseTimeout > 0) {
				config.PARSE_TIMEOUT = globalConfig.parseTimeout;
			}

			// Read DEBUG_MODE
			if (typeof globalConfig.debugMode === 'boolean') {
				config.DEBUG_MODE = globalConfig.debugMode;
			}

			// Read ALLOWED_ORIGINS
			if (Array.isArray(globalConfig.allowedOrigins) && globalConfig.allowedOrigins.length > 0) {
				config.ALLOWED_ORIGINS = globalConfig.allowedOrigins.filter(function(origin) {
					return typeof origin === 'string' && origin.length > 0;
				});
			}
		} catch (e) {
			console.warn('[MermaidValidator] Failed to read global configuration:', e);
		}

		return config;
	}

	/**
	 * Initialize configuration by merging default, global, and URL configs
	 * Priority: URL parameters > Global config > Default config
	 * @returns {Object} Final configuration object
	 */
	function initializeConfig() {
		var config = {};

		// Start with default config
		for (var key in DEFAULT_CONFIG) {
			if (DEFAULT_CONFIG.hasOwnProperty(key)) {
				config[key] = DEFAULT_CONFIG[key];
			}
		}

		// Merge global config (overrides defaults)
		var globalConfig = readConfigFromGlobal();
		for (var key in globalConfig) {
			if (globalConfig.hasOwnProperty(key)) {
				config[key] = globalConfig[key];
			}
		}

		// Merge URL config (overrides global and defaults)
		var urlConfig = readConfigFromURL();
		for (var key in urlConfig) {
			if (urlConfig.hasOwnProperty(key)) {
				config[key] = urlConfig[key];
			}
		}

		return config;
	}

	// Initialize configuration
	var CONFIG = initializeConfig();

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

	/**
	 * Validate basic message format
	 * @param {MessageEvent} evt - Message event
	 * @param {Object} data - Parsed message data
	 * @returns {{valid: boolean, error?: string, errorCode?: string}}
	 */
	function validateMessageFormat(evt, data) {
		// Check if data exists
		if (!data || typeof data !== 'object') {
			return {
				valid: false,
				error: 'Invalid message format: data must be an object',
				errorCode: ERROR_CODES.INVALID_FORMAT
			};
		}

		// Check if action field exists
		if (!data.action || typeof data.action !== 'string') {
			return {
				valid: false,
				error: 'Invalid message format: missing or invalid action field',
				errorCode: ERROR_CODES.INVALID_FORMAT
			};
		}

		// Check if mermaid field exists for generateMermaid action
		if (data.action === 'generateMermaid') {
			if (data.mermaid === undefined || data.mermaid === null) {
				return {
					valid: false,
					error: 'Invalid message format: missing mermaid field',
					errorCode: ERROR_CODES.INVALID_FORMAT
				};
			}

			// Check if mermaid field is a string
			if (typeof data.mermaid !== 'string') {
				return {
					valid: false,
					error: 'Invalid message format: mermaid field must be a string',
					errorCode: ERROR_CODES.INVALID_FORMAT
				};
			}
		}

		return { valid: true };
	}

	/**
	 * Validate that mermaid field is not empty or whitespace-only
	 * @param {Object} data - Parsed message data
	 * @returns {{valid: boolean, error?: string, errorCode?: string}}
	 */
	function validateMermaidNotEmpty(data) {
		if (data.action === 'generateMermaid') {
			var mermaid = data.mermaid;
			
			// Check if empty or whitespace-only
			if (mermaid.trim().length === 0) {
				return {
					valid: false,
					error: 'Mermaid text cannot be empty or contain only whitespace',
					errorCode: ERROR_CODES.EMPTY_MERMAID
				};
			}
		}

		return { valid: true };
	}

	/**
	 * Check if origin is allowed
	 * @param {string} origin - Message origin
	 * @returns {boolean}
	 */
	function isOriginAllowed(origin) {
		// If wildcard is in the list, allow all origins
		if (CONFIG.ALLOWED_ORIGINS.indexOf('*') !== -1) {
			return true;
		}

		// Check if origin is in the allowed list
		return CONFIG.ALLOWED_ORIGINS.indexOf(origin) !== -1;
	}

	/**
	 * Validate message origin
	 * @param {MessageEvent} evt - Message event
	 * @returns {{valid: boolean, error?: string, errorCode?: string}}
	 */
	function validateOrigin(evt) {
		if (!evt || !evt.origin) {
			return {
				valid: false,
				error: 'Invalid message event: missing origin',
				errorCode: ERROR_CODES.ORIGIN_DENIED
			};
		}

		if (!isOriginAllowed(evt.origin)) {
			return {
				valid: false,
				error: 'Message origin not allowed: ' + evt.origin,
				errorCode: ERROR_CODES.ORIGIN_DENIED
			};
		}

		return { valid: true };
	}

	/**
	 * Check message size
	 * @param {Object} data - Message data
	 * @returns {boolean}
	 */
	function checkMessageSize(data) {
		try {
			var size = JSON.stringify(data).length;
			return size <= CONFIG.MAX_MESSAGE_SIZE;
		} catch (e) {
			return false;
		}
	}

	/**
	 * Validate message size
	 * @param {Object} data - Parsed message data
	 * @returns {{valid: boolean, error?: string, errorCode?: string}}
	 */
	function validateMessageSize(data) {
		if (!checkMessageSize(data)) {
			return {
				valid: false,
				error: 'Message size exceeds maximum allowed size of ' + CONFIG.MAX_MESSAGE_SIZE + ' bytes',
				errorCode: ERROR_CODES.SIZE_EXCEEDED
			};
		}

		return { valid: true };
	}

	/**
	 * Check if text contains XSS attack vectors
	 * @param {string} text - Text to check
	 * @returns {boolean}
	 */
	function containsXSS(text) {
		if (typeof text !== 'string') {
			return false;
		}

		var xssPatterns = [
			/<script[\s\S]*?>[\s\S]*?<\/script>/gi,
			/javascript:/gi,
			/on\s*\w+\s*=/gi,  // onclick, onload, on click, on load, etc. (with optional spaces)
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
	 * Validate message content for XSS
	 * @param {Object} data - Parsed message data
	 * @returns {{valid: boolean, error?: string, errorCode?: string}}
	 */
	function validateXSS(data) {
		if (data.action === 'generateMermaid' && data.mermaid) {
			if (containsXSS(data.mermaid)) {
				return {
					valid: false,
					error: 'Potential XSS attack detected in mermaid text',
					errorCode: ERROR_CODES.XSS_DETECTED
				};
			}
		}

		return { valid: true };
	}

	/**
	 * Main validation function - validates all aspects of the message
	 * @param {MessageEvent} evt - Message event
	 * @param {Object} data - Parsed message data
	 * @returns {{valid: boolean, error?: string, errorCode?: string}}
	 */
	function validateMessage(evt, data) {
		// Validate message format
		var formatResult = validateMessageFormat(evt, data);
		if (!formatResult.valid) {
			return formatResult;
		}

		// Validate origin
		var originResult = validateOrigin(evt);
		if (!originResult.valid) {
			return originResult;
		}

		// Validate message size
		var sizeResult = validateMessageSize(data);
		if (!sizeResult.valid) {
			return sizeResult;
		}

		// Validate mermaid field is not empty
		var emptyResult = validateMermaidNotEmpty(data);
		if (!emptyResult.valid) {
			return emptyResult;
		}

		// Validate XSS
		var xssResult = validateXSS(data);
		if (!xssResult.valid) {
			return xssResult;
		}

		return { valid: true };
	}

	/**
	 * Log a message with specified level
	 * @param {string} level - Log level: 'error', 'warn', 'info', or 'debug'
	 * @param {string} message - Log message
	 * @param {*} [context] - Optional context data (object, error, etc.)
	 */
	function log(level, message, context) {
		// Skip debug logs if debug mode is disabled
		if (!CONFIG.DEBUG_MODE && level === 'debug') {
			return;
		}

		// Validate level parameter
		var validLevels = ['error', 'warn', 'info', 'debug'];
		if (validLevels.indexOf(level) === -1) {
			console.error('[MermaidValidator] Invalid log level:', level);
			return;
		}

		// Format timestamp
		var timestamp = new Date().toISOString();

		// Format log message
		var logMessage = '[' + timestamp + '] [' + level.toUpperCase() + '] ' + message;

		// Output log based on level
		if (level === 'error') {
			if (context !== undefined) {
				console.error(logMessage, context);
			} else {
				console.error(logMessage);
			}
		} else if (level === 'warn') {
			if (context !== undefined) {
				console.warn(logMessage, context);
			} else {
				console.warn(logMessage);
			}
		} else {
			// 'info' and 'debug' use console.log
			if (context !== undefined) {
				console.log(logMessage, context);
			} else {
				console.log(logMessage);
			}
		}
	}

	/**
	 * Send response message back to the sender
	 * @param {MessageEvent} evt - Original message event (used to get source)
	 * @param {boolean} success - Whether the operation was successful
	 * @param {string} [error] - Error message (optional, for error responses)
	 * @param {string} [errorCode] - Error code (optional, for error responses)
	 * @param {Object} [data] - Additional data (optional, for success responses)
	 */
	function sendResponse(evt, success, error, errorCode, data) {
		// Validate evt parameter
		if (!evt || !evt.source) {
			console.error('[MermaidValidator] Cannot send response: invalid event or missing source');
			return;
		}

		// Construct response message
		var response = {
			event: 'generateMermaid',
			status: success ? 'ok' : 'error'
		};

		// Add error information for error responses
		if (!success) {
			if (error) {
				response.error = error;
			}
			if (errorCode) {
				response.errorCode = errorCode;
			}
		}

		// Add additional data for success responses
		if (success && data) {
			response.data = data;
		}

		// Send response using postMessage
		try {
			// Get target origin from the original event
			var targetOrigin = evt.origin || '*';
			
			// Send the response
			evt.source.postMessage(JSON.stringify(response), targetOrigin);
			
			// Log success in debug mode
			if (CONFIG.DEBUG_MODE) {
				console.log('[MermaidValidator] Response sent successfully:', response);
			}
		} catch (e) {
			// Handle postMessage exceptions
			console.error('[MermaidValidator] Failed to send response:', e);
			console.error('[MermaidValidator] Response that failed to send:', response);
		}
	}

	// Export functions
	if (typeof window !== 'undefined') {
		window.MermaidValidator = {
			validateMessage: validateMessage,
			validateMessageFormat: validateMessageFormat,
			validateMermaidNotEmpty: validateMermaidNotEmpty,
			validateOrigin: validateOrigin,
			validateMessageSize: validateMessageSize,
			validateXSS: validateXSS,
			isOriginAllowed: isOriginAllowed,
			containsXSS: containsXSS,
			sendResponse: sendResponse,
			log: log,
			CONFIG: CONFIG,
			ERROR_CODES: ERROR_CODES,
			// Export config functions for testing
			readConfigFromURL: readConfigFromURL,
			readConfigFromGlobal: readConfigFromGlobal,
			initializeConfig: initializeConfig,
			DEFAULT_CONFIG: DEFAULT_CONFIG
		};
	}
})();
