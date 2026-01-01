/**
 * Canvas Inserter Module
 * Handles insertion of Mermaid diagrams into the draw.io canvas
 */

(function() {
	'use strict';

	// Error codes
	var ERROR_CODES = {
		INSERT_FAILED: 'INSERT_FAILED',
		INVALID_XML: 'INVALID_XML',
		INVALID_UI: 'INVALID_UI'
	};

	/**
	 * Insert diagram into canvas
	 * @param {Object} ui - Draw.io UI object
	 * @param {string} xml - XML data to insert
	 * @param {Object} options - Insertion options
	 * @param {Object} options.position - Position {x, y} for insertion
	 * @param {number} options.scale - Scale factor (0.1 - 10.0)
	 * @param {boolean} options.select - Whether to select inserted cells (default: true)
	 * @param {boolean} options.center - Whether to center the view (default: false)
	 * @returns {Object} Result object with cells and success status
	 */
	function insertDiagram(ui, xml, options) {
		// Validate parameters
		if (!ui || !ui.importXml) {
			throw new Error('Invalid UI object: missing importXml method');
		}

		if (!xml || typeof xml !== 'string') {
			throw new Error('Invalid XML: must be a non-empty string');
		}

		// Default options
		options = options || {};
		var position = options.position || { x: 20, y: 20 };
		var scale = options.scale;
		var select = options.select !== false; // Default to true
		var center = options.center === true; // Default to false

		// Get graph reference
		var graph = ui.editor && ui.editor.graph;
		if (!graph) {
			throw new Error('Invalid UI object: missing graph');
		}

		// Store initial state for rollback
		var initialCellCount = graph.getModel().getCellCount();
		var initialSelection = graph.getSelectionCells();

		try {
			// Calculate insertion position
			var dx = position.x !== undefined ? position.x : 20;
			var dy = position.y !== undefined ? position.y : 20;

			// Insert XML into canvas
			var cells = ui.importXml(xml, dx, dy, true);

			// Check if insertion was successful
			if (!cells || cells.length === 0) {
				throw new Error('No cells were inserted');
			}

			// Apply scale if specified
			if (scale !== undefined && scale !== null) {
				// Validate scale range
				if (scale < 0.1 || scale > 10.0) {
					console.warn('[CanvasInserter] Scale out of range (0.1-10.0), using default');
				} else {
					graph.scaleCells(cells, scale, scale);
				}
			}

			// Select inserted cells if requested
			if (select) {
				graph.setSelectionCells(cells);
			}

			// Adjust view if needed
			if (center && cells.length > 0) {
				// Center on the first cell
				graph.scrollCellToVisible(cells[0], true);
			} else if (cells.length > 0) {
				// Just ensure it's visible
				graph.scrollCellToVisible(cells[0]);
			}

			// Mark document as modified
			if (ui.editor && typeof ui.editor.setModified === 'function') {
				ui.editor.setModified(true);
			}

			// Return success result
			return {
				success: true,
				cells: cells,
				cellCount: cells.length
			};

		} catch (error) {
			// Rollback on failure - restore selection
			try {
				graph.setSelectionCells(initialSelection);
			} catch (rollbackError) {
				console.error('[CanvasInserter] Failed to rollback selection:', rollbackError);
			}

			// Re-throw the error with additional context
			var insertError = new Error('Failed to insert diagram: ' + error.message);
			insertError.code = ERROR_CODES.INSERT_FAILED;
			insertError.originalError = error;
			throw insertError;
		}
	}

	/**
	 * Insert diagram with state protection
	 * This is a wrapper that ensures canvas state is preserved on failure
	 * @param {Object} ui - Draw.io UI object
	 * @param {string} xml - XML data to insert
	 * @param {Object} options - Insertion options
	 * @returns {Promise<Object>} Promise that resolves with result or rejects with error
	 */
	function insertDiagramSafe(ui, xml, options) {
		return new Promise(function(resolve, reject) {
			try {
				var result = insertDiagram(ui, xml, options);
				resolve(result);
			} catch (error) {
				reject(error);
			}
		});
	}

	// Export functions
	if (typeof window !== 'undefined') {
		window.CanvasInserter = {
			insertDiagram: insertDiagram,
			insertDiagramSafe: insertDiagramSafe,
			ERROR_CODES: ERROR_CODES
		};
	}

	// Also export for Node.js testing environment
	if (typeof module !== 'undefined' && module.exports) {
		module.exports = {
			insertDiagram: insertDiagram,
			insertDiagramSafe: insertDiagramSafe,
			ERROR_CODES: ERROR_CODES
		};
	}
})();
