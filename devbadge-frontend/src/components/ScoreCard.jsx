import React from 'react';
import './ScoreCard.css';

/**
 * Score card component to display user scores
 * Props:
 * - score: object containing all score data from backend
 */
const ScoreCard = ({ score }) => {
    // Function to get color based on score value
    const getScoreColor = (value) => {
        if (value >= 8) return '#10b981'; // Green for excellent
        if (value >= 6) return '#f59e0b'; // Orange for good
        if (value >= 4) return '#ef4444'; // Red for needs improvement
        return '#6b7280'; // Gray for low
    };

    // Function to get performance label
    const getPerformanceLabel = (value) => {
        if (value >= 8) return 'Excellent';
        if (value >= 6) return 'Good';
        if (value >= 4) return 'Fair';
        return 'Needs Work';
    };

    // Individual score metrics from your backend
    const metrics = [
        {
            name: 'Commit Quality',
            value: score.commitQuality,
            icon: '✍️',
            description: 'Quality of commit messages and changes'
        },
        {
            name: 'Consistency',
            value: score.consistency,
            icon: '📅',
            description: 'Regular contribution patterns'
        },
        {
            name: 'Collaboration',
            value: score.collaboration,
            icon: '🤝',
            description: 'Pull requests and issue participation'
        },
        {
            name: 'Impact',
            value: score.impact,
            icon: '💥',
            description: 'Scope and significance of changes'
        },
        {
            name: 'Code Review',
            value: score.codeReview,
            icon: '🔍',
            description: 'Review comments and feedback'
        },
    ];

    return (
        <div className="score-card">
            {/* Header with username */}
            <div className="score-header">
                <h2 className="score-username">
                    <span className="github-icon">😺</span>
                    {score.username}
                </h2>
                <a
                    href={`https://github.com/${score.username}`}
                    target="_blank"
                    rel="noopener noreferrer"
                    className="github-link"
                >
                    View on GitHub →
                </a>
            </div>

            {/* Overall Score - Big Display */}
            <div className="overall-score">
                <div className="overall-score-circle">
                    <div
                        className="score-value"
                        style={{ color: getScoreColor(score.overallScore) }}
                    >
                        {score.overallScore.toFixed(1)}
                    </div>
                    <div className="score-max">/10</div>
                </div>
                <div className="overall-label">
                    <h3>Overall Score</h3>
                    <span
                        className="performance-badge"
                        style={{ backgroundColor: getScoreColor(score.overallScore) }}
                    >
            {getPerformanceLabel(score.overallScore)}
          </span>
                </div>
            </div>

            {/* Individual Metrics */}
            <div className="metrics-grid">
                {metrics.map((metric) => (
                    <div key={metric.name} className="metric-card">
                        <div className="metric-header">
                            <span className="metric-icon">{metric.icon}</span>
                            <h4 className="metric-name">{metric.name}</h4>
                        </div>

                        <div className="metric-score">
              <span
                  className="metric-value"
                  style={{ color: getScoreColor(metric.value) }}
              >
                {metric.value.toFixed(1)}
              </span>
                            <span className="metric-max">/10</span>
                        </div>

                        {/* Progress Bar */}
                        <div className="metric-progress-bar">
                            <div
                                className="metric-progress-fill"
                                style={{
                                    width: `${(metric.value / 10) * 100}%`,
                                    backgroundColor: getScoreColor(metric.value)
                                }}
                            ></div>
                        </div>

                        <p className="metric-description">{metric.description}</p>
                    </div>
                ))}
            </div>
        </div>
    );
};

export default ScoreCard;