import React from 'react';
import { Link } from 'react-router-dom';
import './Navbar.css';

/**
 * Navigation bar component
 * Shows the app logo and navigation links
 */
const Navbar = () => {
    return (
        <nav className="navbar">
            <div className="navbar-container">
                <Link to="/" className="navbar-logo">
                    <span className="logo-icon">🏆</span>
                    DevBadge
                </Link>

                <div className="navbar-links">
                    <Link to="/" className="nav-link">
                        Home
                    </Link>
                    <a
                        href="https://github.com"
                        target="_blank"
                        rel="noopener noreferrer"
                        className="nav-link"
                    >
                        GitHub
                    </a>
                </div>
            </div>
        </nav>
    );
};

export default Navbar;