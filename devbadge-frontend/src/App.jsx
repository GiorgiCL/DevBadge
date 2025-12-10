import React from 'react';
import { BrowserRouter as Router, Routes, Route } from 'react-router-dom';
import Navbar from './components/Navbar';
import HomePage from './pages/HomePage';
import UserScorePage from './pages/UserScorePage';
import './App.css';

/**
 * Main App Component
 * Sets up routing and navigation
 */
function App() {
    return (
        <Router>
            <div className="App">
                {/* Navigation bar appears on all pages */}
                <Navbar />

                {/* Routes define which component shows for each URL */}
                <Routes>
                    {/* Home page at / */}
                    <Route path="/" element={<HomePage />} />

                    {/* User score page at /user/:username */}
                    <Route path="/user/:username" element={<UserScorePage />} />
                </Routes>
            </div>
        </Router>
    );
}

export default App;