import React, { useEffect, useState } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { getUserScore, getUserHistory } from '../api/api';
import ScoreCard from '../components/ScoreCard';
import ScoreHistory from '../components/ScoreHistory';
import './UserScorePage.css';

const UserScorePage = () => {
    const { username } = useParams();
    const navigate = useNavigate();

    const [scoreData, setScoreData] = useState(null);
    const [historyData, setHistoryData] = useState([]);
    const [loading, setLoading] = useState(true);
    const [syncing, setSyncing] = useState(false);
    const [error, setError] = useState(null);

    const fetchUserData = async () => {
        try {
            setLoading(true);
            setError(null);
            setSyncing(false);

            const scoreResponse = await getUserScore(username);

            if (scoreResponse?.code === 'USER_NOT_IN_DB') {
                setSyncing(true);
                setLoading(false);
                setTimeout(fetchUserData, 60000);
                return;
            }

            const history = await getUserHistory(username);

            setScoreData(scoreResponse);
            setHistoryData(history);
        } catch (err) {
            if (err.response?.status === 202) {
                setSyncing(true);
                setLoading(false);
                setTimeout(fetchUserData, 60000);
                return;
            }

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

    useEffect(() => {
        fetchUserData();
    }, [username]);

    const handleBackToHome = () => {
        navigate('/');
    };

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

    if (syncing) {
        return (
            <div className="user-score-page">
                <div className="loading-container">
                    <div className="loading-spinner"></div>
                    <h2>Fetching data from GitHub…</h2>
                    <p>User was not yet stored. Data is being prepared. Please wait a moment.</p>
                </div>
            </div>
        );
    }

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

    return (
        <div className="user-score-page">
            <div className="page-container">
                <button onClick={handleBackToHome} className="back-link">
                    ← Back to Search
                </button>

                {scoreData && <ScoreCard score={scoreData} />}
                {historyData && <ScoreHistory history={historyData} />}

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
