import React, { useState } from 'react';
import './SearchBar.css';

/**
 * Search bar component for entering GitHub username
 * Props:
 * - onSearch: function to call when user submits username
 * - loading: boolean to show loading state
 */
const SearchBar = ({ onSearch, loading }) => {
    const [username, setUsername] = useState('');

    const handleSubmit = (e) => {
        e.preventDefault();

        // Don't search if input is empty
        if (!username.trim()) {
            alert('Please enter a GitHub username');
            return;
        }

        // Call the parent component's search function
        onSearch(username.trim());
    };

    return (
        <div className="search-container">
            <form onSubmit={handleSubmit} className="search-form">
                <div className="search-input-wrapper">
                    <span className="search-icon">🔍</span>
                    <input
                        type="text"
                        placeholder="Enter GitHub username (e.g., torvalds)"
                        value={username}
                        onChange={(e) => setUsername(e.target.value)}
                        className="search-input"
                        disabled={loading}
                    />
                </div>

                <button
                    type="submit"
                    className="search-button"
                    disabled={loading}
                >
                    {loading ? (
                        <>
                            <span className="spinner"></span>
                            Analyzing...
                        </>
                    ) : (
                        'Analyze Profile'
                    )}
                </button>
            </form>

            <p className="search-hint">
                💡 Try: torvalds, gaearon, addyosmani, or any GitHub username
            </p>
        </div>
    );
};

export default SearchBar;