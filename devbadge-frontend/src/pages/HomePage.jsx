import React from 'react';
import { useNavigate } from 'react-router-dom';
import SearchBar from '../components/SearchBar';
import './HomePage.css';

/**
 * Home page - landing page with search functionality
 */
const HomePage = () => {
    const navigate = useNavigate();
    const [loading, setLoading] = React.useState(false);

    // Handle when user searches for a username
    const handleSearch = (username) => {
        setLoading(true);

        // Navigate to user score page
        // The UserScorePage will handle fetching the data
        navigate(`/user/${username}`);

        // Reset loading after navigation
        setTimeout(() => setLoading(false), 500);
    };

    return (
        <div className="home-page">
            <div className="hero-section">
                {/* Header */}
                <div className="hero-content">
                    <h1 className="hero-title">
                        <span className="gradient-text">DevBadge</span>
                        <span className="wave">👋</span>
                    </h1>
                    <p className="hero-subtitle">
                        Analyze any GitHub developer's profile and get detailed contribution scores
                    </p>

                    {/* Feature badges */}
                    <div className="feature-badges">
                        <span className="badge">✍️ Commit Quality</span>
                        <span className="badge">📅 Consistency</span>
                        <span className="badge">🤝 Collaboration</span>
                        <span className="badge">💥 Impact</span>
                        <span className="badge">🔍 Code Review</span>
                    </div>
                </div>

                {/* Search Bar */}
                <div className="search-section">
                    <SearchBar onSearch={handleSearch} loading={loading} />
                </div>
            </div>

            {/* How it works section */}
            <div className="info-section">
                <h2 className="info-title">How It Works</h2>
                <div className="info-grid">
                    <div className="info-card">
                        <div className="info-icon">🔍</div>
                        <h3>1. Search</h3>
                        <p>Enter any GitHub username to analyze</p>
                    </div>

                    <div className="info-card">
                        <div className="info-icon">⚡</div>
                        <h3>2. Analyze</h3>
                        <p>We fetch and analyze their public GitHub activity</p>
                    </div>

                    <div className="info-card">
                        <div className="info-icon">📊</div>
                        <h3>3. Score</h3>
                        <p>Get detailed scores across 5 key metrics</p>
                    </div>

                    <div className="info-card">
                        <div className="info-icon">📈</div>
                        <h3>4. Track</h3>
                        <p>View historical scores and progress over time</p>
                    </div>
                </div>
            </div>

            {/* What we measure section */}
            <div className="metrics-info-section">
                <h2 className="info-title">What We Measure</h2>
                <div className="metrics-info-grid">
                    <div className="metric-info-card">
                        <h4>✍️ Commit Quality</h4>
                        <p>Quality of commit messages, change scope, and commit patterns</p>
                    </div>

                    <div className="metric-info-card">
                        <h4>📅 Consistency</h4>
                        <p>Regular contribution patterns and sustained activity over time</p>
                    </div>

                    <div className="metric-info-card">
                        <h4>🤝 Collaboration</h4>
                        <p>Pull requests, issue participation, and team contributions</p>
                    </div>

                    <div className="metric-info-card">
                        <h4>💥 Impact</h4>
                        <p>Size and significance of changes, merged pull requests</p>
                    </div>

                    <div className="metric-info-card">
                        <h4>🔍 Code Review</h4>
                        <p>Review comments, feedback quality, and review participation</p>
                    </div>
                </div>
            </div>
        </div>
    );
};

export default HomePage;