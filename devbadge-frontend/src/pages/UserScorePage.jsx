import React, { useEffect, useState } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { getUserScore, getUserHistory } from '../api/api';
import ScoreCard from '../components/ScoreCard';
import ScoreHistory from '../components/ScoreHistory';
import './UserScorePage.css';

/**
 * User Score Page - Shows score and history for a specific user
 * Gets username from URL parameter
 */
const UserScorePage = () => {
    // Get username from URL (e.g., /user/torvalds -> username = "torvalds")
    const { username } = useParams();
    const navigate = useNavigate();

    // State management
    const [scoreData, setScoreData] = useState(null);
    const [historyData, setHistoryData] = useState([]);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState(null);

    // Function to fetch both score and history
    const fetchUserData = async () => {
        try {
            setLoading(true);
            setError(null);

            // Call both API endpoints in parallel
            const [score, history] = await Promise.all([
                getUserScore(username),
                getUserHistory(username)
            ]);

            setScoreData(score);
            setHistoryData(history);
        } catch (err) {
            console.error('Error fetching user data:', err);

            // Handle different types of errors
            if (err.response?.status === 404) {
                setError(`User "${username}" not found on GitHub`);
            } else if (err.response?.status === 429) {
                setError('Rate limit exceeded. Please try again later.');
            } else {
                setError('Failed to fetch user data. Please try again.');
            }
        } finally {
            setLoading(false);
        }
    };

    // Fetch data when component loads or username changes
    useEffect(() => {
        fetchUserData();
    }, [username]);

    // Go back to home page
    const handleBackToHome = () => {
        navigate('/');
    };

    // Loading state
    if (loading) {
        return (
            <div className="user-score-page">
                <div className="loading-container">
                    <div className="loading-spinner"></div>
                    <h2>Analyzing {username}'s GitHub profile...</h2>
                    <p>This may take a few seconds</p>
                </div>
            </div>
        );
    }

    // Error state
    if (error) {
        return (
            <div className="user-score-page">
                <div className="error-container">
                    <div className="error-icon">❌</div>
                    <h2>Oops! Something went wrong</h2>
                    <p className="error-message">{error}</p>
                    <button onClick={handleBackToHome} className="back-button">
                        ← Back to Home
                    </button>
                </div>
            </div>
        );
    }

    // Success state - show data
    return (
        <div className="user-score-page">
            <div className="page-container">
                {/* Back button */}
                <button onClick={handleBackToHome} className="back-link">
                    ← Back to Search
                </button>

                {/* Score Card */}
                {scoreData && <ScoreCard score={scoreData} />}

                {/* Score History */}
                {historyData && <ScoreHistory history={historyData} />}

                {/* Refresh button */}
                <div className="refresh-section">
                    <button onClick={fetchUserData} className="refresh-button">
                        🔄 Refresh Scores
                    </button>
                    <p className="refresh-hint">
                        Scores are cached for 1 hour to reduce API calls
                    </p>
                </div>
            </div>
        </div>
    );
};

export default UserScorePage;