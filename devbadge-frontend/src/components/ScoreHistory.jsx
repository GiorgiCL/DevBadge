import React from 'react';
import './ScoreHistory.css';

/**
 * Score history component to show past calculations
 * Props:
 * - history: array of historical score records from backend
 */
const ScoreHistory = ({ history }) => {
    // If no history, show message
    if (!history || history.length === 0) {
        return (
            <div className="history-container">
                <h3 className="history-title">📊 Score History</h3>
                <p className="no-history">No previous calculations found for this user.</p>
            </div>
        );
    }

    // Format date to readable string
    const formatDate = (dateString) => {
        const date = new Date(dateString);
        return date.toLocaleDateString('en-US', {
            year: 'numeric',
            month: 'short',
            day: 'numeric',
            hour: '2-digit',
            minute: '2-digit'
        });
    };

    // Get color for score
    const getScoreColor = (value) => {
        if (value >= 8) return '#10b981';
        if (value >= 6) return '#f59e0b';
        if (value >= 4) return '#ef4444';
        return '#6b7280';
    };

    return (
        <div className="history-container">
            <h3 className="history-title">📊 Score History</h3>
            <p className="history-subtitle">
                Showing {history.length} previous calculation{history.length !== 1 ? 's' : ''}
            </p>

            <div className="history-table-wrapper">
                <table className="history-table">
                    <thead>
                    <tr>
                        <th>Date & Time</th>
                        <th>Overall</th>
                        <th>Commit Quality</th>
                        <th>Consistency</th>
                        <th>Collaboration</th>
                        <th>Impact</th>
                        <th>Code Review</th>
                    </tr>
                    </thead>
                    <tbody>
                    {history.map((record, index) => (
                        <tr key={index} className="history-row">
                            <td className="date-cell">
                                {formatDate(record.calculatedAt)}
                            </td>
                            <td>
                  <span
                      className="score-badge overall"
                      style={{ backgroundColor: getScoreColor(record.overall) }}
                  >
                    {record.overall.toFixed(1)}
                  </span>
                            </td>
                            <td>
                  <span
                      className="score-badge"
                      style={{ color: getScoreColor(record.commitQuality) }}
                  >
                    {record.commitQuality.toFixed(1)}
                  </span>
                            </td>
                            <td>
                  <span
                      className="score-badge"
                      style={{ color: getScoreColor(record.consistency) }}
                  >
                    {record.consistency.toFixed(1)}
                  </span>
                            </td>
                            <td>
                  <span
                      className="score-badge"
                      style={{ color: getScoreColor(record.collaboration) }}
                  >
                    {record.collaboration.toFixed(1)}
                  </span>
                            </td>
                            <td>
                  <span
                      className="score-badge"
                      style={{ color: getScoreColor(record.impact) }}
                  >
                    {record.impact.toFixed(1)}
                  </span>
                            </td>
                            <td>
                  <span
                      className="score-badge"
                      style={{ color: getScoreColor(record.codeReview) }}
                  >
                    {record.codeReview.toFixed(1)}
                  </span>
                            </td>
                        </tr>
                    ))}
                    </tbody>
                </table>
            </div>
        </div>
    );
};

export default ScoreHistory;