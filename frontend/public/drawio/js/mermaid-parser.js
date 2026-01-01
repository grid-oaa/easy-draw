/**
 * Mermaid Parser Wrapper
 * Provides timeout control and error handling for Mermaid diagram parsing
 */

(function() {
	'use strict';

	// Configuration
	var PARSE_TIMEOUT = 10000; // 10 seconds

	// Error codes
	var ERROR_CODES = {
		PARSE_ERROR: 'PARSE_ERROR',
		TIMEOUT: 'TIMEOUT',
		UNSUPPORTED_TYPE: 'UNSUPPORTED_TYPE'
	};

	/**
	 * Parse Mermaid diagram with timeout control
	 * @param {Object} ui - Draw.io UI object
	 * @param {string} mermaidText - Mermaid syntax text
	 * @param {number} [timeout] - Timeout in milliseconds (default: 10000)
	 * @returns {Promise<string>} Promise that resolves with XML data or rejects with error
	 */
	function parseMermaidWithTimeout(ui, mermaidText, timeout) {
		// Use default timeout if not specified
		if (timeout === undefined || timeout === null) {
			timeout = PARSE_TIMEOUT;
		}

		return new Promise(function(resolve, reject) {
			var timeoutId = null;
			var completed = false;

			// Set up timeout
			timeoutId = setTimeout(function() {
				if (!completed) {
					completed = true;
					reject({
						message: 'Mermaid parsing timed out after ' + timeout + 'ms',
						code: ERROR_CODES.TIMEOUT,
						timeout: timeout
					});
				}
			}, timeout);

			// Call the actual parser
			try {
				ui.parseMermaidDiagram(
					mermaidText,
					null,
					function(xml) {
						// Success callback
						if (!completed) {
							completed = true;
							clearTimeout(timeoutId);
							resolve(xml);
						}
					},
					function(err) {
						// Error callback
						if (!completed) {
							completed = true;
							clearTimeout(timeoutId);
							
							// Extract error information
							var errorInfo = extractErrorInfo(err);
							reject(errorInfo);
						}
					}
				);
			} catch (e) {
				// Handle synchronous errors
				if (!completed) {
					completed = true;
					clearTimeout(timeoutId);
					
					var errorInfo = extractErrorInfo(e);
					reject(errorInfo);
				}
			}
		});
	}

	/**
	 * Extract detailed error information from parser error
	 * @param {*} err - Error object or message
	 * @returns {Object} Detailed error object
	 */
	function extractErrorInfo(err) {
		var errorInfo = {
			message: 'Unknown error',
			code: ERROR_CODES.PARSE_ERROR,
			details: null,
			position: null
		};

		// Handle different error types
		if (err === null || err === undefined) {
			errorInfo.message = 'Unknown parsing error';
			return errorInfo;
		}

		// If err is a string
		if (typeof err === 'string') {
			errorInfo.message = err;
			errorInfo.details = err;
			
			// Try to extract position information from error message
			var positionMatch = err.match(/line\s+(\d+)|position\s+(\d+)|at\s+(\d+)/i);
			if (positionMatch) {
				errorInfo.position = {
					line: positionMatch[1] ? parseInt(positionMatch[1], 10) : null,
					position: positionMatch[2] || positionMatch[3] ? 
						parseInt(positionMatch[2] || positionMatch[3], 10) : null
				};
			}
			
			return errorInfo;
		}

		// If err is an Error object
		if (err instanceof Error) {
			errorInfo.message = err.message || 'Parsing error';
			errorInfo.details = err.message;
			
			// Check for error code on Error object
			if (err.code) {
				errorInfo.code = err.code;
			}
			
			// Include stack trace if available
			if (err.stack) {
				errorInfo.stack = err.stack;
			}
			
			// Try to extract position from error message
			var posMatch = err.message.match(/line\s+(\d+)|position\s+(\d+)|at\s+(\d+)/i);
			if (posMatch) {
				errorInfo.position = {
					line: posMatch[1] ? parseInt(posMatch[1], 10) : null,
					position: posMatch[2] || posMatch[3] ? 
						parseInt(posMatch[2] || posMatch[3], 10) : null
				};
			}
			
			return errorInfo;
		}

		// If err is an object with message property
		if (typeof err === 'object' && err.message) {
			errorInfo.message = err.message;
			errorInfo.details = err.message;
			
			// Check for position information in the error object
			if (err.line !== undefined) {
				errorInfo.position = errorInfo.position || {};
				errorInfo.position.line = err.line;
			}
			if (err.position !== undefined) {
				errorInfo.position = errorInfo.position || {};
				errorInfo.position.position = err.position;
			}
			if (err.column !== undefined) {
				errorInfo.position = errorInfo.position || {};
				errorInfo.position.column = err.column;
			}
			
			// Check for error code
			if (err.code) {
				errorInfo.code = err.code;
			}
			
			// Include stack if available
			if (err.stack) {
				errorInfo.stack = err.stack;
			}
			
			return errorInfo;
		}

		// If err is some other object, convert to string
		if (typeof err === 'object') {
			try {
				errorInfo.message = JSON.stringify(err);
				errorInfo.details = err;
			} catch (e) {
				errorInfo.message = String(err);
				errorInfo.details = String(err);
			}
			return errorInfo;
		}

		// Fallback: convert to string
		errorInfo.message = String(err);
		errorInfo.details = String(err);
		return errorInfo;
	}

	// Export functions
	if (typeof window !== 'undefined') {
		window.MermaidParser = {
			parseMermaidWithTimeout: parseMermaidWithTimeout,
			extractErrorInfo: extractErrorInfo,
			ERROR_CODES: ERROR_CODES,
			PARSE_TIMEOUT: PARSE_TIMEOUT
		};
	}

	// Also export for Node.js testing environment
	if (typeof module !== 'undefined' && module.exports) {
		module.exports = {
			parseMermaidWithTimeout: parseMermaidWithTimeout,
			extractErrorInfo: extractErrorInfo,
			ERROR_CODES: ERROR_CODES,
			PARSE_TIMEOUT: PARSE_TIMEOUT
		};
	}
})();
