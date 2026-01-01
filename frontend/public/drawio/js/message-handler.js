/**
 * Main Message Handler for Mermaid iframe integration
 * Coordinates validation, parsing, and canvas insertion
 */

(function() {
	'use strict';

	/**
	 * Handle generateMermaid action
	 * @param {Object} ui - Draw.io UI object
	 * @param {MessageEvent} evt - Message event
	 * @param {Object} data - Parsed message data
	 */
	function handleGenerateMermaid(ui, evt, data) {
		// Get validator and logger
		var validator = window.MermaidValidator;
		var parser = window.MermaidParser;
		var inserter = window.CanvasInserter;

		if (!validator || !parser || !inserter) {
			console.error('[MessageHandler] Required modules not loaded');
			return;
		}

		// 1. Validate message
		validator.log('debug', 'Validating generateMermaid message');
		var validation = validator.validateMessage(evt, data);
		
		if (!validation.valid) {
			validator.log('error', 'Message validation failed', {
				error: validation.error,
				errorCode: validation.errorCode
			});
			validator.sendResponse(evt, false, validation.error, validation.errorCode);
			return;
		}

		validator.log('debug', 'Message validation passed');

		// 2. Parse Mermaid
		var mermaid = data.mermaid;
		var options = data.options || {};

		validator.log('debug', 'Starting Mermaid parsing', {
			mermaidLength: mermaid.length,
			options: options
		});

		// Parse with timeout
		parser.parseMermaidWithTimeout(ui, mermaid, parser.PARSE_TIMEOUT)
			.then(function(xml) {
				validator.log('debug', 'Mermaid parsing successful');

				// 3. Insert diagram into canvas
				try {
					validator.log('debug', 'Inserting diagram into canvas', {
						position: options.position,
						scale: options.scale,
						select: options.select
					});

					var result = inserter.insertDiagram(ui, xml, {
						position: options.position,
						scale: options.scale,
						select: options.select,
						center: options.center
					});

					validator.log('info', 'Diagram inserted successfully', {
						cellCount: result.cellCount
					});

					// 4. Send success response
					validator.sendResponse(evt, true, null, null, {
						cellCount: result.cellCount
					});

				} catch (insertError) {
					validator.log('error', 'Failed to insert diagram', insertError);
					validator.sendResponse(
						evt,
						false,
						insertError.message || 'Failed to insert diagram',
						insertError.code || validator.ERROR_CODES.INSERT_FAILED
					);
				}
			})
			.catch(function(parseError) {
				validator.log('error', 'Mermaid parsing failed', parseError);
				
				// Determine error code
				var errorCode = parseError.code || validator.ERROR_CODES.PARSE_ERROR;
				var errorMessage = parseError.message || 'Failed to parse Mermaid diagram';
				
				// Include position information if available
				if (parseError.position) {
					errorMessage += ' (at line ' + (parseError.position.line || 'unknown') + ')';
				}

				validator.sendResponse(evt, false, errorMessage, errorCode);
			});
	}

	// Export function
	if (typeof window !== 'undefined') {
		window.MessageHandler = {
			handleGenerateMermaid: handleGenerateMermaid
		};
	}

	// Also export for Node.js testing environment
	if (typeof module !== 'undefined' && module.exports) {
		module.exports = {
			handleGenerateMermaid: handleGenerateMermaid
		};
	}
})();
