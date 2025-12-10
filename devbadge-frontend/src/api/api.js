import axios from 'axios';

// Base URL for your Spring Boot backend
const API_BASE_URL = '/api';

// Create axios instance with default config
const api = axios.create({
    baseURL: API_BASE_URL,
    headers: {
        'Content-Type': 'application/json',
    },
});

// API Functions that match your backend endpoints

/**
 * Get score for a GitHub username
 * Calls: GET /api/score/{username}
 */
export const getUserScore = async (username) => {
    try {
        const response = await api.get(`/score/${username}`);
        return response.data;
    } catch (error) {
        console.error('Error fetching user score:', error);
        throw error;
    }
};

/**
 * Get score history for a GitHub username
 * Calls: GET /api/score/{username}/history
 */
export const getUserHistory = async (username) => {
    try {
        const response = await api.get(`/score/${username}/history`);
        return response.data;
    } catch (error) {
        console.error('Error fetching user history:', error);
        throw error;
    }
};

/**
 * Get all GitHub users from database
 * Calls: GET /api/users
 * Note: Requires authentication (you can add this later)
 */
export const getAllUsers = async () => {
    try {
        const response = await api.get(`/users`);
        return response.data;
    } catch (error) {
        console.error('Error fetching all users:', error);
        throw error;
    }
};

export default api;